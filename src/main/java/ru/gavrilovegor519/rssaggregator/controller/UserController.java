package ru.gavrilovegor519.rssaggregator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gavrilovegor519.rssaggregator.dto.input.user.LoginDto;
import ru.gavrilovegor519.rssaggregator.dto.input.user.RegDto;
import ru.gavrilovegor519.rssaggregator.dto.output.user.TokenDto;
import ru.gavrilovegor519.rssaggregator.entity.User;
import ru.gavrilovegor519.rssaggregator.mapper.UserMapper;
import ru.gavrilovegor519.rssaggregator.service.UserService;

@RestController
@RequestMapping("/api/1.0/user")
@AllArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    @Operation(summary = "Get JWT token by login and password",
            responses = {
                    @ApiResponse(description = "JWT token for user",
                            responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "User is not found or incorrect password")
            })
    public TokenDto login(@Parameter(required = true, description = "User data")
                              @RequestBody @Valid LoginDto loginDto) {
        User entity = userMapper.toEntity(loginDto);
        return userService.login(entity);
    }

    @PostMapping("/reg")
    @Operation(summary = "Register the user for using this service",
            responses = {
                    @ApiResponse(description = "User is registered",
                            responseCode = "200"),
                    @ApiResponse(responseCode = "409",
                            description = "Duplicate registration data")
            })
    public void reg(@Parameter(required = true, description = "User data")
                        @RequestBody @Valid RegDto regDto) {
        User entity = userMapper.toEntity(regDto);
        userService.reg(entity);
    }
}
