package randoop.operation;

import java.lang.reflect.Array;
import java.util.List;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.ExceptionalExecution;
import randoop.ExecutionOutcome;
import randoop.NormalExecution;
import randoop.sequence.Variable;
import randoop.types.Type;
import randoop.types.TypeTuple;

/** Created by bjkeller on 8/19/16. */
class ArrayElementSet extends CallableOperation {

  private final int ARRAY = 0;
  private final int INDEX = 1;
  private final int VALUE = 2;

  private final Type elementType;

  ArrayElementSet(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public @Det ExecutionOutcome execute(@Det ArrayElementSet this, @Det Object @Det [] input) {
    assert input.length == 3
        : "array element assignment must have array, index and value as arguments";
    Object array = input[ARRAY];
    int index = (int) input[INDEX];
    Object value = input[VALUE];

    try {
      Array.set(array, index, value);
    } catch (Throwable thrown) {
      return new ExceptionalExecution(thrown, 0);
    }
    return new NormalExecution(null, 0);
  }

  @Override
  public void appendCode(
      @Det ArrayElementSet this,
      @Det Type declaringType,
      @Det TypeTuple inputTypes,
      @Det Type outputType,
      @Det List<@Det Variable> inputVars,
      @Det StringBuilder b) {

    b.append(inputVars.get(ARRAY).getName()).append("[");
    @Det Variable indexVariable = inputVars.get(INDEX);
    String index = getArgumentString(indexVariable);
    b.append(index).append("]").append(" = ");
    String value = getArgumentString(inputVars.get(VALUE));
    b.append(value);
  }

  @Override
  public String toParsableString(Type declaringType, TypeTuple inputTypes, Type outputType) {
    return getName();
  }

  @Override
  public String getName() {
    return "<set>" + elementType + "[]";
  }
}
