package randoop.util;

import org.checkerframework.checker.determinism.qual.Det;

public class RunnerThread extends Thread {

  // Fields assigned when calling setup(..)
  private ReflectionCode code;

  // Fields assigned when calling run()
  boolean runFinished;

  // The state of the thread.
  private NextCallMustBe state;

  private enum NextCallMustBe {
    SETUP,
    RUN
  }

  /**
   * Create a new runner thread.
   *
   * @param threadGroup the group for this thread
   */
  RunnerThread(ThreadGroup threadGroup) {
    super(threadGroup, "randoop.util.RunnerThread");
    this.code = null;
    this.runFinished = false;
    this.state = NextCallMustBe.SETUP;
    this.setUncaughtExceptionHandler(RandoopUncaughtRunnerThreadExceptionHandler.getHandler());
  }

  public void setup(@Det ReflectionCode code) {
    if (state != NextCallMustBe.SETUP) throw new IllegalStateException();
    if (code == null) throw new IllegalArgumentException("code cannot be null.");
    this.code = code;
    this.state = NextCallMustBe.RUN;
  }

  @Override
  public final void run() {
    if (state != NextCallMustBe.RUN) throw new IllegalStateException();
    runFinished = false;
    @SuppressWarnings("determinism") // constructors guarantee all instances of this class are @Det:
    // it's safe to call this method that requires a @Det receiver
    @Det RunnerThread runner = this;
    runner.executeReflectionCode();
    runFinished = true;
    this.state = NextCallMustBe.SETUP;
  }

  private void executeReflectionCode(@Det RunnerThread this)
      throws ReflectionCode.ReflectionCodeException {
    code.runReflectionCode();
  }

  /**
   * Return the ReflectionCode that is being, or was, run.
   *
   * @return the ReflectionCode that is being, or was, run
   */
  public ReflectionCode getCode() {
    return code;
  }
}
