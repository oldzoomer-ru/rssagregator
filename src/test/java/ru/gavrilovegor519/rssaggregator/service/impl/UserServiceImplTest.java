package ru.gavrilovegor519.rssaggregator.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import ru.gavrilovegor519.rssaggregator.entity.User;
import ru.gavrilovegor519.rssaggregator.exception.DuplicateUserException;
import ru.gavrilovegor519.rssaggregator.exception.IncorrectPasswordException;
import ru.gavrilovegor519.rssaggregator.exception.UserNotFoundException;
import ru.gavrilovegor519.rssaggregator.repo.UserRepo;
import ru.gavrilovegor519.rssaggregator.security.JwtUtilities;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private JwtUtilities jwtUtilities;

    @Mock
    private UserRepo userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loginWithExistUser() throws UserNotFoundException, IncorrectPasswordException {
        User user = mock(User.class);
        User loginDto = mock(User.class);

        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.login(loginDto));
    }

    @Test
    void loginWithExistUserButWithIncorrectPassword() {
        User user = mock(User.class);
        User loginDto = mock(User.class);

        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThrows(IncorrectPasswordException.class, () -> userService.login(loginDto));
    }

    @Test
    void loginWithNotExistUser() {
        User loginDto = mock(User.class);

        assertThrows(UserNotFoundException.class, () -> userService.login(loginDto));
    }

    @Test
    void registrationWithDuplicatedUser() {
        User loginDto = mock(User.class);

        when(userRepository.existsByEmail(any())).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> userService.reg(loginDto));
    }
}