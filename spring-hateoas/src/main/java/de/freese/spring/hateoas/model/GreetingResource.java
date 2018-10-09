// Created: 08.08.2016
package de.freese.spring.hateoas.model;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

/**
 * @author Thomas Freese
 */
// @XmlRootElement
public class GreetingResource extends Resource<GreetingPOJO>
{
    /**
     * Erzeugt eine neue Instanz von {@link GreetingResource}
     */
    public GreetingResource()
    {
        super(null);
    }

    /**
     * Erzeugt eine neue Instanz von {@link GreetingResource}
     *
     * @param content {@link GreetingPOJO}
     * @param links {@link Link}[]
     */
    public GreetingResource(final GreetingPOJO content, final Link...links)
    {
        super(content, links);
    }
}
