/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe;

/**
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
public class OrderItem
{
    /**
     *
     */
    private boolean iced = false;

    /**
     *
     */
    private final Order order;

    /**
     * 
     */
    private DrinkType type;

    /**
     * Erstellt ein neues {@link OrderItem} Object.
     * 
     * @param order {@link Order}
     * @param type {@link DrinkType}
     * @param iced boolean
     */
    public OrderItem(final Order order, final DrinkType type, final boolean iced)
    {
        super();

        this.order = order;
        this.type = type;
        this.iced = iced;
    }

    /**
     * @return {@link DrinkType}
     */
    public DrinkType getDrinkType()
    {
        return this.type;
    }

    /**
     * @return {@link Order}
     */
    public int getOrderNumber()
    {
        return this.order.getNumber();
    }

    /**
     * @return boolean
     */
    public boolean isIced()
    {
        return this.iced;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return ((this.iced) ? "iced " : "hot ") + this.type;
    }
}
