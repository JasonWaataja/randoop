package randoop.types;

import java.util.Objects;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;

/** Represents a type variable that is a type parameter. (See JLS, section 4.3) */
class ExplicitTypeVariable extends TypeVariable {

  /** the type parameter */
  private final java.lang.reflect.TypeVariable<?> variable;

  /**
   * Create a {@code ExplicitTypeVariable} for the given type parameter.
   *
   * @param variable the type parameter
   * @param bound the upper bound on the parameter
   */
  ExplicitTypeVariable(java.lang.reflect.@Det TypeVariable<?> variable, @Det ParameterBound bound) {
    super(new EagerReferenceBound(JavaTypes.NULL_TYPE), bound);
    this.variable = variable;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Checks that the type parameter is equal. This may be more restrictive than desired because
   * equivalent TypeVariable objects from different instances of the same type may be distinct.
   *
   * @return true if the type parameters are equal, false otherwise
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ExplicitTypeVariable)) {
      return isAssignableFrom(null);
    }
    ExplicitTypeVariable t = (ExplicitTypeVariable) obj;
    return variable.equals(t.variable) && super.equals(t);
  }

  @Override
  public @NonDet int hashCode() {
    return Objects.hash(variable, super.hashCode());
  }

  @Override
  public String toString() {
    return variable.toString();
  }

  @Override
  public String getName(@Det ExplicitTypeVariable this) {
    return variable.getName();
  }

  @Override
  public String getSimpleName(@Det ExplicitTypeVariable this) {
    return this.getName();
  }

  java.lang.reflect.TypeVariable<?> getReflectionTypeVariable() {
    return this.variable;
  }

  @Override
  public boolean isGeneric(@Det ExplicitTypeVariable this) {
    return true;
  }

  @Override
  public ReferenceType apply(
      @Det ExplicitTypeVariable this, @Det Substitution<ReferenceType> substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null && !type.isVariable()) {
      return type;
    }
    ParameterBound upperBound = getUpperTypeBound().apply(substitution);
    if (type == null) {
      if (!upperBound.equals(getUpperTypeBound())) {
        return new ExplicitTypeVariable(this.variable, upperBound);
      }
      return this;
    }
    if (!upperBound.equals(getUpperTypeBound())) {
      return ((TypeVariable) type).createCopyWithBounds(getLowerTypeBound(), upperBound);
    }
    return type;
  }

  @Override
  public TypeVariable createCopyWithBounds(
      ParameterBound lowerBound, @Det ParameterBound upperBound) {
    return new ExplicitTypeVariable(this.variable, upperBound);
  }
}
