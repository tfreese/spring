package de.spring.ai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Thomas Freese
 */
@Configuration
@Profile("!test")
@EnableScheduling
// Or
// @ConditionalOnProperty(value = "scheduling.enabled", havingValue = "true", matchIfMissing = true)
// @EnableScheduling
//
// And in Test-Class: @SpringBootTest(properties = {"scheduling.enabled: false"})
public class SchedulingConfig {
}
