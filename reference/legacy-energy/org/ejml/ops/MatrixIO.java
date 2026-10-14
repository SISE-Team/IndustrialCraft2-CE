/*
 * Vendored minimal replacement for org.ejml.ops.MatrixIO (EJML, Apache 2.0).
 */
package org.ejml.ops;

import java.io.PrintStream;
import java.util.Locale;
import org.ejml.data.DMatrixRMaj;

public final class MatrixIO {
   private MatrixIO() {}

   public static void print(PrintStream ps, DMatrixRMaj matrix, String format) {
      for (int i = 0; i < matrix.numRows; i++) {
         StringBuilder sb = new StringBuilder();
         for (int j = 0; j < matrix.numCols; j++) {
            if (j > 0) {
               sb.append(' ');
            }
            sb.append(String.format(Locale.ROOT, format, matrix.get(i, j)));
         }
         ps.println(sb);
      }
   }
}
