package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;

public class KeyToMultiSet<T1 extends @Det Object, T2 extends @Det Object> {

  private final Map<T1, MultiSet<T2>> map;

  public KeyToMultiSet() {
    map = new LinkedHashMap<>();
  }

  public void addAll(@Det KeyToMultiSet<T1, T2> this, @Det Map<? extends T1, ? extends T2> m) {
    for (@Det T1 t1 : m.keySet()) {
      add(t1, m.get(t1));
    }
  }

  public void addAll(
      @Det KeyToMultiSet<T1, T2> this, T1 key, @Det Collection<? extends T2> values) {
    for (@Det T2 t2 : values) {
      add(key, t2);
    }
  }

  public void add(@Det KeyToMultiSet<T1, T2> this, T1 key, T2 value) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      values = new MultiSet<>();
    }
    values.add(value);
    map.put(key, values);
  }

  public void remove(@Det KeyToMultiSet<T1, T2> this, T1 key, T2 value) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    values.remove(value);
  }

  public void remove(@Det KeyToMultiSet<T1, T2> this, T1 key) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  public Set<T2> getVariables(T1 key) {
    MultiSet<T2> values = map.get(key);
    if (values == null) {
      return Collections.emptySet();
    }
    return values.getElements();
  }

  public Set<T1> keySet() {
    return map.keySet();
  }

  public boolean contains(T1 obj) {
    return map.containsKey(obj);
  }

  // Removes all keys with an empty set
  public void clean(@Det KeyToMultiSet<T1, T2> this) {
    @Det Iterator<Entry<T1, MultiSet<T2>>> iter = map.entrySet().iterator();
    for (; iter.hasNext(); ) {
      Entry<T1, MultiSet<T2>> element = iter.next();
      if (element.getValue().isEmpty()) {
        iter.remove();
      }
    }
  }

  public void removeAllInstances(@Det KeyToMultiSet<T1, T2> this, @Det Set<T2> values) {
    for (@Det MultiSet<T2> multiSet : map.values()) {
      multiSet.removeAllInstances(values);
    }
  }

  public void clear(@Det KeyToMultiSet<T1, T2> this) {
    map.clear();
  }

  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }
}
