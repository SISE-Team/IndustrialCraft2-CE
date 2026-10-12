/*
 * Vendored minimal replacement for org.ejml.dense.row.factory.LinearSolverFactory_DDRM (EJML, Apache 2.0).
 * symmPosDef: Cholesky solver. linear: LU solver with partial pivoting.
 * Both solvers snapshot A internally but report modifiesA() == true to match
 * upstream EJML behavior that IC2's Grid expects.
 */
package org.ejml.dense.row.factory;

import org.ejml.data.DMatrixRMaj;
import org.ejml.interfaces.linsol.LinearSolverDense;

public final class LinearSolverFactory_DDRM {
   private LinearSolverFactory_DDRM() {}

   public static LinearSolverDense<DMatrixRMaj> symmPosDef(int size) {
      return new CholeskySolver();
   }

   public static LinearSolverDense<DMatrixRMaj> linear(int size) {
      return new LuSolver();
   }

   private static final class CholeskySolver implements LinearSolverDense<DMatrixRMaj> {
      private double[][] L;

      @Override
      public boolean setA(DMatrixRMaj A) {
         int n = A.numRows;
         if (A.numCols != n) {
            return false;
         }
         double[][] L = new double[n][n];
         for (int i = 0; i < n; i++) {
            for (int j = 0; j <= i; j++) {
               double sum = A.get(i, j);
               for (int k = 0; k < j; k++) {
                  sum -= L[i][k] * L[j][k];
               }
               if (i == j) {
                  if (!(sum > 0.0) || Double.isNaN(sum) || Double.isInfinite(sum)) {
                     this.L = null;
                     return false;
                  }
                  L[i][j] = Math.sqrt(sum);
               } else {
                  L[i][j] = sum / L[j][j];
               }
            }
         }
         this.L = L;
         return true;
      }

      @Override
      public void solve(DMatrixRMaj B, DMatrixRMaj X) {
         int n = this.L.length;
         // forward substitution: L y = B
         for (int r = 0; r < n; r++) {
            double v = B.get(r, 0);
            for (int k = 0; k < r; k++) {
               v -= this.L[r][k] * X.data[k];
            }
            X.data[r] = v / this.L[r][r];
         }
         // back substitution: L^T x = y
         for (int r = n - 1; r >= 0; r--) {
            double v = X.data[r];
            for (int k = r + 1; k < n; k++) {
               v -= this.L[k][r] * X.data[k];
            }
            X.data[r] = v / this.L[r][r];
         }
      }

      @Override
      public boolean modifiesA() {
         return true;
      }

      @Override
      public boolean modifiesB() {
         return false;
      }
   }

   private static final class LuSolver implements LinearSolverDense<DMatrixRMaj> {
      private double[][] LU;
      private int[] piv;

      @Override
      public boolean setA(DMatrixRMaj A) {
         int n = A.numRows;
         if (A.numCols != n) {
            return false;
         }
         double[][] LU = new double[n][n];
         int[] piv = new int[n];
         for (int i = 0; i < n; i++) {
            piv[i] = i;
            for (int j = 0; j < n; j++) {
               LU[i][j] = A.get(i, j);
            }
         }
         for (int col = 0; col < n; col++) {
            // pivot selection
            int maxRow = col;
            double maxVal = Math.abs(LU[col][col]);
            for (int r = col + 1; r < n; r++) {
               double v = Math.abs(LU[r][col]);
               if (v > maxVal) {
                  maxVal = v;
                  maxRow = r;
               }
            }
            if (maxVal == 0.0 || Double.isNaN(maxVal)) {
               this.LU = null;
               return false;
            }
            if (maxRow != col) {
               double[] tmp = LU[col];
               LU[col] = LU[maxRow];
               LU[maxRow] = tmp;
               int t = piv[col];
               piv[col] = piv[maxRow];
               piv[maxRow] = t;
            }
            double pivot = LU[col][col];
            for (int r = col + 1; r < n; r++) {
               double f = LU[r][col] / pivot;
               LU[r][col] = f;
               for (int c = col + 1; c < n; c++) {
                  LU[r][c] -= f * LU[col][c];
               }
            }
         }
         this.LU = LU;
         this.piv = piv;
         return true;
      }

      @Override
      public void solve(DMatrixRMaj B, DMatrixRMaj X) {
         int n = this.LU.length;
         double[] y = new double[n];
         for (int i = 0; i < n; i++) {
            y[i] = B.get(this.piv[i], 0);
         }
         // forward substitution with unit-diagonal L
         for (int r = 0; r < n; r++) {
            for (int k = 0; k < r; k++) {
               y[r] -= this.LU[r][k] * y[k];
            }
         }
         // back substitution with U
         for (int r = n - 1; r >= 0; r--) {
            for (int k = r + 1; k < n; k++) {
               y[r] -= this.LU[r][k] * y[k];
            }
            y[r] /= this.LU[r][r];
         }
         for (int i = 0; i < n; i++) {
            X.data[i] = y[i];
         }
      }

      @Override
      public boolean modifiesA() {
         return true;
      }

      @Override
      public boolean modifiesB() {
         return false;
      }
   }
}
