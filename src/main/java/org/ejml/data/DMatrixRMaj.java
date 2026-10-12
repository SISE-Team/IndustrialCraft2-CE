/*
 * Vendored minimal replacement for org.ejml.data.DMatrixRMaj (EJML, Apache 2.0).
 * Only the operations used by IC2's energy net (Grid/StructureCache) are implemented.
 */
package org.ejml.data;

public class DMatrixRMaj {
   public int numRows;
   public int numCols;
   public double[] data;

   public DMatrixRMaj(int numRows, int numCols) {
      if (numRows < 0 || numCols < 0) {
         throw new IllegalArgumentException("Negative matrix dimensions");
      }
      this.numRows = numRows;
      this.numCols = numCols;
      this.data = new double[numRows * numCols];
   }

   public DMatrixRMaj(DMatrixRMaj orig) {
      this.numRows = orig.numRows;
      this.numCols = orig.numCols;
      this.data = orig.data.clone();
   }

   public void set(int row, int col, double value) {
      this.data[row * this.numCols + col] = value;
   }

   public double get(int row, int col) {
      return this.data[row * this.numCols + col];
   }

   /** Single-index access, row-major; used for column vectors. */
   public double get(int index) {
      return this.data[index];
   }

   public void zero() {
      java.util.Arrays.fill(this.data, 0.0);
   }

   public DMatrixRMaj copy() {
      return new DMatrixRMaj(this);
   }
}
