package ru.gavrilovegor519.rssaggregator.service.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gavrilovegor519.rssaggregator.ErrorMsg;
import ru.gavrilovegor519.rssaggregator.dto.output.feed.NewsEntryDto;
import ru.gavrilovegor519.rssaggregator.entity.Feed;
import ru.gavrilovegor519.rssaggregator.exception.DuplicateFeedException;
import ru.gavrilovegor519.rssaggregator.exception.FeedNotFoundException;
import ru.gavrilovegor519.rssaggregator.exception.IncorrectInputDataException;
import ru.gavrilovegor519.rssaggregator.repo.FeedRepo;
import ru.gavrilovegor519.rssaggregator.service.FeedService;
import ru.gavrilovegor519.rssaggregator.util.GetFeed;

import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class FeedServiceImpl implements FeedService {
    private FeedRepo feedRepo;

    @Override
    @Transactional
    public Feed addFeed(Feed feed, String email) {
        if (getFeedsInternal(email).stream()
                .anyMatch(f -> f.getUrl().equals(feed.getUrl()) ||
                        f.getName().equals(feed.getName()))) {
            throw new DuplicateFeedException(ErrorMsg.DUPLICATE_FEED);
        }
        feed.setEmail(email);
        return feedRepo.save(feed);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Feed> getFeeds(String email) {
        return getFeedsInternal(email);
    }

    @Override
    @Transactional
    public void deleteFeed(long id, String email) {
        Feed feed = getFeedsInternal(email).stream()
                .filter(f -> f.getId() == id)
                .findFirst()
                .orElseThrow(() -> new FeedNotFoundException(
                        String.format(ErrorMsg.FEED_NOT_FOUND, id)));

        feedRepo.delete(feed);
    }

    @Override
    @Cacheable(value = "mainNews", key = "{#email, #pageable.pageNumber, #pageable.pageSize}")
    public Page<NewsEntryDto> getNewsHeadings(String email, Pageable pageable) {
        List<Feed> feeds = getFeedsInternal(email);
        List<NewsEntryDto> allEntries = feeds.stream()
                .flatMap(feed -> GetFeed.getFeed(feed.getUrl())
                        .getEntries()
                        .stream()
                        .map(this::convertToDto))
                .sorted(Comparator.comparing(NewsEntryDto::getNewsDate).reversed())
                .toList();

        int total = allEntries.size();
        if (pageable.getOffset() >= total) {
            throw new IncorrectInputDataException("Page number out of bounds");
        }
        int endIndex = Math.min((int) pageable.getOffset() + pageable.getPageSize(), total);
        List<NewsEntryDto> pageContent = allEntries.subList((int) pageable.getOffset(), endIndex);
        return new PageImpl<>(pageContent, pageable, total);
    }

    private NewsEntryDto convertToDto(SyndEntry entry) {
        return NewsEntryDto.builder()
                .newsHead(entry.getTitle())
                .feedUrl(entry.getLink())
                .newsDate(entry.getPublishedDate().toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();
    }

    private List<Feed> getFeedsInternal(String email) {
        return feedRepo.findByEmail(email);
    }
}
