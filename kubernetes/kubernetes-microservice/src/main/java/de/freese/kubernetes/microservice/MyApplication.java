package de.freese.kubernetes.microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Thomas Freese
 */
@SpringBootApplication
public final class MyApplication
{
    public static void main(final String[] args)
    {
        SpringApplication.run(MyApplication.class, args);
    }

    private MyApplication()
    {
        super();
    }
}
