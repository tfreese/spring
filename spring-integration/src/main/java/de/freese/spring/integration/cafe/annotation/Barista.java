/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe.annotation;

import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;

import de.freese.spring.integration.cafe.Drink;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
@Component
public class Barista extends de.freese.spring.integration.cafe.xml.Barista
{
    /**
     * Erstellt ein neues {@link Barista} Object.
     */
    public Barista()
    {
        super();
    }

    /**
     * @see de.freese.spring.integration.cafe.xml.Barista#prepareColdDrink(de.freese.spring.integration.cafe.OrderItem)
     */
    @Override
    @ServiceActivator(inputChannel = "coldDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareColdDrink(final OrderItem orderItem)
    {
        return super.prepareColdDrink(orderItem);
    }

    /**
     * @see de.freese.spring.integration.cafe.xml.Barista#prepareHotDrink(de.freese.spring.integration.cafe.OrderItem)
     */
    @Override
    @ServiceActivator(inputChannel = "hotDrinkBarista", outputChannel = "preparedDrinks")
    public Drink prepareHotDrink(final OrderItem orderItem)
    {
        return super.prepareHotDrink(orderItem);
    }
}
