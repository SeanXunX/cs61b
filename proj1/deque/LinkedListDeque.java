package deque;


import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {

    /**
     * Node of linked list deque
     */
    private class LNode {
        T item;
        LNode prev;
        LNode next;

        LNode(T item, LNode prev, LNode next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }

    //Use one sentinel.(Circular Sentinel)
    //Sentinel's next pointer points to the first Node of the deque (if exits).
    //Sentinel's prev pointer points to the last Node of the deque (if exits).
    private LNode sentinel;
    private int size;

    public LinkedListDeque() {
        size = 0;
        sentinel = new LNode(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    @Override
    public void addFirst(T item) {
        LNode newNode = new LNode(item, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    @Override
    public void addLast(T item) {
        LNode newNode = new LNode(item, sentinel.prev, sentinel);
        sentinel.prev.next = newNode;
        sentinel.prev = newNode;
        size++;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        LNode t = sentinel.next;
        while (t != sentinel) {
            System.out.print(t.item + " ");
            t = t.next;
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T res = sentinel.next.item;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size--;
        return res;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T res = sentinel.prev.item;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return res;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > size - 1 || isEmpty()) {
            return null;
        }
        LNode temp = sentinel;
        int i = 0;
        while (i++ <= index) {
            temp = temp.next;
        }
        return temp.item;
    }

    private T getRecursive(int index, LNode t) {
        if (index == 0) {
            return t.item;
        }
        return getRecursive(index - 1, t.next);
    }

    public T getRecursive(int index) {
        if (index < 0 || index > size - 1 || isEmpty()) {
            return null;
        }
        return getRecursive(index, sentinel.next);
    }

    @Override
    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<T> {
        LNode t = sentinel;

        @Override
        public boolean hasNext() {
            return t.next != sentinel;
        }

        @Override
        public T next() {
            T res = t.next.item;
            t = t.next;
            return res;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque<T> t = (Deque<T>) o;
        if (t.size() != size) {
            return false;
        }
        for (int i = 0; i < t.size(); i++) {
            if (!t.get(i).equals(get(i))) {
                return false;
            }
        }
        return true;
    }
}
