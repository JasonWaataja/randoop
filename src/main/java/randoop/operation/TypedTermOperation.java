package randoop.operation;

import java.util.List;
import randoop.sequence.Variable;
import randoop.types.Substitution;
import randoop.types.Type;
import randoop.types.TypeTuple;

import org.checkerframework.checker.determinism.qual.Det;

/**
 * Represents operations that have no declaring class, such as literal value, cast, or array
 * creation/access/assignment.
 */
class TypedTermOperation extends TypedOperation {

  /**
   * Creates a {@link TypedOperation} for a given operation and input and output types.
   *
   * @param operation the operation
   * @param inputTypes the input types
   * @param outputType the output type
   */
  TypedTermOperation(CallableOperation operation, TypeTuple inputTypes, Type outputType) {
    super(operation, inputTypes, outputType);
  }

  @Override
  public boolean hasWildcardTypes() {
    return false;
  }

  @Override
  public void appendCode(List<Variable> inputVars, StringBuilder b) {
    this.getOperation().appendCode(null, getInputTypes(), getOutputType(), inputVars, b);
  }

  @Override
  public @Det TypedTermOperation substitute(@Det TypedTermOperation this, @Det Substitution substitution) {
    @Det TypeTuple inputTypes = this.getInputTypes().substitute(substitution);
    Type outputType = this.getOutputType().substitute(substitution);
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public @Det TypedOperation applyCaptureConversion(@Det TypedTermOperation this) {
    @Det TypeTuple inputTypes = this.getInputTypes().applyCaptureConversion();
    Type outputType = this.getOutputType().applyCaptureConversion();
    return new TypedTermOperation(this.getOperation(), inputTypes, outputType);
  }

  @Override
  public String toParsableString() {
    return this.getOperation().toParsableString(null, getInputTypes(), getOutputType());
  }
}
