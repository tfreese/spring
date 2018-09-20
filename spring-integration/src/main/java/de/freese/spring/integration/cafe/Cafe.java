/*
 * Copyright 2002-2010 the original author or authors. Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed
 * to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing permissions and limitations under the License.
 */

package de.freese.spring.integration.cafe;

import org.springframework.messaging.Message;

/**
 * The entry point for Cafe Demo. The demo's main() method invokes the '<code>placeOrder</code>' method on a generated MessagingGateway
 * proxy. The gateway then passes the {@link Order} as the payload of a {@link Message} to the configured <em>requestChannel</em>. The
 * channel ('orders') is defined in the 'cafeDemo.xml' file.
 *
 * @author Mark Fisher
 */
public interface Cafe
{
    /**
     * @param order {@link Order}
     */
    void placeOrder(Order order);
}
