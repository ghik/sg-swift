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
 * Created: 2012-02-24
 * $Id: FieldPredicates.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.platform.reflect.predicates;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import com.google.common.base.Predicate;

/**
 * Utility predicates for Constructors.
 *
 * @since 2.6
 * @author AGH AgE Team
 */
public final class FieldPredicates {

	/**
	 * Returns a predicate checking if fields are annotated with a given annotation.
	 *
	 * @param annotation
	 *            the annotation
	 * @return a predicate
	 *
	 * @since 2.6
	 */
	public static Predicate<Field> withAnnotation(final Class<? extends Annotation> annotation) {
		return new Predicate<Field>() {
			@Override
			public boolean apply(final Field input) {
				return input.isAnnotationPresent(annotation);
			}
		};
	}

	private FieldPredicates() {
	}
}
