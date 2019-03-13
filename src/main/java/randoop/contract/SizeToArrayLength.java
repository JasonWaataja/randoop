package randoop.contract;

import java.util.Arrays;
import java.util.Collection;
import org.checkerframework.checker.determinism.qual.Det;
import randoop.Globals;
import randoop.types.JavaTypes;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** The contract: {@code c.toArray().length == c.size()} for all Collections c. */
public final class SizeToArrayLength extends ObjectContract {
  private static final SizeToArrayLength instance = new SizeToArrayLength();

  private SizeToArrayLength() {}

  public static SizeToArrayLength getInstance() {
    return instance;
  }

  @Override
  public boolean evaluate(@Det Object... objects) {
    assert objects != null && objects.length == 1;
    Object o = objects[0];
    @SuppressWarnings("determinism") // Collection is an invalid type to write because it must have
    // a @Det component type but doesn't apply to the instanceof operator.
    boolean tmp = o instanceof Collection;
    if (tmp) {
      @Det Collection<? extends @Det Object> c = (Collection<? extends @Det Object>) o;
      assert c != null;
      return c.size() == c.toArray().length;
    }
    return true;
  }

  @Override
  public int getArity() {
    return 1;
  }

  @SuppressWarnings("determinism") // Issue with Arrays.asList
  static TypeTuple inputTypes = new TypeTuple(Arrays.<Type>asList(JavaTypes.COLLECTION_TYPE));

  @Override
  public TypeTuple getInputTypes() {
    return inputTypes;
  }

  @Override
  public String toCommentString() {
    return "x0.toArray().length == x0.size()";
  }

  @Override
  public String get_observer_str() {
    return "SizeToArrayLength";
  }

  @Override
  public String toCodeString() {
    StringBuilder b = new StringBuilder();
    b.append(Globals.lineSep);
    b.append("// Checks the contract: ");
    b.append(" " + toCommentString() + Globals.lineSep);
    b.append("org.junit.Assert.assertTrue(");
    b.append("\"Contract failed: " + toCommentString() + "\", ");
    b.append("x0.toArray().length == x0.size()");
    b.append(");");
    return b.toString();
  }
}
