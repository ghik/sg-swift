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
 * $Id: WorkplaceManager.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.workplace.manager;

import java.util.List;

import org.jage.address.agent.AgentAddress;
import org.jage.agent.IAgent;
import org.jage.services.core.CoreComponent;
import org.jage.workplace.Workplace;

/**
 * The core component that manages threaded units of work called "workplaces".<p>
 * 
 * @author AGH AgE Team
 */
public interface WorkplaceManager extends CoreComponent {

	/**
	 * Returns the workplace with the given address.
	 * 
	 * @param address
	 *            Address of the workplace.
	 * @return a workplace or {@code null} if not found
	 */
	Workplace<IAgent> getWorkplace(AgentAddress address);

	/**
	 * Returns all workplaces in this manager.
	 * 
	 * @return the list of workplaces.
	 */
	List<Workplace<IAgent>> getWorkplaces();

}
