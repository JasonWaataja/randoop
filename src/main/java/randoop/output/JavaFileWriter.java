package randoop.output;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;

/**
 * A {@link CodeWriter} that writes JUnit4 test class source text to a {@code .java} file with
 * annotations so that tests are executed in ascending alphabetical order by test method name.
 */
public class JavaFileWriter implements CodeWriter {

  /** The directory to which JUnit files are written. */
  private final String dirName;

  /**
   * JavaFileWriter creates an instance of class holding information needed to write a test suite.
   *
   * @param junitDirName directory where files are to be written
   */
  public JavaFileWriter(@Det String junitDirName) {
    this.dirName = junitDirName;
  }

  /**
   * writeClassCode writes a code sequence as a JUnit4 test class to a .java file. Tests are
   * executed in ascending alphabetical order by test method name.
   *
   * @param packageName the package name for the class
   * @param className the name of the class
   * @param classCode the source text of the test class
   * @return the Path object for generated java file
   */
  @Override
  public Path writeClassCode(
      @Det JavaFileWriter this,
      @Det String packageName,
      @Det String className,
      @Det String classCode)
      throws RandoopOutputException {
    Path dir = createOutputDir(packageName);
    Path file = new java.io.File(dir.toFile(), className + ".java").toPath();

    try (PrintWriter out = new PrintWriter(file.toFile(), UTF_8.name())) {
      out.println(classCode);
    } catch (IOException e) {
      String message = "Exception creating print writer for file " + file.toString();
      throw new RandoopOutputException(message, e);
    }

    return file;
  }

  @Override
  public Path writeUnmodifiedClassCode(
      @Det JavaFileWriter this,
      @Det String packageName,
      @Det String classname,
      @Det String classCode)
      throws RandoopOutputException {
    return writeClassCode(packageName, classname, classCode);
  }

  /**
   * Create the output directory for the package if it does not already exist.
   *
   * @param packageName the package name
   * @return the {@code Path} for the created directory
   * @throws RandoopOutputException if the directory for the package could not be created
   */
  private Path createOutputDir(String packageName) throws RandoopOutputException {
    Path dir = getDir(packageName);
    if (!Files.exists(dir)) {
      boolean success = dir.toFile().mkdirs();
      if (!success) {
        throw new RandoopOutputException("Unable to create directory: " + dir.toAbsolutePath());
      }
    }
    return dir;
  }

  /**
   * Returns the directory for the package name relative to the directory name of this writer.
   *
   * @param packageName the package name
   * @return the {@code Path} for the directory corresponding to the package name
   */
  private Path getDir(String packageName) {
    @PolyDet Path dir;
    if (dirName == null || dirName.length() == 0) {
      @SuppressWarnings("determinism") // the user home directory will be the same through runs on
      // the same machine
      @Det
      String tmp = System.getProperty("user.dir");
      dir = Paths.get(tmp);
    } else {
      dir = Paths.get(dirName);
    }
    if (packageName == null) {
      return dir;
    }

    if (packageName.length() == 0) {
      return dir;
    }
    String[] split = packageName.split("\\.");
    for (String s : split) {
      dir = new java.io.File(dir.toFile(), s).toPath();
    }
    return dir;
  }
}
