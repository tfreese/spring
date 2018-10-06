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
        System.out.println();
        String response = this.restTemplate.getForObject("http://localhost:8081/auth/rest/me", String.class);
        System.out.println("Prinzipal = " + response);

        System.out.println();
        response = this.restTemplate.getForObject("http://localhost:8081/auth/rest/hello", String.class);
        System.out.println(response);

        System.out.println();

        while (true)
        {
            try
            {
                Thread.sleep(30000);
                response = this.restTemplate.getForObject("http://localhost:8081/auth/rest/me", String.class);
                System.out.println(response);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                break;
            }
        }
    }
}
