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

import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.Splitter;

import de.freese.spring.integration.cafe.Order;
import de.freese.spring.integration.cafe.OrderItem;

/**
 * @author Mark Fisher
 */
@MessageEndpoint
public class OrderSplitter
{
	/**
	 * Erstellt ein neues {@link OrderSplitter} Object.
	 */
	public OrderSplitter()
	{
		super();
	}

	/**
	 * @param order {@link Order}
	 * @return {@link List}
	 */
	@Splitter(inputChannel = "orders", outputChannel = "drinks")
	public List<OrderItem> split(final Order order)
	{
		return order.getItems();
	}
}
