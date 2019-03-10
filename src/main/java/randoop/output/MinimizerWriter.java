package randoop.output;

import java.io.IOException;
import java.nio.file.Path;
import org.checkerframework.checker.determinism.qual.Det;
import randoop.main.Minimize;

/**
 * A {@link CodeWriter} that, for an error-revealing test class, writes both the original and
 * minimized class. Minimizes the methods of the test class using {@link Minimize#mainMinimize(Path,
 * String, int, boolean)}.
 */
public class MinimizerWriter implements CodeWriter {

  /** The {@link JavaFileWriter} used to write classes. */
  private final JavaFileWriter javaFileWriter;

  /**
   * Creates a {@link MinimizerWriter} using the given {@link JavaFileWriter}.
   *
   * @param javaFileWriter the {@link JavaFileWriter} for writing the classes
   */
  public MinimizerWriter(@Det JavaFileWriter javaFileWriter) {
    this.javaFileWriter = javaFileWriter;
  }

  /**
   * {@inheritDoc}
   *
   * <p>Writes both the original class and the minimized class. Returns the original class.
   */
  @Override
  public @Det Path writeClassCode(
      @Det String packageName, @Det String classname, @Det String classCode)
      throws RandoopOutputException {

    // Write the original class.
    Path testFile = javaFileWriter.writeClassCode(packageName, classname, classCode);

    // Minimize the error-revealing test that has been output.
    try {
      Minimize.mainMinimize(
          testFile, Minimize.suiteclasspath, Minimize.testsuitetimeout, Minimize.verboseminimizer);
    } catch (IOException e) {
      throw new RandoopOutputException(e);
    }

    return testFile;
  }

  @Override
  public @Det Path writeUnmodifiedClassCode(
      @Det String packageName, @Det String classname, @Det String classCode)
      throws RandoopOutputException {
    return javaFileWriter.writeClassCode(packageName, classname, classCode);
  }
}
