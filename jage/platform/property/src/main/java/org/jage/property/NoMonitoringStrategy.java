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
package org.jage.property;

/**
 * Strategy that doesn't monitor changes in property value's internal state. It is 
 * used by Property class.
 * @author Tomek
 */
public class NoMonitoringStrategy extends PropertyValueMonitoringStrategy {

	/**
	 * Constructor.
	 * @param property property that uses this strategy.
	 */
	public NoMonitoringStrategy(Property property) {
		super(property);
	}
	
	/**
	 * Informs the strategy that property value has been changed.
	 * @param oldValue old property value.
	 * @param newValue new property value.
	 */
	@Override
	public void propertyValueChanged(Object newValue) {		
	}
}
