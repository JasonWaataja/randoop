package randoop.generation;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.OrderNonDet;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.operation.TypedOperation;

// TODO: It's weird to call this a "history log" when it is just a summary, printed at the end of
// execution.
/**
 * The working implementation of a {@link OperationHistoryLogInterface} that will collect and print
 * the operation history log to the {@code PrintWriter} given when constructed.
 */
public class OperationHistoryLogger implements OperationHistoryLogInterface {

  /** The {@code PrintWriter} for outputting the operation history as a table. */
  private final PrintWriter writer;

  /** A sparse representation for the operation-outcome table. */
  private final @OrderNonDet Map<TypedOperation, @Det Map<OperationOutcome, Integer>> operationMap;

  /**
   * Creates an {@link OperationHistoryLogger} that will write to the given {@code PrintWriter}.
   *
   * @param writer the {@code PrintWriter} for writing the table from the created operation history
   */
  public OperationHistoryLogger(PrintWriter writer) {
    this.writer = writer;
    this.operationMap = new HashMap<>();
  }

  @Override
  public void add(@Det TypedOperation operation, @Det OperationOutcome outcome) {
    @Det Map<OperationOutcome, Integer> outcomeMap = operationMap.get(operation);
    // TODO-jason: The @Det here should be unnecessary, but the checker thinks it's @NonDet unless I
    // put this here. Why?
    @Det int count = 0;
    if (outcomeMap == null) {
      outcomeMap = new EnumMap<OperationOutcome, Integer>(OperationOutcome.class);
    } else {
      Integer countInteger = outcomeMap.get(outcome);
      if (countInteger != null) {
        count = countInteger;
      }
    }
    count += 1;
    outcomeMap.put(outcome, count);
    operationMap.put(operation, outcomeMap);
  }

  @Override
  public void outputTable(@Det OperationHistoryLogger this) {
    writer.format("%nOperation History:%n");
    int maxNameLength = 0;
    for (TypedOperation operation : operationMap.keySet()) {
      @SuppressWarnings(
          "determinism") // combining elements of @OrderNonDet collection for @Det result
      @Det int nameLength = operation.getSignatureString().length();
      maxNameLength = Math.max(nameLength, maxNameLength);
    }
    @Det Map<OperationOutcome, String> formatMap = printHeader(maxNameLength);
    @OrderNonDet List<TypedOperation> keys = new ArrayList<>(operationMap.keySet());
    Collections.sort(keys);
    for (TypedOperation key : keys) {
      printRow(maxNameLength, formatMap, key, operationMap.get(key));
    }
    writer.flush();
  }

  /**
   * Writes the header for the operation history table, and constructs a map of format strings for
   * the columns of the table matching the length of the outcome names.
   *
   * @param firstColumnLength the width to use for the first column
   * @return a map from {@link OperationOutcome} value to numeric column format for subsequent rows
   */
  private Map<OperationOutcome, String> printHeader(int firstColumnLength) {
    @PolyDet Map<OperationOutcome, String> formatMap =
        new EnumMap<OperationOutcome, String>(OperationOutcome.class);
    writer.format("%-" + firstColumnLength + "s", "Operation");
    for (OperationOutcome outcome : OperationOutcome.values()) {
      writer.format("\t%" + outcome.name().length() + "s", outcome);
      formatMap.put(outcome, "\t%" + outcome.name().length() + "d");
    }
    writer.format("%n");
    return formatMap;
  }

  /**
   * Writes a row for a particular operation consisting of the operation signature and the counts of
   * sequences for each outcome.
   *
   * @param firstColumnLength the width to use for the first column
   * @param formatMap the map of format strings for the counts for each outcome
   * @param operation the operation for the row
   * @param countMap the map of counts for the operation and each outcome
   */
  private void printRow(
      int firstColumnLength,
      Map<OperationOutcome, String> formatMap,
      TypedOperation operation,
      Map<OperationOutcome, Integer> countMap) {
    writer.format("%-" + firstColumnLength + "s", operation.getSignatureString());
    for (OperationOutcome outcome : OperationOutcome.values()) {
      Integer count = countMap.get(outcome);
      if (count == null) {
        count = 0;
      }
      writer.format(formatMap.get(outcome), count);
    }
    writer.format("%n");
  }
}
