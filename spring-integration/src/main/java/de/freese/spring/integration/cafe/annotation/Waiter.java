/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version
 * 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe.annotation;

import java.util.List;

import org.springframework.integration.annotation.Aggregator;
import org.springframework.integration.annotation.CorrelationStrategy;
import org.springframework.integration.annotation.MessageEndpoint;

import de.freese.spring.integration.cafe.Delivery;
import de.freese.spring.integration.cafe.Drink;

/**
 * @author Marius Bogoevici
 */
@MessageEndpoint
public class Waiter extends de.freese.spring.integration.cafe.xml.Waiter
{
	/**
	 * Erstellt ein neues {@link Waiter} Object.
	 */
	public Waiter()
	{
		super();
	}

	/**
	 * @param drink {@link Drink}
	 * @return int
	 */
	@CorrelationStrategy
	public int correlateByOrderNumber(final Drink drink)
	{
		return drink.getOrderNumber();
	}

	/**
	 * @see de.freese.spring.integration.cafe.xml.Waiter#prepareDelivery(java.util.List)
	 */
	@Override
	@Aggregator(inputChannel = "preparedDrinks", outputChannel = "deliveries")
	public Delivery prepareDelivery(final List<Drink> drinks)
	{
		return super.prepareDelivery(drinks);
	}
}
