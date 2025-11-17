package ru.gavrilovegor519.rssaggregator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.gavrilovegor519.rssaggregator.dto.input.user.LoginDto;
import ru.gavrilovegor519.rssaggregator.dto.input.user.RegDto;
import ru.gavrilovegor519.rssaggregator.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(LoginDto loginDto);
    User toEntity(RegDto regDto);
}
