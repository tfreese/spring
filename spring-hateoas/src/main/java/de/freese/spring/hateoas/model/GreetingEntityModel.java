// Created: 08.08.2016
package de.freese.spring.hateoas.model;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

/**
 * @author Thomas Freese
 */
// @XmlRootElement
public class GreetingEntityModel extends EntityModel<GreetingPOJO>
{
    /**
     * Erzeugt eine neue Instanz von {@link GreetingEntityModel}
     */
    public GreetingEntityModel()
    {
        super(null);
    }

    /**
     * Erzeugt eine neue Instanz von {@link GreetingEntityModel}
     *
     * @param content {@link GreetingPOJO}
     * @param links {@link Link}[]
     */
    public GreetingEntityModel(final GreetingPOJO content, final Link...links)
    {
        super(content, links);
    }
}
