package ru.gavrilovegor519.rssaggregator.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

        User user1 = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user1.getPassword())) {
            throw new IncorrectPasswordException("Incorrect password for email: " + email);
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setToken(jwtUtilities.generateToken(user1.getUsername(), "ROLE_USER"));
        return tokenDto;
    }

    @Override
    @Transactional
    public void reg(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateUserException("Duplicate E-Mail: " + user.getEmail());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }
}
