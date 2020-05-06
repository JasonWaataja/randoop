package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;

/** The contract: {@code x0.equals(x0)==true}. */
public final class EqualsReflexive extends ObjectContract {
  private static final @Det EqualsReflexive instance = new EqualsReflexive();

  private EqualsReflexive() {}

  public static @Det EqualsReflexive getInstance() {
    return instance;
  }

  @SuppressWarnings("SelfEquals")
  @Override
  public @PolyDet("up") boolean evaluate(Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    assert o != null;
    // noinspection EqualsWithItself
    return o.equals(o);
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
  public String toCommentString() {
    return "x0.equals(x0)";
  }

  @Override
  public String get_observer_str() {
    return "EqualsReflexive";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.equals(x0)");
    b.append(");");
    return b.toString();
  }
}
