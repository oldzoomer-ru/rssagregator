package ru.gavrilovegor519.rssaggregator.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gavrilovegor519.rssaggregator.ErrorMsg;
import ru.gavrilovegor519.rssaggregator.dto.output.user.TokenDto;
import ru.gavrilovegor519.rssaggregator.entity.User;
import ru.gavrilovegor519.rssaggregator.exception.DuplicateUserException;
import ru.gavrilovegor519.rssaggregator.exception.IncorrectPasswordException;
import ru.gavrilovegor519.rssaggregator.exception.UserNotFoundException;
import ru.gavrilovegor519.rssaggregator.repo.UserRepo;
import ru.gavrilovegor519.rssaggregator.security.JwtUtilities;
import ru.gavrilovegor519.rssaggregator.service.UserService;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final JwtUtilities jwtUtilities;
    private final UserRepo userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public TokenDto login(User user) {
        String email = user.getEmail();
        String password = user.getPassword();

        User dbUser = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException(String.format(ErrorMsg.USER_NOT_FOUND, email)));

        validatePassword(password, dbUser.getPassword());
        return TokenDto.builder()
                .token(jwtUtilities.generateToken(dbUser.getUsername(), "ROLE_USER"))
                .build();
    }

    @Override
    @Transactional
    public void reg(User user) {
        String email = user.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateUserException(String.format(ErrorMsg.DUPLICATE_USER, email));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    private void validatePassword(String raw, String encoded) {
        if (!passwordEncoder.matches(raw, encoded)) {
            throw new IncorrectPasswordException("Incorrect password for email: " + raw);
        }
    }
}
