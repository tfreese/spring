// Created: 30.10.2018
package de.freese.spring.thymeleaf.config;

import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

/**
 * @author Thomas Freese
 */
@Configuration
public class CacheConfig {
    @Bean
    CacheManager cacheManager() {
        final ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        // cacheManager.setCacheNames(List.of("userCache")); // Disable dynamic creation of Caches.

        return cacheManager;
    }

    @Bean
    UserCache userCache(final CacheManager cacheManager) {
        final Cache cache = cacheManager.getCache("userCache");

        Objects.requireNonNull(cache, "cache required");

        return new SpringCacheBasedUserCache(cache);
    }
}
