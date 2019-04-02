package randoop.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.OrderNonDet;

/** An abstract class representing type variables. */
public abstract class TypeVariable extends ParameterType {

  /**
   * Creates a type variable with {@link NullReferenceType} as the lower bound, and the {@code
   * Object} type as upper bound.
   */
  TypeVariable() {
    super();
  }

  /**
   * Creates a type variable with the given type bounds. Assumes the bounds are consistent and does
   * not check for the subtype relationship.
   *
   * @param lowerBound the lower type bound on this variable
   * @param upperBound the upper type bound on this variable
   */
  TypeVariable(@Det ParameterBound lowerBound, @Det ParameterBound upperBound) {
    super(lowerBound, upperBound);
  }

  /**
   * Creates a {@code TypeVariable} object for a given {@code java.lang.reflect.Type} reference,
   * which must be a {@code java.lang.reflect.TypeVariable}.
   *
   * @param type the type reference
   * @return the {@code TypeVariable} for the given type
   */
  public static TypeVariable forType(java.lang.reflect.@Det Type type) {
    if (!(type instanceof java.lang.reflect.TypeVariable<?>)) {
      throw new IllegalArgumentException("type must be a type variable, got " + type);
    }
    java.lang.reflect.TypeVariable<?> v = (java.lang.reflect.TypeVariable) type;
    @OrderNonDet Set<java.lang.reflect.TypeVariable<?>> variableSet = new HashSet<>();
    variableSet.add(v);
    return new ExplicitTypeVariable(v, ParameterBound.forTypes(variableSet, v.getBounds()));
  }

  @Override
  public ReferenceType apply(
      @Det TypeVariable this, @Det Substitution<ReferenceType> substitution) {
    ReferenceType type = substitution.get(this);
    if (type != null) {
      return type;
    }
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Returns false, since an uninstantiated type variable may not be assigned to.
   */
  @Override
  public @Det boolean isAssignableFrom(Type sourceType) {
    return false;
  }

  @Override
  public boolean isInstantiationOf(@Det TypeVariable this, @Det ReferenceType otherType) {
    if (super.isInstantiationOf(otherType)) {
      return true;
    }

    if (otherType.isVariable()) {
      TypeVariable variable = (TypeVariable) otherType;
      Substitution<ReferenceType> substitution = getSubstitution(variable, this);
      boolean lowerbound =
          variable.getLowerTypeBound().isLowerBound(getLowerTypeBound(), substitution);
      boolean upperbound =
          variable.getUpperTypeBound().isUpperBound(getUpperTypeBound(), substitution);
      return lowerbound && upperbound;
    }
    return false;
  }

  @Override
  public boolean isSubtypeOf(@Det TypeVariable this, @Det Type otherType) {
    if (super.isSubtypeOf(otherType)) {
      return true;
    }
    if (otherType.isReferenceType()) {
      Substitution<ReferenceType> substitution = getSubstitution(this, (ReferenceType) otherType);
      return this.getUpperTypeBound().isLowerBound(otherType, substitution);
    }
    return false;
  }

  /**
   * Creates a substitution of the given {@link ReferenceType} for the {@link TypeVariable}.
   *
   * @param variable the variable
   * @param otherType the replacement type
   * @return a substitution that replaces {@code variable} with {@code otherType}
   */
  private static Substitution<ReferenceType> getSubstitution(
      @Det TypeVariable variable, @Det ReferenceType otherType) {
    @Det List<TypeVariable> variableList = Collections.singletonList(variable);
    return Substitution.forArgs(variableList, otherType);
  }

  @Override
  public boolean isVariable() {
    return true;
  }

  /**
   * Indicates whether this {@link TypeVariable} can be instantiated by the {@link ReferenceType}.
   * Does not require that all bounds of this variable be instantiated.
   *
   * @param otherType the possibly instantiating type, not a variable
   * @return true if the given type can instantiate this variable, false otherwise
   */
  boolean canBeInstantiatedBy(@Det TypeVariable this, @Det ReferenceType otherType) {
    Substitution<ReferenceType> substitution;
    if (getLowerTypeBound().isVariable()) {
      substitution = getSubstitution(this, otherType);
      ParameterBound boundType = getLowerTypeBound().apply(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
        return false;
      }
    } else {
      substitution = getSubstitution(this, otherType);
      if (!getLowerTypeBound().isLowerBound(otherType, substitution)) {
        return false;
      }
    }
    if (getUpperTypeBound().isVariable()) {
      substitution = getSubstitution(this, otherType);
      ParameterBound boundType = getUpperTypeBound().apply(substitution);
      TypeVariable checkType = (TypeVariable) ((ReferenceBound) boundType).getBoundType();
      if (!checkType.canBeInstantiatedBy(otherType)) {
        return false;
      }
    } else {
      substitution = getSubstitution(this, otherType);
      if (!getUpperTypeBound().isUpperBound(otherType, substitution)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns the type parameters in this type, which is this variable.
   *
   * @return this variable
   */
  @Override
  public @Det List<TypeVariable> getTypeParameters(@Det TypeVariable this) {
    @Det Set<TypeVariable> parameters = new LinkedHashSet<>(super.getTypeParameters());
    parameters.add(this);
    return new ArrayList<>(parameters);
  }

  public abstract TypeVariable createCopyWithBounds(
      @Det ParameterBound lowerBound, @Det ParameterBound upperBound);

  @Override
  public Type getRawtype() {
    return JavaTypes.OBJECT_TYPE;
  }
}
