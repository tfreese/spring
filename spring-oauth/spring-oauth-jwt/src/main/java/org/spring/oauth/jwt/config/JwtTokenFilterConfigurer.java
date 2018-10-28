/**
 * Created: 28.10.2018
 */

package org.spring.oauth.jwt.config;

import java.util.Objects;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author Thomas Freese
 */
public class JwtTokenFilterConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>
{
    /**
     *
     */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Erstellt ein neues {@link JwtTokenFilterConfigurer} Object.
     *
     * @param jwtTokenProvider {@link JwtTokenProvider}
     */
    public JwtTokenFilterConfigurer(final JwtTokenProvider jwtTokenProvider)
    {
        super();

        this.jwtTokenProvider = Objects.requireNonNull(jwtTokenProvider, "jwtTokenProvider required");
    }

    /**
     * @see org.springframework.security.config.annotation.SecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.SecurityBuilder)
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception
    {
        JwtTokenFilter jwtTokenFilter = new JwtTokenFilter(this.jwtTokenProvider);

        http.addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
