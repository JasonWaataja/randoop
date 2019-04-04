package randoop.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.checkerframework.checker.determinism.qual.Det;

public class SimpleArrayList<E extends @Det Object> extends ArrayList<E>
    implements SimpleList<E>, Serializable {

  private static final long serialVersionUID = 20180317;

  public SimpleArrayList(@Det Collection<? extends E> c) {
    super(c);
  }

  public SimpleArrayList() {
    super();
  }

  public SimpleArrayList(@Det int initialCapacity) {
    super(initialCapacity);
  }

  @Override
  // Return the entire list.
  public SimpleList<E> getSublist(int index) {
    return this;
  }

  @Override
  public List<E> toJDKList() {
    return new ArrayList<>(this);
  }
}
