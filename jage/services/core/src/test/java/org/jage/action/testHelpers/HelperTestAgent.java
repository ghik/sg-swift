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
 * Created: 2008-10-07
 * $Id: HelperTestAgent.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.action.testHelpers;

import org.junit.Ignore;

import static org.mockito.Mockito.mock;

import org.jage.action.Action;
import org.jage.address.agent.AgentAddress;
import org.jage.address.agent.DefaultAgentAddress;
import org.jage.address.node.NodeAddress;
import org.jage.agent.AgentException;
import org.jage.agent.SimpleAgent;

/**
 * A sample, helper agent implementation that does nothing.
 *
 * @author AGH AgE Team
 */
@Ignore
public class HelperTestAgent extends SimpleAgent {

	private static final long serialVersionUID = 1374519285747616223L;

	public static final AgentAddress ADDRESS = new DefaultAgentAddress(mock(NodeAddress.class));

	public HelperTestAgent() {
		super(ADDRESS);
	}

	public HelperTestAgent(final AgentAddress newAddress) {
		super(newAddress);
	}

	@Override
	public void step() {
		// Empty
	}

	@Override
	public boolean finish() {
		return true;
	}

	@Override
	public void init() {
		// Empty
	}

	public void runAction(final Action action) throws AgentException {
		doAction(action);
	}
}
