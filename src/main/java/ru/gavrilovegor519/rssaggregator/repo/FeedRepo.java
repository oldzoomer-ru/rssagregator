package ru.gavrilovegor519.rssaggregator.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gavrilovegor519.rssaggregator.entity.Feed;

import java.util.List;

public interface FeedRepo extends JpaRepository<Feed, Long> {
    List<Feed> findByEmail(String email);
}
