package randoop.sequence;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.ExecutionOutcome;
import randoop.NotExecuted;

/**
 * Represents the unfolding execution of a sequence.
 *
 * <p>This is essentially a wrapper around {@code List<ExecutionOutcome>}. Stores information in a
 * list of ExecutionOutcome objects, one for each statement in the sequence.
 */
public final class Execution {

  // The execution outcome of each statement.
  final List<ExecutionOutcome> outcomes;

  private Set<Class<?>> coveredClasses;

  /**
   * Create an Execution to store the execution results of the given sequence. The list of outcomes
   * is initialized to NotExecuted for every statement.
   *
   * @param owner the executed sequence
   */
  public Execution(@Det Sequence owner) {
    this.outcomes = new ArrayList<>(owner.size());
    for (int i = 0; i < owner.size(); i++) {
      outcomes.add(NotExecuted.create());
    }
    this.coveredClasses = new LinkedHashSet<>();
  }

  /**
   * The size of the list.
   *
   * @return the size of the list
   */
  public int size() {
    return outcomes.size();
  }

  /**
   * Get the outcome in the i-th slot.
   *
   * @param i the statement position
   * @return the outcome of the ith statement
   */
  public ExecutionOutcome get(int i) {
    @SuppressWarnings("determinism") // this is @PolyDet("up"), which can't happen because i can't
    // be @OrderNonDet.
    @PolyDet
    ExecutionOutcome result = outcomes.get(i);
    return result;
  }

  void addCoveredClass(@Det Class<?> c) {
    coveredClasses.add(c);
  }

  Set<Class<?>> getCoveredClasses() {
    return coveredClasses;
  }
}
