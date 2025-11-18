package ru.gavrilovegor519.rssaggregator.dto.output.user;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenDto {
    private String token;
}
