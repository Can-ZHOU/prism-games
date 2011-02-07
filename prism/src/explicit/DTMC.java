//==============================================================================
//	
//	Copyright (c) 2002-
//	Authors:
//	* Dave Parker <david.parker@comlab.ox.ac.uk> (University of Oxford)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package explicit;

import java.util.*;
import java.util.Map.Entry;

/**
 * Interface for classes that provide (read-only) access to an explicit-state DTMC.
 */
public interface DTMC extends Model
{
	/**
	 * Get the number of transitions from state s.
	 */
	public double getNumTransitions(int s);

	/**
	 * Get an iterator over the transitions from state s.
	 */
	public Iterator<Entry<Integer,Double>> getTransitionsIterator(int s);

	/**
	 * Get the transition reward (if any) for the transitions in state s.
	 */
	public double getTransitionReward(int s);

	/**
	 * Do a matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. for all s: result[s] = sum_j P(s,j)*vect[j]
	 * @param vect Vector to multiply by
	 * @param result Vector to store result in
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 */
	public void mvMult(double vect[], double result[], BitSet subset, boolean complement);

	/**
	 * Do a single row of matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. return sum_j P(s,j)*vect[j]
	 * @param s Row index
	 * @param vect Vector to multiply by
	 */
	public double mvMultSingle(int s, double vect[]);

	/**
	 * Do a Gauss-Seidel-style matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in,
	 * storing new values directly in {@code vect} as computed.
	 * i.e. for all s: vect[s] = (sum_{j!=s} P(s,j)*vect[j]) / (1-P(s,s))
	 * The maximum (absolute/relative) difference between old/new
	 * elements of {@code vect} is also returned.
	 * @param vect Vector to multiply by (and store the result in)
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 * @param absolute If true, compute absolute, rather than relative, difference
	 * @return The maximum difference between old/new elements of {@code vect}
	 */
	public double mvMultGS(double vect[], BitSet subset, boolean complement, boolean absolute);

	/**
	 * Do a single row of Jacobi-style matrix-vector multiplication for
	 * the DTMC's transition probability matrix P and the vector {@code vect} passed in.
	 * i.e. return (sum_{j!=s} P(s,j)*vect[j]) / (1-P(s,s))
	 * @param s Row index
	 * @param vect Vector to multiply by
	 */
	public double mvMultJacSingle(int s, double vect[]);

	/**
	 * Do a matrix-vector multiplication and sum of action reward.
	 * @param vect Vector to multiply by
	 * @param result Vector to store result in
	 * @param subset Only do multiplication for these rows (ignored if null)
	 * @param complement If true, {@code subset} is taken to be its complement (ignored if {@code subset} is null)
	 */
	public void mvMultRew(double vect[], double result[], BitSet subset, boolean complement);

	/**
	 * Do a single row of matrix-vector multiplication and sum of action reward.
	 * @param s Row index
	 * @param vect Vector to multiply by
	 */
	public double mvMultRewSingle(int s, double vect[]);

}
