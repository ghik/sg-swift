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
 * Created: 2011-11-03
 * $Id: BinarySolutionFactory.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.solution.binary;

import java.util.List;

import javax.inject.Inject;

import org.jage.problem.IVectorProblem;
import org.jage.property.ClassPropertyContainer;
import org.jage.random.INormalizedDoubleRandomGenerator;
import org.jage.solution.ISolutionFactory;
import org.jage.solution.IVectorSolution;
import org.jage.solution.VectorSolution;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;

/**
 * Factory for creating IVectorSolution<Boolean> instances.
 *
 * @author AGH AgE Team
 */
public final class BinarySolutionFactory extends ClassPropertyContainer implements ISolutionFactory<IVectorSolution<Boolean>> {

	@Inject
	private INormalizedDoubleRandomGenerator rand;

	@Inject
	private IVectorProblem<Boolean> problem;

	@Override
    public IVectorSolution<Boolean> createEmptySolution() {
		final boolean[] representation = new boolean[problem.getDimension()];
		return new VectorSolution<Boolean>(new FastBooleanArrayList(representation));
    }

	@Override
	public IVectorSolution<Boolean> createInitializedSolution() {
		final boolean[] representation = new boolean[problem.getDimension()];
		for (int i = 0; i < representation.length; i++) {
			representation[i] = rand.nextDouble() > 0.5;
		}
		return new VectorSolution<Boolean>(new FastBooleanArrayList(representation));
	}

	@Override
    public IVectorSolution<Boolean> copySolution(final IVectorSolution<Boolean> solution) {
		final BooleanArrayList representation = (BooleanArrayList)solution.getRepresentation();
		return new VectorSolution<Boolean>(new FastBooleanArrayList(representation));
    }

	/**
	 * Helper class with faster equals and compareTo methods.
	 *
	 * @author AGH AgE Team
	 */
	private static class FastBooleanArrayList extends BooleanArrayList {

        private static final long serialVersionUID = -5009024500774195129L;

		public FastBooleanArrayList(final boolean[] representation) {
	        super(representation);
        }

		public FastBooleanArrayList(final BooleanArrayList representation) {
	        super(representation);
        }

		@Override
		public boolean equals(final Object o) {
		    if(o instanceof BooleanArrayList) {
		    	return super.equals((BooleanArrayList)o);
		    } else {
		    	return super.equals(o);
		    }
		}

		@Override
		public int hashCode() {
		    return super.hashCode();
		}

		@Override
		public int compareTo(final List<? extends Boolean> o) {
			if(o instanceof BooleanArrayList) {
		    	return super.compareTo((BooleanArrayList)o);
		    } else {
		    	return super.compareTo(o);
		    }
		}
	}
}
