package hashmap;

//import net.sf.saxon.tree.iter.PrependSequenceIterator;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }


    /* Instance Variables */
    private Collection<Node>[] buckets;
    private final double maxLoad;
    private int size;
    private int keySize;
    // You should probably define some more!

    /**
     * Constructors
     */
    public MyHashMap() {
        buckets = createTable(16);
        maxLoad = 0.75;
        size = 0;
    }

    public MyHashMap(int initialSize) {
        buckets = createTable(initialSize);
        maxLoad = 0.75;
        size = 0;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        this.maxLoad = maxLoad;
        size = 0;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!


    @Override
    public void clear() {
        buckets = null;
        size = 0;
        keySize = 0;
    }

    private boolean isEmpty() {
        return size == 0;
    }
    @Override
    public boolean containsKey(K key) {
        if (isEmpty()) {
            return false;
        }
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node elem : bucket) {
                if (elem.key.equals(key)) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public V get(K key) {
        if (isEmpty()) {
            return null;
        }
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node elem : bucket) {
                if (elem.key.equals(key)) {
                    return elem.value;
                }
            }
        }
        return null;
    }
    @Override
    public int size() {
        return keySize;
    }

    private Node getNode(K key) {
        if (isEmpty()) {
            return null;
        }
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node elem : bucket) {
                if (elem.key.equals(key)) {
                    return elem;
                }
            }
        }
        return null;
    }

    private void resize(int newSize) {
        Collection<Node>[] newTable = createTable(newSize);
        System.arraycopy(buckets, 0, newTable, 0, buckets.length);
        buckets = newTable;
    }
    @Override
    public void put(K key, V value) {
        Node newNode = createNode(key, value);
        int index = key.hashCode() % buckets.length;
        if (index < 0) {
            index += buckets.length;
        }
        Node tNode = getNode(key);
        if (tNode != null) {
            tNode.value = value;
        } else {
            if (buckets[index] == null) {
                buckets[index] = createBucket();
                size++;
            }
            buckets[index].add(newNode);
            keySize++;
            if ((double) size / buckets.length > maxLoad) {
                resize(buckets.length * 2);
            }
        }
    }
    @Override
    public Set<K> keySet() {
        HashSet<K> set = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            if (bucket == null) {
                continue;
            }
            for (Node elem : bucket) {
                set.add(elem.key);
            }
        }
        return set;
    }
    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        } else {
            Node tNode = getNode(key);
            V res = tNode.value;
            int index = key.hashCode() % buckets.length;
            if (index < 0) {
                index += buckets.length;
            }
            buckets[index].remove(tNode);
            if (buckets[index].isEmpty()) {
                size--;
            }
            keySize--;
            return res;
        }
    }
    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        } else {
            Node tNode = getNode(key);
            V res = tNode.value;
            if (res != value) {
                return null;
            }
            int index = key.hashCode() % buckets.length;
            if (index < 0) {
                index += buckets.length;
            }
            buckets[index].remove(tNode);
            if (buckets[index].isEmpty()) {
                size--;
            }
            keySize--;
            return res;
        }
    }
    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

}
