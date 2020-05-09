package randoop.reflection;

import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.types.ClassOrInterfaceType;

/**
 * A {@link ClassVisitor} that simply collects {@link ClassOrInterfaceType} objects for visited
 * {@link Class} objects.
 */
public class DeclarationExtractor extends DefaultClassVisitor {
  private final Set<@PolyDet ClassOrInterfaceType> classDeclarationTypes;
  private ReflectionPredicate reflectionPredicate;

  public DeclarationExtractor(
      @PolyDet Set<@PolyDet ClassOrInterfaceType> classDeclarationTypes,
      @PolyDet ReflectionPredicate reflectionPredicate) {
    this.classDeclarationTypes = classDeclarationTypes;
    this.reflectionPredicate = reflectionPredicate;
  }

  @Override
  public void visit(
      @Det DeclarationExtractor this, @Det Class<?> c, @Det ReflectionManager reflectionManager) {
    if (!reflectionPredicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
    reflectionManager.apply(this, c);
  }

  @Override
  public void visitBefore(@Det DeclarationExtractor this, @Det Class<?> c) {
    if (!reflectionPredicate.test(c)) {
      return;
    }
    classDeclarationTypes.add(ClassOrInterfaceType.forClass(c));
  }
}
