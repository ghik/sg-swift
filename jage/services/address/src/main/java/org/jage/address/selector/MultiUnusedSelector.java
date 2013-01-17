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
 * Created: 2009-03-11
 * $Id: MultiUnusedSelector.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.address.selector;

import java.util.Collection;

import org.jage.address.Address;

/**
 * A multicast selector that selects a specified number of random addresses from the list of unused addresses.
 * 
 * @param <AddressClass>
 *            type of address which can be selected by this selector
 * 
 * @author AGH AgE Team
 */
public class MultiUnusedSelector<AddressClass extends Address> extends AbstractMulticastSelector<AddressClass> {

	private static final long serialVersionUID = 1844055260030451773L;

	/**
	 * Construct the selector by specifying the number of addresses to select.
	 * 
	 * @param numberOfAddresses
	 *            the number of addresses to be selected
	 */
	public MultiUnusedSelector(int numberOfAddresses) {
		super(numberOfAddresses);
	}

	@Override
	public void initialize(Collection<AddressClass> allAddresses, Collection<AddressClass> usedAddresses) {
		if (allAddresses == null || usedAddresses == null) {
			throw new IllegalArgumentException("Cannot initialize selector with null sets of addresses");
		}

		initialiseFromCollection(SelectorHelper.createUnusedSet(allAddresses, usedAddresses));
	}

}
