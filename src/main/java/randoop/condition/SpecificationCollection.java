package randoop.condition;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.OrderNonDet;
import org.checkerframework.checker.signature.qual.ClassGetName;
import randoop.compile.SequenceClassLoader;
import randoop.compile.SequenceCompiler;
import randoop.condition.specification.OperationSignature;
import randoop.condition.specification.OperationSpecification;
import randoop.reflection.TypeNames;
import randoop.util.MultiMap;

/**
 * A collection of {@link OperationSpecification} objects, indexed by {@link AccessibleObject}
 * reflection objects. Only represents methods that have a specification.
 *
 * <p>The {@link SpecificationCollection} should be constructed from the specification input before
 * the {@link randoop.reflection.OperationModel} is created.
 *
 * <p>This class stores the {@link OperationSpecification} objects, and only constructs the
 * corresponding {@link ExecutableSpecification} on demand. This lazy strategy avoids building
 * condition methods for specifications that are not used.
 */
public class SpecificationCollection {

  /** Map from method or constructor to the corresponding {@link OperationSpecification}. */
  private final Map<AccessibleObject, OperationSpecification> specificationMap;

  /**
   * Given a method signature, what methods (that have specifications) have that signature? Does not
   * contain constructors.
   */
  private final MultiMap<OperationSignature, Method> signatureToMethods;

  /** Map from reflection object to all the methods it overrides (that have a specification). */
  private final @OrderNonDet Map<AccessibleObject, @OrderNonDet Set<Method>> overridden;

  /** Compiler for creating conditionMethods. */
  private final SequenceCompiler compiler;

  /**
   * Creates a {@link SpecificationCollection} for the given specification map.
   *
   * <p>This constructor is used internally. It is only accessible (package-protected) to allow
   * testing. Clients should use {@link #create(List)} instead.
   *
   * @param specificationMap the map from method or constructor to {@link OperationSpecification}
   * @param signatureToMethods the multimap from a signature to methods with with the signature
   * @param overridden the map from a method to methods that it it overrides and that have a
   *     specification
   */
  SpecificationCollection(
      Map<AccessibleObject, OperationSpecification> specificationMap,
      MultiMap<OperationSignature, Method> signatureToMethods,
      @OrderNonDet Map<AccessibleObject, @OrderNonDet Set<Method>> overridden) {
    this.specificationMap = specificationMap;
    this.signatureToMethods = signatureToMethods;
    this.overridden = overridden;
    this.getExecutableSpecificationCache = new HashMap<>();
    SequenceClassLoader sequenceClassLoader = new SequenceClassLoader(getClass().getClassLoader());
    @Det List<String> options = new ArrayList<>();
    this.compiler = new SequenceCompiler(sequenceClassLoader, options);
  }

  /**
   * Creates a {@link SpecificationCollection} from the list of JSON specification files.
   *
   * @param specificationFiles files containing serialized specifications
   * @return the {@link SpecificationCollection} built from the serialized {@link
   *     OperationSpecification} objects, or null if the argument is null
   */
  public static SpecificationCollection create(@Det List<Path> specificationFiles) {
    if (specificationFiles == null) {
      return null;
    }
    MultiMap<OperationSignature, Method> signatureToMethods = new MultiMap<>();
    @Det Map<AccessibleObject, OperationSpecification> specificationMap = new LinkedHashMap<>();
    for (Path specificationFile : specificationFiles) {
      readSpecificationFile(specificationFile, specificationMap, signatureToMethods);
    }
    @OrderNonDet Map<AccessibleObject, @OrderNonDet Set<Method>> overridden =
        buildOverridingMap(signatureToMethods);
    return new SpecificationCollection(specificationMap, signatureToMethods, overridden);
  }

  /**
   * Constructs a map between reflection objects representing override relationships among methods.
   *
   * @param signatureToMethods the map from a {@link OperationSignature} to methods with that
   *     signature
   * @return the map from an {@code AccessibleObject} to methods that it overrides
   */
  private static @OrderNonDet Map<AccessibleObject, @OrderNonDet Set<Method>> buildOverridingMap(
      @Det MultiMap<OperationSignature, Method> signatureToMethods) {
    @OrderNonDet Map<AccessibleObject, @OrderNonDet Set<Method>> overridden = new HashMap<>();
    for (OperationSignature signature : signatureToMethods.keySet()) {
      // This lookup is required because MultiMap does not have an entrySet() method.
      @Det Set<Method> methods = signatureToMethods.getValues(signature);
      for (Method method : methods) {
        Class<?> declaringClass = method.getDeclaringClass();
        @OrderNonDet Set<Method> parents = findOverridden(declaringClass, methods);
        overridden.put(method, parents);
      }
    }
    return overridden;
  }

  /**
   * Finds the methods in {@code methods} that are declared in a supertype of {@code classType}.
   *
   * <p>This should be called only by {@link #buildOverridingMap}.
   *
   * @param classType the class whose supertypes are searched
   * @param methods the set of methods
   * @return the elements of {@code methods} that are declared in a strict supertype of {@code
   *     classType}
   */
  private static @OrderNonDet Set<Method> findOverridden(
      @Det Class<?> classType, @Det Set<Method> methods) {
    @OrderNonDet Set<Method> parents = new HashSet<>();
    for (Method method : methods) {
      Class<?> declaringClass = method.getDeclaringClass();
      if (declaringClass != classType && declaringClass.isAssignableFrom(classType)) {
        parents.add(method);
      }
    }
    return parents;
  }

  /**
   * Get the {@code java.lang.reflect.AccessibleObject} for the {@link OperationSignature}.
   *
   * @param operation the {@link OperationSignature}
   * @return the {@code java.lang.reflect.AccessibleObject} for {@code operation}
   */
  private static AccessibleObject getAccessibleObject(@Det OperationSignature operation) {
    if (operation.isValid()) {
      @Det List<@ClassGetName String> paramTypeNames = operation.getParameterTypeNames();
      @Det Class<?> @Det [] argTypes = new Class<?>[paramTypeNames.size()];
      try {
        for (int i = 0; i < argTypes.length; i++) {
          argTypes[i] = TypeNames.getTypeForName(paramTypeNames.get(i));
        }
        Class<?> declaringClass = TypeNames.getTypeForName(operation.getClassname());
        if (operation.isConstructor()) {
          return declaringClass.getDeclaredConstructor(argTypes);
        } else {
          return declaringClass.getDeclaredMethod(operation.getName(), argTypes);
        }
      } catch (Throwable e) {
        throw new RandoopSpecificationError(
            "Could not load specification operation: " + operation, e);
      }
    }
    return null;
  }

  // Can't store an object of type {@code Type}, because the
  /** The type of {@code List<OperationSpecification>>}. */
  private static TypeToken<List<OperationSpecification>> LIST_OF_OS_TYPE_TOKEN =
      (new TypeToken<List<OperationSpecification>>() {});

  /**
   * Reads {@link OperationSpecification} objects from the given file, and adds them to the other
   * two arguments, which are modified by side effect.
   *
   * @param specificationFile the JSON file of {@link OperationSpecification} objects
   * @param specificationMap side-effected by this method
   * @param signatureToMethods side-effected by this method
   */
  @SuppressWarnings("unchecked")
  private static void readSpecificationFile(
      @Det Path specificationFile,
      @Det Map<AccessibleObject, OperationSpecification> specificationMap,
      @Det MultiMap<OperationSignature, Method> signatureToMethods) {
    if (specificationFile.toString().toLowerCase().endsWith(".zip")) {
      readSpecificationZipFile(specificationFile, specificationMap, signatureToMethods);
      return;
    }

    Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    try (BufferedReader reader = Files.newBufferedReader(specificationFile, UTF_8)) {
      @Det List<OperationSpecification> specificationList =
          gson.fromJson(reader, LIST_OF_OS_TYPE_TOKEN.getType());

      for (@Det OperationSpecification specification : specificationList) {
        OperationSignature operation = specification.getOperation();

        // Check for bad input
        if (operation == null) {
          throw new Error("operation is null for specification " + specification);
        }
        String duplicateName = specification.getIdentifiers().duplicateName();
        if (duplicateName != null) {
          throw new RandoopSpecificationError(
              "Duplicate name \"" + duplicateName + "\" in specification: " + specification);
        }

        AccessibleObject accessibleObject = getAccessibleObject(operation);
        specificationMap.put(accessibleObject, specification);
        if (accessibleObject instanceof Method) {
          OperationSignature signature = OperationSignature.of(accessibleObject);
          signatureToMethods.add(signature, (Method) accessibleObject);
        }
      }
    } catch (IOException e) {
      throw new RandoopSpecificationError(
          "Unable to read specification file " + specificationFile, e);
    } catch (RandoopSpecificationError e) {
      e.setFile(specificationFile);
      throw e;
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RandoopSpecificationError("Bad specification file " + specificationFile, e);
    }
  }

  /**
   * Reads {@link OperationSpecification} objects from all the subfiles of the given zip file, and
   * adds them to the other two arguments, which are modified by side effect.
   *
   * @param specificationZipFile a zip file containing files that contain {@link
   *     OperationSpecification} objects
   * @param specificationMap side-effected by this method
   * @param signatureToMethods side-effected by this method
   */
  private static void readSpecificationZipFile(
      Path specificationZipFile,
      final @Det Map<AccessibleObject, OperationSpecification> specificationMap,
      final @Det MultiMap<OperationSignature, Method> signatureToMethods) {
    @Det Map<String, ? extends @Det Object> myEmptyMap = Collections.emptyMap();
    FileSystem zipFS;
    try {
      URI uri = URI.create("jar:" + specificationZipFile.toUri().toString());
      zipFS = FileSystems.newFileSystem(uri, myEmptyMap);
      for (Path root : zipFS.getRootDirectories()) {
        Files.walkFileTree(
            root,
            new SimpleFileVisitor<Path>() {
              @Override
              public FileVisitResult visitFile(@Det Path file, BasicFileAttributes attrs)
                  throws IOException {
                // You can do anything you want with the path here
                readSpecificationFile(file, specificationMap, signatureToMethods);
                return FileVisitResult.CONTINUE;
              }

              @Override
              public FileVisitResult preVisitDirectory(@Det Path dir, BasicFileAttributes attrs)
                  throws IOException {
                if (dir.endsWith("__MACOSX")) {
                  return FileVisitResult.SKIP_SUBTREE;
                }
                return super.preVisitDirectory(dir, attrs);
              }
            });
      }
    } catch (IOException e) {
      throw new RandoopSpecificationError(
          "Unable to read specification file " + specificationZipFile, e);
    }
  }

  /** Cache for {@link #getExecutableSpecification}. */
  private @OrderNonDet Map<AccessibleObject, ExecutableSpecification>
      getExecutableSpecificationCache;

  /**
   * Creates an {@link ExecutableSpecification} object for the given constructor or method, from its
   * specifications in this object.
   *
   * <p>The translation makes the following conversions:
   *
   * <ul>
   *   <li>{@link randoop.condition.specification.Precondition} to {@link
   *       randoop.condition.ExecutableBooleanExpression}
   *   <li>{@link randoop.condition.specification.Postcondition} to {@link
   *       randoop.condition.GuardPropertyPair}
   *   <li>{@link randoop.condition.specification.ThrowsCondition} to {@link
   *       randoop.condition.GuardThrowsPair}
   * </ul>
   *
   * @param executable the reflection object for a constructor or method
   * @return the {@link ExecutableSpecification} for the specifications of the given method or
   *     constructor
   */
  public ExecutableSpecification getExecutableSpecification(
      @Det SpecificationCollection this, @Det Executable executable) {

    // Check if executable already has an ExecutableSpecification object
    ExecutableSpecification execSpec = getExecutableSpecificationCache.get(executable);
    if (execSpec != null) {
      return execSpec;
    }

    // Otherwise, build a new one.
    OperationSpecification specification = specificationMap.get(executable);
    if (specification == null) {
      execSpec = new ExecutableSpecification();
    } else {
      execSpec =
          SpecificationTranslator.createExecutableSpecification(
              executable, specification, compiler);
    }

    if (executable instanceof Method) {
      Method method = (Method) executable;
      @OrderNonDet Set<Method> parents = overridden.get(executable);
      // Parents is null in some tests.  Is it ever null other than that?
      if (parents == null) {
        @Det Set<Method> sigSet = signatureToMethods.getValues(OperationSignature.of(method));
        if (sigSet != null) {
          // Todo: why isn't this added to the `parents` map?
          parents = findOverridden(method.getDeclaringClass(), sigSet);
        }
      }
      if (parents == null) {
        throw new Error("parents = null (test #2) for " + executable);
      }
      if (parents != null) {
        for (Method parent : parents) {
          @SuppressWarnings(
              "determinism") // iterating over @OrderNonDet collection to modify another:
          // addParent essentially just adds parentExecSpec to an @OrderNonDet List.
          @Det Method tmp = parent;
          ExecutableSpecification parentExecSpec = getExecutableSpecification(tmp);
          execSpec.addParent(parentExecSpec);
        }
      }
    }

    getExecutableSpecificationCache.put(executable, execSpec);
    return execSpec;
  }
}
