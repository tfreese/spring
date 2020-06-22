/**
 * Created: 30.10.2018
 */

package de.freese.spring.jwt.config;

import java.util.List;
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
     * Erstellt ein neues {@link CacheConfig} Object.
     */
    public CacheConfig()
    {
        super();
    }

    /**
     * @return {@link CacheManager}
     */
    @Bean
    public CacheManager cacheManager()
    {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setAllowNullValues(false);
        cacheManager.setCacheNames(List.of("userCache"));

        return cacheManager;
    }

    /**
     * @param cacheManager {@link CacheManager}
     * @return {@link CacheManager}
     * @throws Exception Falls was schief geht.
     */
    @Bean
    public UserCache userCache(final CacheManager cacheManager) throws Exception
    {
        Cache cache = cacheManager.getCache("userCache");
        SpringCacheBasedUserCache userCache = new SpringCacheBasedUserCache(cache);

        return userCache;
    }
}
