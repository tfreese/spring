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
 * @author Marius Bogoevici
 */
public class Drink
{
	/**
	 * 
	 */
	private final boolean iced;

	/**
     * 
     */
	private final DrinkType drinkType;

	/**
     * 
     */
	private final int orderNumber;

	/**
	 * Erstellt ein neues {@link Drink} Object.
	 * 
	 * @param orderNumber int
	 * @param drinkType {@link DrinkType}
	 * @param iced boolean
	 */
	public Drink(final int orderNumber, final DrinkType drinkType, final boolean iced)
	{
		super();

		this.orderNumber = orderNumber;
		this.drinkType = drinkType;
		this.iced = iced;
	}

	/**
	 * @return int
	 */
	public int getOrderNumber()
	{
		return this.orderNumber;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return (this.iced ? "Iced" : "Hot") + " " + this.drinkType.toString();
	}
}
