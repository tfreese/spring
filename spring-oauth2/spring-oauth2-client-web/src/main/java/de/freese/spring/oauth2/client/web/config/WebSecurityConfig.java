/**
 * Created: 31.10.2019
 */
package de.freese.spring.oauth2.client.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @author Thomas Freese
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    /**
     * @see org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter#configure(org.springframework.security.config.annotation.web.builders.HttpSecurity)
     */
    @Override
    public void configure(final HttpSecurity http) throws Exception
    {
        // @formatter:off
        http.authorizeRequests()
            .antMatchers("/", "/unsecured", "/login**").permitAll()
            .anyRequest().authenticated()
//            .and()
//                .formLogin().permitAll()
            .and()
                .oauth2Login()
            .and()
                .logout()
                    .logoutSuccessUrl("/").permitAll()
        ;
//        http.antMatcher("/**").authorizeRequests()
//            .antMatchers("/", "/login**").permitAll()
//            .anyRequest().authenticated()
//            .and()
//                .oauth2Login()
//            .and()
//            .logout()
//                .permitAll()
//                .clearAuthentication(true)
//                .deleteCookies("JSESSIONID", "remember-me", "UISESSION")
//                .invalidateHttpSession(true)
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/")
//            ;
        // @formatter:on
    }
}
