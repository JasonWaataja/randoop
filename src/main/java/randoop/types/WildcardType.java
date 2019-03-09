package randoop.types;

import java.lang.reflect.TypeVariable;
import java.util.HashSet;
import java.util.Objects;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;

/**
 * Represents a wildcard type, which occurs as a type argument to a parameterized type.
 *
 * <p>A wildcard may have either an upper or lower bound as defined in <a
 * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section
 * 4.5.1</a>.
 *
 * <pre>
 *   ? [ extends ReferenceType ]
 *   ? [ super ReferenceType ]
 * </pre>
 *
 * @see WildcardArgument
 */
class WildcardType extends ParameterType {

  private final boolean hasUpperBound;

  private WildcardType() {
    //  do not create object
    this.hasUpperBound = false;
  }

  WildcardType(@Det ParameterBound bound, @Det boolean hasUpperBound) {
    super();
    this.hasUpperBound = hasUpperBound;
    if (hasUpperBound) {
      this.setUpperBound(bound);
    } else {
      this.setLowerBound(bound);
    }
  }

  /**
   * Creates a wildcard type from a given reflection type. Assumes that the bounds array has a
   * single element as defined in <a
   * href="https://docs.oracle.com/javase/specs/jls/se8/html/jls-4.html#jls-4.5.1">JLS Section
   * 4.5.1</a>.
   *
   * @param type the {@code java.lang.reflect.WildcardType} object
   * @return a {@link WildcardType} with the bounds from the given reflection type
   */
  public static WildcardType forType(java.lang.reflect.@Det WildcardType type) {
    // Note: every wildcard has an upper bound, so need to check lower first
    if (type.getLowerBounds().length > 0) {
      assert type.getLowerBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardType(
          ParameterBound.forTypes(new HashSet<TypeVariable<?>>(), type.getLowerBounds()), false);
    }
    if (type.getUpperBounds().length > 0) {
      assert type.getUpperBounds().length == 1
          : "a wildcard is defined by the JLS to only have one bound";
      return new WildcardType(
          ParameterBound.forTypes(new HashSet<TypeVariable<?>>(), type.getUpperBounds()), true);
    }
    throw new IllegalArgumentException("A wildcard must have either upper or lower bounds");
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof WildcardType)) {
      return false;
    }
    WildcardType otherType = (WildcardType) obj;
    return otherType.hasUpperBound == this.hasUpperBound && super.equals(otherType);
  }

  @Override
  public @NonDet int hashCode() {
    return Objects.hash(hasUpperBound, super.hashCode());
  }

  @Override
  public String toString() {
    if (hasUpperBound) {
      if (this.getUpperTypeBound().isObject()) {
        return "?";
      }
      @SuppressWarnings("determinism") // toString returns @PolyDet("up"), but the Object we call it
      // on can't be @OrderNonDet so it's actually @PolyDet.
      @PolyDet
      String result = "? extends " + this.getUpperTypeBound().toString();
      return result;
    }
    @SuppressWarnings("determinism") // toString returns @PolyDet("up"), but the Object we call it
    // on can't be @OrderNonDet so it's actually @PolyDet.
    @PolyDet
    String result = "? super " + this.getLowerTypeBound().toString();
    return result;
  }

  @Override
  public String getName(@Det WildcardType this) {
    return toString();
  }

  @Override
  public String getSimpleName(@Det WildcardType this) {
    return toString();
  }

  ParameterBound getTypeBound(@Det WildcardType this) {
    if (hasUpperBound) {
      return getUpperTypeBound();
    }
    return getLowerTypeBound();
  }

  @Override
  public WildcardType apply(@Det WildcardType this, @Det Substitution<ReferenceType> substitution) {
    ParameterBound bound = getTypeBound().apply(substitution);
    if (bound.equals(this.getTypeBound())) {
      return this;
    }
    return new WildcardType(bound, hasUpperBound);
  }

  @Override
  public WildcardType applyCaptureConversion(@Det WildcardType this) {
    if (getTypeBound().hasWildcard()) {
      EagerReferenceBound convertedType =
          (EagerReferenceBound) getTypeBound().applyCaptureConversion();
      return new WildcardType(convertedType, this.hasUpperBound);
    }
    return this;
  }

  /**
   * If this type has an upper bound, checks for containment cases:
   *
   * <ul>
   *   <li>{@code ? extends T} contains {@code ? extends S} if {@code T.isSubtypeOf(S)}
   *   <li>{@code ? extends T} contains {@code ?}
   * </ul>
   *
   * Both of which are technically the same because from a reflection perspective {@code ?} is just
   * {@code ? extends Object}. Otherwise, checks for the cases
   *
   * <ul>
   *   <li>{@code ? super T} contains {@code ? super S} if {@code S.isSubtypeOf(T)}
   *   <li>{@code ? super T} contains {@code ?}
   *   <li>{@code ? super T} contains {@code ? extends Object}
   * </ul>
   *
   * The last two being equivalent from the perspective of reflection.
   *
   * @param otherType the type to check for
   * @return true if this type contains the other type, false otherwise
   */
  public boolean contains(@Det WildcardType this, @Det WildcardType otherType) {
    if (this.hasUpperBound) {
      if (otherType.hasUpperBound
          && this.getUpperTypeBound().isSubtypeOf(otherType.getUpperTypeBound())) {
        return true;
      }
    } else {
      if (otherType.hasUpperBound) {
        return otherType.getUpperTypeBound().equals(new EagerReferenceBound(JavaTypes.OBJECT_TYPE));
      } else {
        return otherType.getLowerTypeBound().isSubtypeOf(this.getLowerTypeBound());
      }
    }
    return false;
  }

  @Override
  public boolean isGeneric(@Det WildcardType this) {
    return getTypeBound().isGeneric();
  }

  boolean hasUpperBound() {
    return hasUpperBound;
  }
}
