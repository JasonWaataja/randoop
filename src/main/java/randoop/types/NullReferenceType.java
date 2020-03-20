package randoop.types;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;

/**
 * The {@code null} type is the type of the value {@code null}. As the subtype of all reference
 * types, it is the default lowerbound of a {@link CaptureTypeVariable}.
 */
class NullReferenceType extends ReferenceType {

  private static final @Det NullReferenceType value = new NullReferenceType();

  private NullReferenceType() {}

  /**
   * Returns the null type.
   *
   * @return the null type object
   */
  static @Det NullReferenceType getNullType() {
    return value;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NullReferenceType)) {
      return false;
    }
    return obj == value;
  }

  @Override
  public @NonDet int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return this.getName();
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
  public ReferenceType substitute(@Det NullReferenceType this, @Det Substitution substitution) {
    return this;
  }

  @Override
  public String getName() {
    return "NullType";
  }

  @Override
  public String getSimpleName() {
    return this.getName();
  }

  @Override
  public String getCanonicalName() {
    return this.getName();
  }

  @Override
  public boolean hasWildcard() {
    return false;
  }

  @Override
  public boolean isSubtypeOf(@Det NullReferenceType this, @Det Type otherType) {
    return !otherType.equals(JavaTypes.VOID_TYPE) && otherType.isReferenceType();
  }
}
