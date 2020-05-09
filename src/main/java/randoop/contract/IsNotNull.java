package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

import  org.checkerframework.checker.determinism.qual.PolyDet;
import  org.checkerframework.checker.determinism.qual.Det;

/**
 * The contract: {@code x != null}.
 *
 * <p>Obviously, this is not a property that must hold of all objects in a test. Randoop creates an
 * instance of this contract when, during execution of a sequence, it determines that the above
 * property holds. The property thus represents a <i>regression</i> as it captures the behavior of
 * the code when it is executed.
 */
public final class IsNotNull extends ObjectContract {

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    return o instanceof IsNotNull;
  }

  @Override
  public int hashCode() {
    return 31; // no state to compare.
  }

  @Override
  public @PolyDet("up") boolean evaluate(Object... objects) throws Throwable {
    assert objects.length == 1;
    return objects[0] != null;
  }

  @Override
  public int getArity() {
    return 1;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes = new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE));

  @Override
  public @Det TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCodeString(@Det IsNotNull this) {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append(
        "// Regression assertion (captures the current behavior of the code)" + Globals.lineSep);
    b.append("org.junit.Assert.assertNotNull(x0);");
    return b.toString();
  }

  @Override
  public String toCommentString(@Det IsNotNull this) {
    return "x0 != null";
  }

  @Override
  public String get_observer_str() {
    return "IsNotNull";
  }
}
