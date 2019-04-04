package randoop.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;

/**
 * An abstract class representing kinds of type parameters, which are either type variables or
 * wildcard types. Manages both upper and lower type bounds.
 */
public abstract class ParameterType extends ReferenceType {

  /** The lower bound on this type. */
  private ParameterBound lowerBound;

  /** The upper bound on this type. */
  private ParameterBound upperBound;

  ParameterType() {
    this.lowerBound = new EagerReferenceBound(JavaTypes.NULL_TYPE);
    this.upperBound = new EagerReferenceBound(JavaTypes.OBJECT_TYPE);
  }

  ParameterType(ParameterBound lowerBound, ParameterBound upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof ParameterType)) {
      return false;
    }
    ParameterType other = (ParameterType) object;
    return this.lowerBound.equals(other.lowerBound) && this.upperBound.equals(other.upperBound);
  }

  @Override
  public @NonDet int hashCode() {
    return Objects.hash(lowerBound, upperBound);
  }

  @Override
  public String toString() {
    return "ParameterType [ " + lowerBound + ", " + upperBound + " ]";
  }

  @Override
  public String getCanonicalName(@Det ParameterType this) {
    return this.getName();
  }

  public ParameterBound getLowerTypeBound(@Det ParameterType this) {
    return lowerBound;
  }

  public ParameterBound getUpperTypeBound() {
    return upperBound;
  }

  @Override
  public List<TypeVariable> getTypeParameters(@Det ParameterType this) {
    @Det Set<TypeVariable> parameters = new LinkedHashSet<>();
    parameters.addAll(lowerBound.getTypeParameters());
    parameters.addAll(upperBound.getTypeParameters());
    return new ArrayList<>(parameters);
  }

  /**
   * {@inheritDoc}
   *
   * @return null since type variables do not have a runtime class
   */
  @Override
  public Class<?> getRuntimeClass() {
    return null;
  }

  void setUpperBound(@Det ParameterBound upperBound) {
    this.upperBound = upperBound;
  }

  void setLowerBound(@Det ParameterBound lowerBound) {
    this.lowerBound = lowerBound;
  }

  @Override
  public boolean hasWildcard(@Det ParameterType this) {
    return getLowerTypeBound().hasWildcard() || getUpperTypeBound().hasWildcard();
  }

  public boolean hasGenericBound(@Det ParameterType this) {
    return getUpperTypeBound().isGeneric() || getLowerTypeBound().isGeneric();
  }
}
