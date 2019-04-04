package randoop.types;

import java.util.List;
import org.checkerframework.checker.determinism.qual.Det;

/**
 * Represents a bound on a type variable where the bound is a {@link ReferenceType} that can be used
 * directly. Contrast with {@link LazyReferenceBound}.
 */
class EagerReferenceBound extends ReferenceBound {

  /**
   * Creates a bound for the given reference type.
   *
   * @param boundType the reference boundType
   */
  EagerReferenceBound(ReferenceType boundType) {
    super(boundType);
  }

  @Override
  public EagerReferenceBound apply(
      @Det EagerReferenceBound this, @Det Substitution<ReferenceType> substitution) {
    ReferenceType referenceType = getBoundType().apply(substitution);
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return new EagerReferenceBound(referenceType);
  }

  @Override
  public EagerReferenceBound applyCaptureConversion(@Det EagerReferenceBound this) {
    ReferenceType referenceType = getBoundType().applyCaptureConversion();
    if (referenceType.equals(getBoundType())) {
      return this;
    }
    return new EagerReferenceBound(referenceType);
  }

  @Override
  public List<TypeVariable> getTypeParameters(@Det EagerReferenceBound this) {
    return getBoundType().getTypeParameters();
  }

  @Override
  public boolean isLowerBound(
      @Det EagerReferenceBound this, @Det Type argType, @Det Substitution<ReferenceType> subst) {
    ReferenceType boundType = this.getBoundType().apply(subst);
    if (boundType.equals(JavaTypes.NULL_TYPE)) {
      return true;
    }
    if (boundType.isVariable()) {
      return ((TypeVariable) boundType).getLowerTypeBound().isLowerBound(argType, subst);
    }
    if (argType.isParameterized()) {
      if (!(boundType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType argClassType = (InstantiatedType) argType.applyCaptureConversion();
      InstantiatedType boundSuperType =
          ((ClassOrInterfaceType) boundType)
              .getMatchingSupertype(argClassType.getGenericClassType());
      if (boundSuperType == null) {
        return false;
      }
      boundSuperType = boundSuperType.applyCaptureConversion();
      return boundSuperType.isInstantiationOf(argClassType);
    }
    return boundType.isSubtypeOf(argType);
  }

  @Override
  boolean isLowerBound(
      @Det EagerReferenceBound this,
      @Det ParameterBound bound,
      @Det Substitution<ReferenceType> substitution) {
    assert bound instanceof EagerReferenceBound : "only handling reference bounds";
    return isLowerBound(((EagerReferenceBound) bound).getBoundType(), substitution);
  }

  @Override
  public boolean isSubtypeOf(@Det EagerReferenceBound this, @Det ParameterBound bound) {
    if (bound instanceof EagerReferenceBound) {
      return this.getBoundType().isSubtypeOf(((EagerReferenceBound) bound).getBoundType());
    }
    assert false : "not handling EagerReferenceBound subtype of other bound type";
    return false;
  }

  @Override
  public boolean isUpperBound(
      @Det EagerReferenceBound this, @Det Type argType, @Det Substitution<ReferenceType> subst) {
    ReferenceType boundType = this.getBoundType().apply(subst);
    if (boundType.equals(JavaTypes.OBJECT_TYPE)) {
      return true;
    }
    if (boundType.isVariable()) {
      return ((TypeVariable) boundType).getUpperTypeBound().isUpperBound(argType, subst);
    }
    if (boundType.isParameterized()) {
      if (!(argType instanceof ClassOrInterfaceType)) {
        return false;
      }
      InstantiatedType boundClassType = (InstantiatedType) boundType.applyCaptureConversion();
      InstantiatedType argSuperType =
          ((ClassOrInterfaceType) argType)
              .getMatchingSupertype(boundClassType.getGenericClassType());
      if (argSuperType == null) {
        return false;
      }
      argSuperType = argSuperType.applyCaptureConversion();
      return argSuperType.isInstantiationOf(boundClassType);
    }
    return argType.isSubtypeOf(boundType);
  }

  @Override
  boolean isUpperBound(
      @Det EagerReferenceBound this,
      @Det ParameterBound bound,
      @Det Substitution<ReferenceType> substitution) {
    return isUpperBound(getBoundType(), substitution);
  }
}
