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
 * Created: 2012-04-07
 * $Id: AgentActions.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.action;

import org.jage.action.context.AddAgentActionContext;
import org.jage.action.context.GetAgentActionContext;
import org.jage.action.context.KillAgentActionContext;
import org.jage.action.context.MoveAgentActionContext;
import org.jage.action.context.PassToParentActionContext;
import org.jage.action.context.SendMessageActionContext;
import org.jage.address.agent.AgentAddress;
import org.jage.address.selector.agent.ParentAgentAddressSelector;
import org.jage.agent.ISimpleAgent;
import org.jage.communication.message.IMessage;

import static org.jage.address.selector.Selectors.unicastFor;

/**
 * Factory methods for agent actions.
 *
 * @author AGH AgE Team
 */
public class AgentActions {

	/**
	 * Creates an action which adds the second agent to the parent of the first one.
	 *
	 * @param agent
	 *            the agent to which parent the new agent will be added
	 * @param newAgent
	 *            the agent to be added
	 * @return an addToParent action
	 */
	public static SingleAction addToParent(final ISimpleAgent agent, final ISimpleAgent newAgent) {
		AddAgentActionContext context = new AddAgentActionContext(newAgent);
		return new SingleAction(new ParentAgentAddressSelector(agent.getAddress()), context);
	}

	/**
	 * Creates a death action.
	 *
	 * @param agent
	 *            the agent to be killed
	 * @return a death action
	 */
	public static SingleAction death(final ISimpleAgent agent) {
		return new SingleAction(unicastFor(agent.getAddress()), new KillAgentActionContext());
	}

	/**
	 * Creates a migration action.
	 *
	 * @param agent
	 *            the agent to be migrated
	 * @param destination
	 *            the migration destination
	 * @return a migration action
	 */
	public static Action migrate(final ISimpleAgent agent, final AgentAddress destination) {
		MoveAgentActionContext moveContext = new MoveAgentActionContext();
		SingleAction moveAction = new SingleAction(unicastFor(destination), moveContext);
		PassToParentActionContext parentContext = new PassToParentActionContext(agent.getAddress(), moveAction);
		GetAgentActionContext getAgentContext = new GetAgentActionContext(moveContext);

		ComplexAction action = new ComplexAction();
		action.addChild(new SingleAction(unicastFor(agent.getAddress()), getAgentContext));
		action.addChild(new SingleAction(new ParentAgentAddressSelector(agent.getAddress()), parentContext));
		return action;
	}

	/**
	 * Creates a send message action.
	 *
	 * @param message
	 *            the message to be sent
	 * @return a send message action
	 */
	public static SingleAction sendMessage(final IMessage<AgentAddress, ?> message) {
		return new SingleAction(message.getHeader().getReceiverSelector(), new SendMessageActionContext(message));
	}	
}
