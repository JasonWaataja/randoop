package randoop.types;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;

/**
 * Represents {@code void}. Technically, {@code void} is used to indicate that a method has no
 * return values, and is not a type. However, we need to pretend that it is to be able to represent
 * typed operations.
 *
 * <p>The decision to have {@code void} be a separate "type" is counter to the fact that the
 * reflection method {@code Class.isPrimitive()} returns true for {@code void}.
 */
public class VoidType extends Type {

  private static final VoidType value = new VoidType();

  private VoidType() {}

  public static VoidType getVoidType() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof VoidType) && obj == value;
  }

  @Override
  public @NonDet int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    @SuppressWarnings("determinism") // constructors guarantee all instances of this class are @Det:
    // it's safe to call this method that requires a @Det receiver
    @PolyDet String name = this.getName();
    return name;
  }

  @Override
  public Class<?> getRuntimeClass() {
    return void.class;
  }

  @Override
  public String getName(@Det VoidType this) {
    return "void";
  }

  @Override
  public String getSimpleName(@Det VoidType this) {
    return this.getName();
  }

  @Override
  public boolean isVoid() {
    return true;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, since {@code void} is not a subtype of any type
   */
  @Override
  public boolean isSubtypeOf(@Det VoidType this, @Det Type otherType) {
    return false;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Return false because cannot assign to void.
   */
  @Override
  public boolean isAssignableFrom(@Det VoidType this, @Det Type sourceType) {
    return false;
  }
}
