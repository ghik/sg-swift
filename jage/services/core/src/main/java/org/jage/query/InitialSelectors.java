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
 * Created: 2011-09-12
 * $Id: InitialSelectors.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.query;

/**
 * Common implementations of {@link IInitialSelector}.
 * 
 * @author AGH AgE Team
 */
public final class InitialSelectors {

	/**
	 * Returns an initial selector that chooses a specified number of elements from the beginning of collection (if it
	 * is order, if it is not a selection is dependent on the iterator implementation).
	 * 
	 * @param number
	 *            A number of elements to choose.
	 * @return A new initial selector.
	 */
	public static IInitialSelector first(final int number) {
		return new IInitialSelector() {

			private int left = 0;

			@Override
			public void initialise(long elementsCount) {
				left = number;
			}

			@Override
			public boolean include() {
				if (left > 0) {
					left--;
					return true;
				}
				return false;
			}

		};
	}

	/**
	 * Returns an initial selector that chooses all elements.
	 * 
	 * @return A new initial selector.
	 */
	public static IInitialSelector all() {
		return new IInitialSelector() {
			@Override
			public void initialise(long elementsCount) {
				// Empty
			}

			@Override
			public boolean include() {
				return true;
			}
		};
	}

	private InitialSelectors() {
		// Empty
	}
}
