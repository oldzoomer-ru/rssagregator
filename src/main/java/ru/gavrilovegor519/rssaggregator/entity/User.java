package ru.gavrilovegor519.rssaggregator.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "email", unique = true, length = 50, nullable = false)
    private String email;

    @Column(name = "password", length = 120, nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Setter(AccessLevel.NONE)
    private List<Feed> feeds = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> userRole = new HashSet<>();
        userRole.add(new SimpleGrantedAuthority("ROLE_USER")); // all users are users, not admins
        return userRole;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void addFeed(Feed feed) {
        feeds.add(feed);
        feed.setUser(this);
    }

    public void removeFeed(Feed feed) {
        feeds.remove(feed);
        feed.setUser(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
