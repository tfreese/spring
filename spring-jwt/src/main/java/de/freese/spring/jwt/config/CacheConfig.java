// Created: 30.10.2018
package de.freese.spring.jwt.config;

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
public class CacheConfig
{
    /**
     * @return {@link CacheManager}
     */
    @Bean
    public CacheManager cacheManager()
    {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        // cacheManager.setCacheNames(List.of("userCache")); // Damit wäre er nicht mehr dynamisch -> fest verdrahtete Cache-Namen.

        return cacheManager;
    }

    /**
     * @param cacheManager {@link CacheManager}
     *
     * @return {@link CacheManager}
     *
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public UserCache userCache(final CacheManager cacheManager) throws Exception
    {
        Cache cache = cacheManager.getCache("userCache");

        return new SpringCacheBasedUserCache(cache);
    }
}
