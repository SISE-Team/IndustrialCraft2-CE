/*
 * Vendored minimal replacement for org.ejml.dense.row.factory.DecompositionFactory_DDRM (EJML, Apache 2.0).
 * eig() returns a Jacobi eigenvalue iteration used only for diagnostics when the
 * network matrix cannot be decomposed by the linear solvers.
 */
package org.ejml.dense.row.factory;

import org.ejml.data.Complex_F64;
import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.decomposition.EigenDecomposition_F64;

public final class DecompositionFactory_DDRM {
   private DecompositionFactory_DDRM() {}

   public static EigenDecomposition_F64<DMatrixRMaj> eig(int size, boolean computeVectors) {
      return new JacobiEigen(size);
   }

   private static final class JacobiEigen implements EigenDecomposition_F64<DMatrixRMaj> {
      private final int size;
      private double[] eigenvalues;
      private boolean converged;

      JacobiEigen(int size) {
         this.size = size;
      }

      @Override
      public boolean decompose(DMatrixRMaj A) {
         int n = this.size;
         // Symmetrize a working copy; Jacobi needs a symmetric matrix.
         double[][] a = new double[n][n];
         for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
               a[i][j] = 0.5 * (A.get(i, j) + A.get(j, i));
            }
         }
         for (int sweep = 0; sweep < 50; sweep++) {
            double off = 0.0;
            for (int i = 0; i < n; i++) {
               for (int j = i + 1; j < n; j++) {
                  off += a[i][j] * a[i][j];
               }
            }
            if (off < 1.0E-20) {
               break;
            }
            for (int p = 0; p < n - 1; p++) {
               for (int q = p + 1; q < n; q++) {
                  double apq = a[p][q];
                  if (Math.abs(apq) < 1.0E-15) {
                     continue;
                  }
                  double theta = (a[q][q] - a[p][p]) / (2.0 * apq);
                  double t = Math.signum(theta) / (Math.abs(theta) + Math.sqrt(theta * theta + 1.0));
                  double c = 1.0 / Math.sqrt(t * t + 1.0);
                  double s = t * c;
                  for (int k = 0; k < n; k++) {
                     double akp = a[k][p];
                     double akq = a[k][q];
                     a[k][p] = c * akp - s * akq;
                     a[k][q] = s * akp + c * akq;
                  }
                  for (int k = 0; k < n; k++) {
                     double apk = a[p][k];
                     double aqk = a[q][k];
                     a[p][k] = c * apk - s * aqk;
                     a[q][k] = s * apk + c * aqk;
                  }
               }
            }
         }
         this.eigenvalues = new double[n];
         for (int i = 0; i < n; i++) {
            this.eigenvalues[i] = a[i][i];
         }
         this.converged = true;
         return true;
      }

      @Override
      public Complex_F64 getEigenvalue(int index) {
         if (this.eigenvalues == null) {
            return new Complex_F64(Double.NaN, 0.0);
         }
         return new Complex_F64(this.eigenvalues[index], 0.0);
      }

      @Override
      public boolean inputModified() {
         return false;
      }
   }
}
