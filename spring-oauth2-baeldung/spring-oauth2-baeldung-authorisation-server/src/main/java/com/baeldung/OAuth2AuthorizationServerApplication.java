package com.baeldung;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 *
 */
@SpringBootApplication
public class OAuth2AuthorizationServerApplication
{
    /**
     * @param args String[]
     */
    public static void main(final String[] args)
    {
        SpringApplication.run(OAuth2AuthorizationServerApplication.class, args);
    }
}