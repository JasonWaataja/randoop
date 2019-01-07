package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.checkerframework.checker.determinism.qual.Det;
import org.checkerframework.checker.determinism.qual.PolyDet;
import randoop.main.RandoopBug;

/**
 * Given a list of lists, defines methods that can access all the elements as if they were part of a
 * single list, without actually merging the lists.
 *
 * <p>This class is used for performance reasons. We want the ability to select elements collected
 * across several lists, but we observed that creating a brand new list (i.e. via a sequence of
 * List.addAll(..) operations can be very expensive, because it happened in a hot spot (method
 * SequenceCollection.getSequencesThatYield).
 */
public class ListOfLists<T extends @Det Object> implements SimpleList<T>, Serializable {

  private static final long serialVersionUID = -3307714585442970263L;

  public final List<SimpleList<T>> lists;

  /** The i-th value is the number of elements in the sublists up to the i-th one, inclusive. */
  private int[] cumulativeSize;

  private int totalelements;

  @SuppressWarnings({"varargs", "unchecked"}) // heap pollution warning
  public ListOfLists(@Det SimpleList<T>... lists) {
    @SuppressWarnings("determinism") // can't declare lists @Det but its order is decided at compile
    // time anyway
    @Det
    int len = lists.length;
    this.lists = new ArrayList<>(len);
    for (SimpleList<T> sl : lists) {
      this.lists.add(sl);
    }
    this.cumulativeSize = new int[lists.length];
    this.totalelements = 0;
    for (int i = 0; i < len; i++) {
      SimpleList<T> l = lists[i];
      if (l == null) {
        throw new IllegalArgumentException("All lists should be non-null");
      }
      this.totalelements += l.size();
      this.cumulativeSize[i] = this.totalelements;
    }
  }

  public ListOfLists(@Det List<SimpleList<T>> lists) {
    if (lists == null) throw new IllegalArgumentException("param cannot be null");
    this.lists = lists;
    this.cumulativeSize = new int[lists.size()];
    this.totalelements = 0;
    for (int i = 0; i < lists.size(); i++) {
      SimpleList<T> l = lists.get(i);
      if (l == null) {
        throw new IllegalArgumentException("All lists should be non-null");
      }
      this.totalelements += l.size();
      this.cumulativeSize[i] = this.totalelements;
    }
  }

  @Override
  public int size() {
    return this.totalelements;
  }

  @Override
  public boolean isEmpty() {
    return this.totalelements == 0;
  }

  @Override
  public T get(@Det int index) {
    if (index < 0 || index > this.totalelements - 1) {
      throw new IllegalArgumentException("index must be between 0 and size()-1");
    }
    int previousListSize = 0;
    for (int i = 0; i < this.cumulativeSize.length; i++) {
      if (index < this.cumulativeSize[i]) {
        return this.lists.get(i).get(index - previousListSize);
      }
      previousListSize = this.cumulativeSize[i];
    }
    throw new RandoopBug("Indexing error in ListOfLists");
  }

  @Override
  public SimpleList<T> getSublist(@Det int index) {
    if (index < 0 || index > this.totalelements - 1) {
      throw new IllegalArgumentException("index must be between 0 and size()-1");
    }
    int previousListSize = 0;
    for (int i = 0; i < this.cumulativeSize.length; i++) {
      if (index < this.cumulativeSize[i]) {
        // Recurse.
        return lists.get(i).getSublist(index - previousListSize);
      }
      previousListSize = cumulativeSize[i];
    }
    throw new RandoopBug("indexing error in ListOfLists");
  }

  @Override
  public @Det List<T> toJDKList(@Det ListOfLists<T> this) {
    List<T> result = new ArrayList<>();
    for (SimpleList<T> l : lists) {
      result.addAll(l.toJDKList());
    }
    return result;
  }

  @Override
  public String toString(ListOfLists<T> this) {
    @SuppressWarnings("determinism") // toJDKList requires @Det but it's clearly @PolyDet
    @PolyDet
    String result = toJDKList().toString();
    return result;
  }
}
