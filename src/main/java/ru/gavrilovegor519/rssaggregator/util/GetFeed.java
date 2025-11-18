package ru.gavrilovegor519.rssaggregator.util;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import ru.gavrilovegor519.rssaggregator.exception.GetFeedException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;

public final class GetFeed {
    /**
     * This is a private constructor,
     * because here has only static methods.
     */
    private GetFeed() {}

    /**
     * This method allows getting an RSS feed from URL.
     * @param feedUrl URL of feed
     * @return Feed representation
     */
    public static SyndFeed getFeed(String feedUrl) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(feedUrl)).build();
        CompletableFuture<SyndFeed> feedFuture = client
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body)
                .thenApply(GetFeed::bodyToFeed)
                .orTimeout(5, SECONDS);
        try {
            return feedFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new GetFeedException("Can't get feed", e);
        }
    }

    /**
     * This is an internal method for convert InputStream
     * (which is used in Java 11+ HttpClient) to SyndFeed's
     * feed representation.
     * @param body HTTP body of RSS feed
     * @return Feed representation
     */
    private static SyndFeed bodyToFeed(InputStream body) {
        try {
            return new SyndFeedInput().build(new XmlReader(body));
        } catch (FeedException | IOException e) {
            throw new CompletionException(e);
        }
    }
}
