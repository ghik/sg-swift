/**
 * Copyright (C) 2006 - 2012
 *   Pawel Kedzior
 *   Tomasz Kmiecik
 *   Kamil Pietak
 *   Krzysztof Sikora
 *   Adam Wos
 *   Lukasz Faber
 *   Daniel Krzywicki
 *   and other students of AGH University of Science and Technology.
 *
 * This file is part of AgE.
 *
 * AgE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AgE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AgE.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * Created: 2009-04-21
 * $Id: AnyUsedSelectorTest.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.address.selector;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import org.jage.address.agent.AgentAddress;

public class AnyUsedSelectorTest extends AbstractAddressSelectorTest {

	private AnyUsedSelector<AgentAddress> selector;

	@Before
	public void setUp() {
		selector = new AnyUsedSelector<AgentAddress>();
		// set deterministic random generator
		selector.random = random;
	}

	@Test
	public void testAddresses() {
		testAddressesOneOf(used);
	}

	@Test
	public void testSelected() {
		testSelectedOneOf(used);
	}

	@Test(expected = IllegalArgumentException.class)
	@SuppressWarnings("unchecked")
	public void testInitializeWithEmptyUsed() {
		selector.initialize(all, Collections.EMPTY_SET);
	}

	@Override
	protected IAddressSelector<AgentAddress> getSelectorUnderTest() {
		return selector;
	}

}
