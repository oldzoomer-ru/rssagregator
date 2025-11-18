package ru.gavrilovegor519.rssaggregator.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JwtTokenProvider {
    private final JwtUtilities jwtUtils;

    public String resolveToken(HttpServletRequest request) {
        return jwtUtils.getToken(request);
    }
}
