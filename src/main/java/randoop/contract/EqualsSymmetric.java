package randoop.contract;

import java.util.Arrays;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.TypeTuple;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;

/** The contract: {@code o1.equals(o2) ==> o2.equals(o1)}. */
public final class EqualsSymmetric extends ObjectContract {
  private static final @Det EqualsSymmetric instance = new EqualsSymmetric();

  private EqualsSymmetric() {}

  public static @Det EqualsSymmetric getInstance() {
    return instance;
  }

  @Override
  public @PolyDet("up") boolean evaluate(Object... objects) {

    Object o1 = objects[0];
    Object o2 = objects[1];

    return !o1.equals(o2) || o2.equals(o1);
  }

  @Override
  public int getArity() {
    return 2;
  }

  /** The arguments to which this contract can be applied. */
  static TypeTuple inputTypes =
      new TypeTuple(Arrays.asList(JavaTypes.OBJECT_TYPE, JavaTypes.OBJECT_TYPE));

  @Override
  public @Det TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "equals-symmetric on x0 and x1.";
  }

  @Override
  public String get_observer_str() {
    return "equals-symmetric";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// This assertion (symmetry of equals) fails ");
    b.append(Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.equals(x1) == x1.equals(x0)");
    b.append(");");
    return b.toString();
  }
}
