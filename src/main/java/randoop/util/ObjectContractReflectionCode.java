package randoop.util;

import java.util.Arrays;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import randoop.contract.ObjectContract;

public final class ObjectContractReflectionCode extends ReflectionCode {

  final ObjectContract c;
  final Object[] objs;

  public ObjectContractReflectionCode(final ObjectContract c, final Object... objs) {
    this.c = c;
    this.objs = objs;
  }

  @Override
  protected void runReflectionCodeRaw(@Det ObjectContractReflectionCode this) {
    try {
      retval = c.evaluate(objs);
    } catch (Throwable e) {
      exceptionThrown = e;
    }
  }

  @Override
  public @NonDet String toString() {
    @SuppressWarnings("determinism") // constructors guarantee all instances of this class are @Det:
    // it's safe to call this method that requires a @Det receiver
    String status = status();
    return "Check of ObjectContract " + c + " args: " + Arrays.toString(objs) + status;
  }
}
