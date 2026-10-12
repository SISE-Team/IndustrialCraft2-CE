/*
 * Vendored minimal replacement for org.ejml.data.Complex_F64 (EJML, Apache 2.0).
 */
package org.ejml.data;

public class Complex_F64 {
   public double real;
   public double imaginary;

   public Complex_F64(double real, double imaginary) {
      this.real = real;
      this.imaginary = imaginary;
   }

   public boolean isReal() {
      return this.imaginary == 0.0;
   }

   @Override
   public String toString() {
      return this.isReal() ? String.valueOf(this.real) : this.real + (this.imaginary < 0.0 ? "" : "+") + this.imaginary + "i";
   }
}
