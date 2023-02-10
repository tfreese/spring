// Created: 30.10.2018
package de.freese.spring.thymeleaf.config;

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
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        // cacheManager.setCacheNames(List.of("userCache")); // Fest verdrahtete Cache-Namen, damit wäre er nicht mehr dynamisch.

        return cacheManager;
    }

    @Bean
    UserCache userCache(final CacheManager cacheManager) {
        Cache cache = cacheManager.getCache("userCache");

        return new SpringCacheBasedUserCache(cache);
    }
}
