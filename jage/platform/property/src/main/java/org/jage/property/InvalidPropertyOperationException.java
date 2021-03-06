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
 * Exception thrown when invalid property operation has been invoked (for example, setting
 * value of read-only property or attaching monitor to property that is not monitorable).
 * @author Tomek
 *
 */
public class InvalidPropertyOperationException extends PropertyException {

	public static final long serialVersionUID = 1;

	/**
	 * Constructor.
	 * @param message message for the exception.
	 */
	public InvalidPropertyOperationException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message message for the exception.
	 * @param throwable inner exception.
	 */
	public InvalidPropertyOperationException(final String message, final Throwable throwable) {
		super(message, throwable);
	}

	/**
	 * Constructor.
	 * @param throwable inner exception.
	 */
	public InvalidPropertyOperationException(final Throwable throwable) {
		super(throwable);
	}
}


