/**
 * Created: 12.09.2018
 */
package de.freese.spring.oauth2.client.rest;

import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(20)
public class RestTemplateRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(RestTemplateRunner.class);

    /**
     *
     */
    @Resource(name = "oAuth2RestTemplate")
    private RestTemplate restTemplate;

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        String principal = this.restTemplate.getForEntity("http://localhost:8888/res_srv/secured/user/me", String.class).getBody();
        System.out.println(principal);

        String json = this.restTemplate.getForEntity("http://localhost:8888/res_srv/secured", String.class).getBody();
        System.out.println(json);
    }
}
