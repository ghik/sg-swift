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
 * Created: 2011-04-07
 * $Id: DelegationSimpleAggregate.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.examples.delegation;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jage.address.agent.AgentAddress;
import org.jage.address.agent.AgentAddressSupplier;
import org.jage.agent.IAgent;
import org.jage.agent.IAgentEnvironment;
import org.jage.agent.SimpleAggregate;
import org.jage.platform.component.exception.ComponentException;

/**
 * This aggregate presents an example delegation of strategies. It basically enforces its children to use a strategy
 * that it has configured.
 *
 * @author AGH AgE Team
 */

public class DelegationSimpleAggregate extends SimpleAggregate {

	private static final long serialVersionUID = 2L;

	private static final Logger log = LoggerFactory.getLogger(DelegationSimpleAggregate.class);

	@Inject
	private IEchoStrategy echoStrategy;

	private String childStrategy;

	public void setEchoStrategy(final IEchoStrategy echoStrategy) {
		this.echoStrategy = echoStrategy;
	}

	public void setChildStrategy(final String childStrategy) {
		this.childStrategy = childStrategy;
	}

	public DelegationSimpleAggregate(final AgentAddress address) {
		super(address);
	}

	@Inject
	public DelegationSimpleAggregate(final AgentAddressSupplier supplier) {
		super(supplier);
	}

	/**
	 * Initialises this aggregate. It also sets an <em>echo strategy</em> for all its children agents that are instances
	 * of the {@link IEchoStrategyAcceptingAgent} class.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see org.jage.agent.SimpleAggregate#init()
	 */

	@Override
	public void setAgentEnvironment(final IAgentEnvironment localEnvironment) {
		super.setAgentEnvironment(localEnvironment);

		try {
			getAgentsLock().readLock().lockInterruptibly();
			try {
				for (final IAgent agent : agents.values()) {
					log.info("Agent: {}", agent.getClass());
					if (agent instanceof IEchoStrategyAcceptingAgent) {
						((IEchoStrategyAcceptingAgent)agent).acceptEchoStrategy(childStrategy);
					}
				}
			} finally {
				getAgentsLock().readLock().unlock();
			}
		} catch (final InterruptedException e) {
			log.error("Interrupted in run", e);
		}
	}

	/**
	 * Executes a step of the aggregate. It uses an <em>echo strategy</em> and then delegates execution to the parent
	 * class.
	 * <p>
	 * {@inheritDoc}
	 *
	 * @see org.jage.agent.SimpleAggregate#step()
	 */
	@Override
	public void step() {
		log.info("{} says Hello World from {}", getAddress(), getParentAddress());

		echoStrategy.echo(getParentAddress().toString());

		super.step();

		try {
			Thread.sleep(200);
		} catch (final InterruptedException e) {
			log.error("Interrupted", e);
		}
	}

	@Override
	public boolean finish() throws ComponentException {
		log.info("Finishing Delegation Simple Aggregate: {}", getAddress());

		return super.finish();
	}
}
