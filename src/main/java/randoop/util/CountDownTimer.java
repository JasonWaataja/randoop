package randoop.util;

import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.NonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;

public class CountDownTimer {

  private long totalTime;
  private @NonDet long startTime;

  private @NonDet CountDownTimer(long timeMillis) {
    this.totalTime = timeMillis;
    this.startTime = System.currentTimeMillis();
  }

  public @NonDet boolean reachedZero() {
    return (elapsedTime() >= totalTime);
  }

  public @NonDet long elapsedTime() {
    return System.currentTimeMillis() - this.startTime;
  }

  public @NonDet long remainingTime() {
    long remainingTime = totalTime - elapsedTime();
    if (remainingTime < 0) {
      return 0;
    }
    return remainingTime;
  }

  public static @NonDet CountDownTimer createAndStart(@Det long totalTimeMillis) {
    return new CountDownTimer(totalTimeMillis);
  }

  @Override
  public @PolyDet("up") String toString() {
    @SuppressWarnings(
        "determinism") // constructors guarantee all instances of this class are @NonDet:
    // the return of this method is always @NonDet, but that's fine because it agrees with
    // @PolyDet("up") when called with a @NonDet receiver, which will always be the case.
    @PolyDet("up") String result = "elapsed: " + elapsedTime() + ", remaining: " + remainingTime();
    return result;
  }
}
