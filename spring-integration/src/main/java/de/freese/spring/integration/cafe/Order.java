/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark Fisher
 * @author Marius Bogoevici
 */
public class Order
{
	/**
	 * 
	 */
	private List<OrderItem> orderItems = new ArrayList<>();

	/**
	 * 
	 */
	private int number;

	/**
	 * Erstellt ein neues {@link Order} Object.
	 * 
	 * @param number int
	 */
	public Order(final int number)
	{
		super();

		this.number = number;
	}

	/**
	 * @param drinkType {@link DrinkType}
	 * @param iced boolean
	 */
	public void addItem(final DrinkType drinkType, final boolean iced)
	{
		this.orderItems.add(new OrderItem(this, drinkType, iced));
	}

	/**
	 * @return {@link List}
	 */
	public List<OrderItem> getItems()
	{
		return this.orderItems;
	}

	/**
	 * @return int
	 */
	public int getNumber()
	{
		return this.number;
	}
}
