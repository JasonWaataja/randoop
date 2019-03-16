package randoop.reflection;

import java.util.ArrayList;
import java.util.Collection;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.operation.NonreceiverTerm;
import randoop.operation.TypedOperation;
import randoop.sequence.Sequence;
import randoop.sequence.Variable;
import randoop.types.ClassOrInterfaceType;
import randoop.util.ClassFileConstants;
import randoop.util.MultiMap;

/**
 * {@code ClassLiteralExtractor} is a {@link ClassVisitor} that extracts literals from the bytecode
 * of each class visited, adding a sequence for each to a map associating a sequence with a type.
 *
 * @see OperationModel
 */
class ClassLiteralExtractor extends DefaultClassVisitor {

  private MultiMap<ClassOrInterfaceType, Sequence> literalMap;

  ClassLiteralExtractor(@Det MultiMap<ClassOrInterfaceType, Sequence> literalMap) {
    this.literalMap = literalMap;
  }

  @Override
  public void visitBefore(Class<?> c) {
    @PolyDet Collection<ClassFileConstants.ConstantSet> constList = new ArrayList<>();
    constList.add(ClassFileConstants.getConstants(c.getName()));
    MultiMap<Class<?>, NonreceiverTerm> constantMap = ClassFileConstants.toMap(constList);
    for (Class<?> constantClass : constantMap.keySet()) {
      ClassOrInterfaceType constantType = ClassOrInterfaceType.forClass(constantClass);
      for (NonreceiverTerm term : constantMap.getValues(constantClass)) {
        Sequence seq =
            new Sequence()
                .extend(
                    TypedOperation.createNonreceiverInitialization(term),
                    new ArrayList<Variable>());
        literalMap.add(constantType, seq);
      }
    }
  }
}
