package ru.gavrilovegor519.rssaggregator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.gavrilovegor519.rssaggregator.dto.input.feed.FeedInputDto;
import ru.gavrilovegor519.rssaggregator.dto.output.feed.FeedOutputDto;
import ru.gavrilovegor519.rssaggregator.dto.output.feed.NewsEntryDto;
import ru.gavrilovegor519.rssaggregator.entity.Feed;
import ru.gavrilovegor519.rssaggregator.mapper.FeedMapper;
import ru.gavrilovegor519.rssaggregator.service.FeedService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/1.0/feed")
@PreAuthorize("isAuthenticated()")
@AllArgsConstructor
public class FeedController {
    private final FeedService feedService;
    private final FeedMapper feedMapper;

    @PostMapping("/add")
    @Operation(summary = "Add new feed",
            responses = {
                    @ApiResponse(description = "Feed is added",
                            responseCode = "200"),
                    @ApiResponse(responseCode = "409",
                            description = "Duplicate feed data"),
                    @ApiResponse(responseCode = "403",
                            description = "User is not authenticated")
            })
    public ResponseEntity<FeedOutputDto> addFeed(@Parameter(description = "Feed data", required = true)
                            @RequestBody @Valid FeedInputDto dto,
                        Authentication authentication) {
        Feed feed = feedMapper.map(dto);
        Feed added = feedService.addFeed(feed, authentication.getName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(added.getId()).toUri();
        return ResponseEntity.created(location).body(feedMapper.mapOutput(added));
    }

    @GetMapping("/list")
    @Operation(summary = "List of feeds",
            responses = {
                    @ApiResponse(description = "List of feeds is returned",
                            responseCode = "200"),
                    @ApiResponse(responseCode = "403",
                            description = "User is not authenticated")
            })
    public ResponseEntity<List<FeedOutputDto>> listFeeds(Authentication authentication) {
        List<Feed> feeds = feedService.getFeeds(authentication.getName());
        return ResponseEntity.ok(feedMapper.mapOutputToList(feeds));
    }

    @DeleteMapping("/delete/{id}")
    @Operation(summary = "Delete feed",
            responses = {
                    @ApiResponse(description = "Feed is deleted",
                            responseCode = "204"),
                    @ApiResponse(responseCode = "403",
                            description = "User is not authenticated")
            })
    public ResponseEntity<Void> deleteFeed(@Parameter(description = "Feed id", required = true)
                               @PathVariable long id,
                           Authentication authentication) {
        feedService.deleteFeed(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/headlines")
    @Operation(summary = "News headlines",
            responses = {
                    @ApiResponse(description = "The list of news headings is returned",
                            responseCode = "200"),
                    @ApiResponse(responseCode = "400",
                            description = "Incorrect request"),
                    @ApiResponse(responseCode = "403",
                            description = "User is not authenticated")
            })
    public ResponseEntity<Page<NewsEntryDto>> getNewsFromAllFeeds(Authentication authentication,
                                                                  @Parameter(description = "Page number", required = true)
                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @Parameter(description = "Page size", required = true)
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(feedService.getNewsHeadings(authentication.getName(), pageable));
    }
}
