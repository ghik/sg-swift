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
 * Created: 2011-12-02
 * $Id: AckleyEvaluator.java 471 2012-10-30 11:17:00Z faber $
 */

package org.jage.evaluation.realvalued;

import org.jage.evaluation.ISolutionEvaluator;
import org.jage.property.ClassPropertyContainer;
import org.jage.solution.IVectorSolution;

import it.unimi.dsi.fastutil.doubles.DoubleList;

/**
 * This class represents a floating-point coded Ackley function. <br />
 * Solution: min=0.0, xi=0, i=1..n <br />
 * http://tracer.lcc.uma.es/problems/ackley/ackley.html <br />
 * <br />
 * The original problem is a minimalization one but it is much convenient to maximize the problem function. So the
 * original function is modified g(x)=-f(x)
 *
 * @author AGH AgE Team
 */
public final class AckleyEvaluator extends ClassPropertyContainer implements
        ISolutionEvaluator<IVectorSolution<Double>, Double> {

	@Override
	public Double evaluate(IVectorSolution<Double> solution) {
		DoubleList representation = (DoubleList)solution.getRepresentation();
		int n = representation.size();

		double sum1 = 0;
		double sum2 = 0;
		for (int i = 0; i < n; i++) {
			double value = representation.getDouble(i);
			sum1 += value * value;
			sum2 += Math.cos(2 * Math.PI * value);
		}

		return 20 * Math.exp(-0.2 * Math.sqrt((1.0 / n) * sum1)) + Math.exp((1.0 / n) * sum2) - 20 - Math.exp(1);
	}
}
