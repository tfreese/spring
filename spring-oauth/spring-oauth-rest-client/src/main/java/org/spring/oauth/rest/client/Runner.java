/**
 * Created: 01.10.2018
 */

package org.spring.oauth.rest.client;

import javax.annotation.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author Thomas Freese
 */
@Component
public class Runner implements CommandLineRunner
{
    /**
     *
     */
    @Resource
    private RestTemplate restTemplate = null;

    /**
     * Erstellt ein neues {@link Runner} Object.
     */
    public Runner()
    {
        super();
    }

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        System.out.println(this.restTemplate);

        String response = this.restTemplate.getForObject("http://localhost:8081/auth/user/me", String.class);

        System.out.println(response);
    }
}
