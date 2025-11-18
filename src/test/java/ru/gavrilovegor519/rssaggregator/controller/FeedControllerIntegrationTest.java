package ru.gavrilovegor519.rssaggregator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gavrilovegor519.rssaggregator.config.TestContainersConfig;
import ru.gavrilovegor519.rssaggregator.dto.input.feed.FeedInputDto;
import ru.gavrilovegor519.rssaggregator.entity.Feed;
import ru.gavrilovegor519.rssaggregator.entity.User;
import ru.gavrilovegor519.rssaggregator.exception.UserNotFoundException;
import ru.gavrilovegor519.rssaggregator.repo.FeedRepo;
import ru.gavrilovegor519.rssaggregator.repo.UserRepo;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
public class FeedControllerIntegrationTest extends TestContainersConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FeedRepo feedRepository;

    @Autowired
    private UserRepo userRepository;

    @BeforeEach
    void setupTest() {
        User user = new User();
        user.setPassword("password");
        user.setEmail("test@example.com");

        Feed feed = new Feed();
        feed.setName("Test Feed");
        feed.setUrl("https://www.opennet.ru/opennews/opennews_all_noadv.rss");

        user.addFeed(feed);

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        feedRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testAddFeed() throws Exception {
        FeedInputDto feedInputDto = new FeedInputDto();
        feedInputDto.setName("Test Feed 2");
        feedInputDto.setUrl("https://www.example.com/rss");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/1.0/feed/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedInputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Feed 2"))
                .andExpect(jsonPath("$.url").value("https://www.example.com/rss"));

        User user = userRepository.findByEmail("test@example.com").orElseThrow(UserNotFoundException::new);
        Feed feed = user.getFeeds().stream().toList().getLast();

        assertFalse(user.getFeeds().isEmpty());
        assertEquals("Test Feed 2", feed.getName());
        assertEquals("https://www.example.com/rss", feed.getUrl());
        assertTrue(feedRepository.findById(feed.getId()).isPresent());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testListFeeds() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/1.0/feed/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("Test Feed"))
                .andExpect(jsonPath("$[0].url")
                        .value("https://www.opennet.ru/opennews/opennews_all_noadv.rss"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteFeed() throws Exception {
        long feedId = feedRepository.findAll().getLast().getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/1.0/feed/delete/" + feedId))
                .andExpect(status().isNoContent());

        assertTrue(feedRepository.findById(feedId).isEmpty());
    }

    @ParameterizedTest
    @WithMockUser(username = "test@example.com")
    @CsvSource({"0, 10", "1, 5"})
    void testGetNewsFromAllFeeds(String page, String size) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/1.0/feed/headlines")
                        .param("page", page)
                        .param("size", size))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
