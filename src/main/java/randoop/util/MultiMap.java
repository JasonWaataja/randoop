package randoop.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.checkerframework.checker.determinism.qual.Det;

/** Implements an IMultiMap with a java.util.LinkedHashMap. */
public class MultiMap<T1 extends @Det Object, T2 extends @Det Object> implements IMultiMap<T1, T2> {

  private final Map<T1, Set<T2>> map;

  public MultiMap() {
    map = new LinkedHashMap<>();
  }

  public MultiMap(int initialCapacity) {
    map = new LinkedHashMap<>(initialCapacity);
  }

  public void put(@Det MultiMap<T1, T2> this, @Det T1 key, @Det Collection<? extends T2> values) {
    if (contains(key)) remove(key);
    map.put(key, new LinkedHashSet<T2>(values));
  }

  public void addAll(@Det MultiMap<T1, T2> this, @Det Map<? extends T1, ? extends T2> m) {
    for (T1 t1 : m.keySet()) {
      add(t1, m.get(t1));
    }
  }

  public void addAll(@Det MultiMap<T1, T2> this, T1 key, @Det Collection<? extends T2> values) {
    for (T2 t2 : values) {
      add(key, t2);
    }
  }

  public void addAll(@Det MultiMap<T1, T2> this, MultiMap<T1, T2> mmap) {
    for (Map.Entry<T1, Set<T2>> entry : mmap.map.entrySet()) {
      addAll(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void add(@Det MultiMap<T1, T2> this, T1 key, T2 value) {
    Set<T2> values = map.get(key);
    if (values == null) {
      values = new LinkedHashSet<>(1);
      map.put(key, values);
    }
    values.add(value);
  }

  @Override
  public void remove(@Det MultiMap<T1, T2> this, T1 key, T2 value) {
    Set<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: "
              + key
              + " Variable: "
              + value);
    }
    values.remove(value);
  }

  public void remove(@Det MultiMap<T1, T2> this, T1 key) {
    Set<T2> values = map.get(key);
    if (values == null) {
      throw new IllegalStateException(
          "No values where found when trying to remove from multiset. Key: " + key);
    }
    map.remove(key);
  }

  @Override
  public Set<T2> getValues(T1 key) {
    Set<T2> values = map.get(key);
    if (values == null) {
      return Collections.emptySet();
    }
    return values;
  }

  @Override
  public Set<T1> keySet() {
    return map.keySet();
  }

  public boolean contains(T1 obj) {
    return map.containsKey(obj);
  }

  public void clear(@Det MultiMap<T1, T2> this) {
    map.clear();
  }

  @Override
  public int size() {
    return map.size();
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public String toString() {
    return map.toString();
  }
}
