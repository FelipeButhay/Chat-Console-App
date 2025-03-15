package server;

import java.util.HashMap;
import java.util.Set;

class BiHashMap<K, V> {
    private HashMap<K, V> forward = new HashMap<>();
    private HashMap<V, K> reverse = new HashMap<>();

    public void put(K key, V value) {
        forward.put(key, value);
        reverse.put(value, key);
    }

    public V get(K key) {
        return forward.get(key);
    }

    public K getInverse(V value) {
        return reverse.get(value);
    }

    public boolean containsKV(K key, V value) {
        return forward.containsKey(key) && forward.get(key).equals(value);
    }    

    public boolean containsValue(V value) {
        return reverse.containsKey(value);
    }

    public boolean containsKey(K key) {
        return forward.containsKey(key);
    }

    public Set<K> getAllKeys() {
        return forward.keySet();
    }

    public Set<V> getAllValues() {
        return reverse.keySet();
    }
}
