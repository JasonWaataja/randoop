package randoop.types;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;

/**
 * The {@code null} type is the type of the value {@code null}. As the subtype of all reference
 * types, it is the default lowerbound of a {@link CaptureTypeVariable}.
 */
class NullReferenceType extends ReferenceType {

  private static final NullReferenceType value = new NullReferenceType();

  private NullReferenceType() {}

  /**
   * Returns the null type.
   *
   * @return the null type object
   */
  static NullReferenceType getNullType() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof NullReferenceType) && obj == value;
  }

  @Override
  public @NonDet int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    @SuppressWarnings("determinism") // toString requires @PolyDet, and only @Det instances will be
    // constructed so this will never introduce nondeterminism.
    @PolyDet
    String name = this.getName();
    return name;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This method returns null since the {@link NullReferenceType} does not have a runtime
   * representation
   */
  @Override
  public Class<?> getRuntimeClass() {
    return null;
  }

  @Override
  public ReferenceType apply(
      @Det NullReferenceType this, @Det Substitution<ReferenceType> substitution) {
    return this;
  }

  @Override
  public String getName(@Det NullReferenceType this) {
    return "NullType";
  }

  @Override
  public String getSimpleName(@Det NullReferenceType this) {
    return this.getName();
  }

  @Override
  public String getCanonicalName(@Det NullReferenceType this) {
    return this.getName();
  }

  /**
   * Indicate whether this type has a wildcard either as or in a type argument.
   *
   * @return true if this type has a wildcard, and false otherwise
   */
  @Override
  public @Det boolean hasWildcard() {
    return false;
  }

  @Override
  public boolean isSubtypeOf(@Det NullReferenceType this, @Det Type otherType) {
    return !otherType.equals(JavaTypes.VOID_TYPE) && otherType.isReferenceType();
  }
}
