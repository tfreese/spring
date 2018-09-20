/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe;

import java.util.List;

/**
 * @author Marius Bogoevici
 */
public class Delivery
{
	/**
	 * 
	 */
	private final List<Drink> deliveredDrinks;

	/**
     * 
     */
	private final int orderNumber;

	/**
	 * Erstellt ein neues {@link Delivery} Object.
	 * 
	 * @param deliveredDrinks {@link List}
	 */
	public Delivery(final List<Drink> deliveredDrinks)
	{
		super();

		assert (deliveredDrinks.size() > 0);
		this.deliveredDrinks = deliveredDrinks;
		this.orderNumber = deliveredDrinks.get(0).getOrderNumber();
	}

	/**
	 * @return {@link List}
	 */
	public List<Drink> getDeliveredDrinks()
	{
		return this.deliveredDrinks;
	}

	/**
	 * @return int
	 */
	public int getOrderNumber()
	{
		return this.orderNumber;
	}
}
