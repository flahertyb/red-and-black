// Visitor interface for assignment 7.

public interface Visitor<K,V> {

    // Given a key and a value, returns a suitable value.

    public V visit (K k, V v);
}
