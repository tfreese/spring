// Created: 11.12.2021
package de.freese.spring.jwt;

import org.springframework.test.context.ActiveProfiles;

/**
 * @author Thomas Freese
 */
@ActiveProfiles(
{
        "test", "filterOnly"
})
class TestJwtTokenFilterOnly extends AbstractTestJwtToken
{
}
