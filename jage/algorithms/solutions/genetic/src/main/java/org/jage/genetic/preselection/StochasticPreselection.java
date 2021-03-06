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
 * Created: 2011-05-12
 * $Id: StochasticPreselection.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.genetic.preselection;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jage.genetic.scaling.IScaling;
import org.jage.random.INormalizedDoubleRandomGenerator;
import org.jage.utils.CumulativeDistribution;

/**
 * Preselect strategy. Stochastic universal sampling implementation. Scales solutions evaluations using a provided {@link IScaling}.
 *
 * @author AGH AgE Team
 */
public final class StochasticPreselection extends AbstractPreselection {

	private static final Logger LOG = LoggerFactory.getLogger(StochasticPreselection.class);

	@Inject
	private IScaling scaling;

	@Inject
	private INormalizedDoubleRandomGenerator rand;

	@Override
	protected int[] getPreselectedIndices(final double[] values) {
		final CumulativeDistribution distribution = new CumulativeDistribution(scaling.scale(values));
		final int n = values.length;
		final int[] indices = new int[n];

		final double initialRandomNumber = rand.nextDouble() / n;
		for (int i = 0; i < n; i++) {
			indices[i] = distribution.getValueFor(initialRandomNumber + i * (1.0 / n));
		}

		LOG.debug("Distribution {} resulted in indices {}", distribution, indices);

		return indices;
	}
}
