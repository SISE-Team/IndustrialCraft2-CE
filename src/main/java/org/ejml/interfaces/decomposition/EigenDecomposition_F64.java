/*
 * Vendored minimal replacement for org.ejml.interfaces.decomposition.EigenDecomposition_F64 (EJML, Apache 2.0).
 * Only the symmetric diagnostic path used by IC2 (Jacobi iteration) is implemented.
 */
package org.ejml.interfaces.decomposition;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;

public interface EigenDecomposition_F64<T extends DMatrixRMaj> {
   boolean decompose(T A);

   Complex_F64 getEigenvalue(int index);

   boolean inputModified();
}
