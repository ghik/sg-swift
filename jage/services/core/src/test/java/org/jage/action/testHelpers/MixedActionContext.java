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
 * Created: 2009-01-08
 * $Id: MixedActionContext.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.action.testHelpers;

import org.jage.action.IActionContext;
import org.jage.action.context.AgentActionContext;

/**
 * A context for mixed (strategic + aggregate) actions.
 * 
 * @author AGH AgE Team
 */
@AgentActionContext({ MixedActionContext.STRATEGIC_ACTION_ID, MixedActionContext.AGGREGATE_ACTION_ID,
        MixedActionContext.UNKNOWN_ACTION_ID })
public class MixedActionContext implements IActionContext {

	public final static String STRATEGIC_ACTION_ID = "mixedStrategic";

	public final static String AGGREGATE_ACTION_ID = "mixedAggregate";

	public final static String UNKNOWN_ACTION_ID = "unknown";

	private boolean execStrat = false;

	private boolean execAggr = false;

	private boolean execUnknown = false;

	public boolean getExecAggr() {
		return execAggr;
	}

	public boolean getExecStrat() {
		return execStrat;
	}

	public boolean getExecUnknown() {
		return execUnknown;
	}

	public void setExecAggr(boolean execAggr) {
		this.execAggr = execAggr;
	}

	public void setExecStrat(boolean execStrat) {
		this.execStrat = execStrat;
	}

	public void setExecUnknown(boolean execUnknown) {
		this.execUnknown = execUnknown;
	}
}
