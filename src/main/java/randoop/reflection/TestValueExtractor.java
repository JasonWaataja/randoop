package randoop.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.TestValue;
import randoop.generation.SeedSequences;
import randoop.main.GenInputsAbstract;
import randoop.sequence.Sequence;

/**
 * {@code TestValueExtractor} is a {@link ClassVisitor} that inspects the fields passed to it to see
 * if they are annotated with {@link randoop.TestValue}, are static, and have type that is
 * primitive, String, or an array of primitive or String.
 */
public class TestValueExtractor extends DefaultClassVisitor {

  private final Set<Sequence> valueSequences;

  public TestValueExtractor(@Det Set<Sequence> valueSequences) {
    this.valueSequences = valueSequences;
  }

  /**
   * {@inheritDoc}
   *
   * <p>If the field is static and is annotated with {@code randoop.TestValue}, adds the value of
   * the field to the sequence set constructed by this visitor. Requires that the field type be
   * primitive, String, or an array of primitive or String type.
   */
  @Override
  public void visit(@Det TestValueExtractor this, @Det Field f) {
    if (f.getAnnotation(TestValue.class) != null) {
      if (!Modifier.isStatic(f.getModifiers())) {
        String msg =
            "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
                + f.getName()
                + " in class "
                + f.getDeclaringClass()
                + " to be declared static but it was not.";
        throw new RuntimeException(msg);
      }

      valueSequences.addAll(SeedSequences.objectsToSeeds(getValue(f)));
    }
  }

  /**
   * Returns the value stored in the given (static) field with primitive, String or array of
   * primitive or String type.
   *
   * @param f the field
   * @return the value(s) in the field
   */
  private List<Object> getValue(@Det Field f) {

    @PolyDet List<Object> valueList = new ArrayList<>();

    Class<?> fieldType = f.getType();
    if (fieldType.isPrimitive()
        || fieldType.equals(String.class)
        || (fieldType.isArray()
            && (fieldType.getComponentType().isPrimitive()
                || fieldType.getComponentType().equals(String.class)))) {

      if (GenInputsAbstract.progressdisplay) {
        printDetectedAnnotatedFieldMsg(f);
      }
      f.setAccessible(true);
      Object value;
      try {
        value = f.get(null);
      } catch (IllegalAccessException e) {
        String msg =
            "RANDOOP ANNOTATION ERROR: IllegalAccessException when processing @TestValue-annotated field "
                + f.getName()
                + " in class "
                + f.getDeclaringClass()
                + ". (Is the class declaring this field publicly-visible?)";
        throw new RuntimeException(msg);
      }

      if (!fieldType.isArray()) {
        valueList.add(value);
      } else {
        int length = Array.getLength(value);
        for (int i = 0; i < length; i++) {
          valueList.add(Array.get(value, i));
        }
      }
    } else {
      String msg =
          "RANDOOP ANNOTATION ERROR: Expected @TestValue-annotated field "
              + f.getName()
              + " in class "
              + f.getDeclaringClass()
              + " to declare a primitive type, String, or an array of primitives of Strings, "
              + "but the field's type is "
              + f.getType()
              + ".";
      throw new RuntimeException(msg);
    }
    return valueList;
  }

  /**
   * Prints an informational message that an annotated field has been found.
   *
   * @param f the field
   */
  private static void printDetectedAnnotatedFieldMsg(@Det Field f) {
    String msg =
        "ANNOTATION: Detected @TestValue-annotated field "
            + f.getType().getCanonicalName()
            + " \""
            + f.getName()
            + "\" in class "
            + f.getDeclaringClass().getCanonicalName()
            + ". Will collect its primitive values to use in generation.";
    System.out.println(msg);
  }
}
