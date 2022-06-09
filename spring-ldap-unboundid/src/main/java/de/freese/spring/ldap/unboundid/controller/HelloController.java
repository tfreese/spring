// Created: 17.02.2019
package de.freese.spring.ldap.unboundid.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.ldap.userdetails.Person;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Thomas Freese
 */
@RestController
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class HelloController
{
    @GetMapping("/")
    public String hello(Authentication authentication)
    {
        return "Hello, " + authentication.getName() + " !";
    }

    @GetMapping("/friendly")
    public String hello(@AuthenticationPrincipal Person person)
    {
        return "Hello, " + person.getGivenName() + " !";
    }
}
