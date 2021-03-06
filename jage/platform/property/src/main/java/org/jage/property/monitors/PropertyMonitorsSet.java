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
 * Created: 2010-06-23
 * $Id: PropertyMonitorsSet.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.property.monitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jage.property.Property;

/**
 * Set that stores property monitors and their rules.
 *
 * Synchornization on this.
 *
 * @author AGH AgE Team
 */
public class PropertyMonitorsSet implements Iterable<PropertyMonitorRulePair> {

	private ArrayList<PropertyMonitorRulePair> monitors;

	/**
	 * Constructor.
	 */
	public PropertyMonitorsSet() {
		monitors = new ArrayList<PropertyMonitorRulePair>();
	}

	/**
	 * Returns iterator that can be used to iterate through all monitor-rule pairs from the set.
	 *
	 * @return iterator
	 */
	public Iterator<PropertyMonitorRulePair> iterator() {
		return monitors.iterator();
	}

	/**
	 * Adds new monitor with rule to the set.
	 *
	 * @param monitor
	 *            monitor to add.
	 * @param rule
	 *            rule for the monitor.
	 */
	public synchronized void addMonitor(AbstractPropertyMonitor monitor, IPropertyMonitorRule rule) {
		monitors.add(new PropertyMonitorRulePair(monitor, rule));
	}

	/**
	 * Removes monitor from the rule.
	 *
	 * @param monitor
	 *            monitor to remove.
	 */
	public synchronized void removeMonitor(AbstractPropertyMonitor monitor) {
		for (int i = monitors.size() - 1; i >= 0; i--) {
			PropertyMonitorRulePair pair = monitors.get(i);
			if (pair.getPropertyMonitor() == monitor) {
				monitors.remove(i);
			}
		}
	}

	/**
	 * Notifies all monitors about property change. Note, due to threading issues, a monitor can be notified in a short
	 * time after it is removed from this collection.
	 *
	 * @param property
	 *            changed property
	 * @param newValue
	 *            new value of the property
	 * @param forceNotifying
	 *            if this parameter is true, the monitors
	 */
	public void notifyMonitors(Property property, Object newValue, boolean forceNotifying) {
		if (!shouldMonitorsBeNotified(newValue, forceNotifying)) {
			return;
		}

		Collection<PropertyMonitorRulePair> copy;
		synchronized (this) {
			copy = new ArrayList<PropertyMonitorRulePair>(monitors);
		}
		for (PropertyMonitorRulePair pair : copy) {
			Object oldValue = pair.getPropertyMonitor().getOldValue();
			if (pair.getPropertyMonitorRule().isActive(oldValue, newValue)) {
				pair.getPropertyMonitor().propertyChanged(property, newValue);
			}
		}
	}

	/**
	 * Checks if the monitors should be notified about change.
	 *
	 * @param newValue
	 * @param forceNotifying
	 * @return
	 */
	public synchronized boolean shouldMonitorsBeNotified(Object newValue, boolean forceNotifying) {
		if (forceNotifying) {
			return true;
		}
		if (monitors.size() == 0) {
			return false;
		}
		AbstractPropertyMonitor firstMonitor = monitors.get(0).getPropertyMonitor();
		if (firstMonitor.getOldValue() == null) {
			return newValue != null;
		}
		return !firstMonitor.getOldValue().equals(newValue);
	}

	/**
	 * Notifies all monitors about property change, event if the new value is the same as the old one.
	 *
	 * @param property
	 *            monitored property
	 * @param newValue
	 *            new value of the property
	 */
	public void notifyMonitors(Property property, Object newValue) {
		notifyMonitors(property, newValue, true);
	}
}
