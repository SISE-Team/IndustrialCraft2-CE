/*
 * Vendored minimal replacement for org.ejml.interfaces.linsol.LinearSolverDense (EJML, Apache 2.0).
 */
package org.ejml.interfaces.linsol;

import org.ejml.data.DMatrixRMaj;

public interface LinearSolverDense<T extends DMatrixRMaj> {
   /** Decompose/snapshot A; return false if the matrix cannot be solved with this solver. */
   boolean setA(T A);

   /** Solve A*x=B into X. Must not modify B. */
   void solve(T B, T X);

   boolean modifiesA();

   boolean modifiesB();
}
