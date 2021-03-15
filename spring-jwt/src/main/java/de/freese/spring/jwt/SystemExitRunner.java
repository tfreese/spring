/**
 * Created: 12.09.2018
 */
package de.freese.spring.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Thomas Freese
 */
@Component
@Profile("!test")
@Order(20)
public class SystemExitRunner implements CommandLineRunner
{
    /**
    *
    */
    public static final Logger LOGGER = LoggerFactory.getLogger(SystemExitRunner.class);

    /**
     * @see org.springframework.boot.CommandLineRunner#run(java.lang.String[])
     */
    @Override
    public void run(final String...args) throws Exception
    {
        LOGGER.info("");

        // -Drun_in_ide=true
        // In der Runtime als Default VM-Argument setzen oder in der eclipse.ini
        // if (Boolean.parseBoolean(System.getenv("run_in_ide")) || Boolean.parseBoolean(System.getProperty("run_in_ide", "false")))
        // {
        System.out.println();
        System.out.println("******************************************************************************************************************");
        System.out.println("You're using an IDE, click in this console and press ENTER to call System.exit() and trigger the shutdown routine.");
        System.out.println("******************************************************************************************************************");

        try
        {
            System.in.read();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        System.exit(0);
        // }
    }
}
