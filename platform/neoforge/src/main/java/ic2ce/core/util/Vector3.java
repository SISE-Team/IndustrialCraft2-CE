package ic2ce.core.util;

import java.util.Objects;
import net.minecraft.world.phys.Vec3;

/**
 * Checked mutable native geometry adapter: zero normals stay zero and finite large lengths avoid
 * squared overflow.
 */
public final class Vector3 {
  public static final Vector3 UP = new Vector3(0, 1, 0);
  public double x, y, z;

  public Vector3() {}

  public Vector3(double x, double y, double z) {
    set(x, y, z);
  }

  public Vector3(Vector3 vector) {
    this(vector.x, vector.y, vector.z);
  }

  public Vector3(Vec3 vector) {
    this(vector.x, vector.y, vector.z);
  }

  public Vector3 copy() {
    return new Vector3(x, y, z);
  }

  public Vector3 copy(Vector3 destination) {
    return Objects.requireNonNull(destination).set(x, y, z);
  }

  public Vector3 set(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    return this;
  }

  public Vector3 set(Vector3 value) {
    return set(value.x, value.y, value.z);
  }

  public Vector3 set(Vec3 value) {
    return set(value.x, value.y, value.z);
  }

  public Vector3 add(double x, double y, double z) {
    return set(this.x + x, this.y + y, this.z + z);
  }

  public Vector3 add(Vector3 value) {
    return add(value.x, value.y, value.z);
  }

  public Vector3 addScaled(Vector3 value, double scale) {
    return add(value.x * scale, value.y * scale, value.z * scale);
  }

  public Vector3 sub(double x, double y, double z) {
    return add(-x, -y, -z);
  }

  public Vector3 sub(Vector3 value) {
    return sub(value.x, value.y, value.z);
  }

  public Vector3 cross(double x, double y, double z) {
    return set(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
  }

  public Vector3 cross(Vector3 value) {
    return cross(value.x, value.y, value.z);
  }

  public double dot(double x, double y, double z) {
    return this.x * x + this.y * y + this.z * z;
  }

  public double dot(Vector3 value) {
    return dot(value.x, value.y, value.z);
  }

  public double lengthSquared() {
    return x * x + y * y + z * z;
  }

  public double length() {
    return Math.hypot(Math.hypot(x, y), z);
  }

  public Vector3 normalize() {
    double length = length();
    if (length == 0) return this;
    if (!Double.isFinite(length))
      throw new IllegalStateException("Cannot normalize nonfinite geometry");
    return set(x / length, y / length, z / length);
  }

  public Vector3 negate() {
    return set(-x, -y, -z);
  }

  public double distanceSquared(double x, double y, double z) {
    double dx = this.x - x, dy = this.y - y, dz = this.z - z;
    return dx * dx + dy * dy + dz * dz;
  }

  public double distanceSquared(Vector3 value) {
    return distanceSquared(value.x, value.y, value.z);
  }

  public double distanceSquared(Vec3 value) {
    return distanceSquared(value.x, value.y, value.z);
  }

  public double distance(double x, double y, double z) {
    return Math.hypot(Math.hypot(this.x - x, this.y - y), this.z - z);
  }

  public double distance(Vector3 value) {
    return distance(value.x, value.y, value.z);
  }

  public double distance(Vec3 value) {
    return distance(value.x, value.y, value.z);
  }

  public Vector3 scale(double amount) {
    return set(x * amount, y * amount, z * amount);
  }

  public Vector3 scaleTo(double desired) {
    if (!Double.isFinite(desired) || desired < 0)
      throw new IllegalArgumentException("Invalid geometry length");
    double length = length();
    if (length == 0) return this;
    if (!Double.isFinite(length))
      throw new IllegalStateException("Cannot scale nonfinite geometry");
    return set(x / length * desired, y / length * desired, z / length * desired);
  }

  public Vec3 toVec3() {
    return new Vec3(x, y, z);
  }

  @Override
  public String toString() {
    return "[ " + x + ", " + y + ", " + z + " ]";
  }
}
