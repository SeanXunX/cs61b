package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {

    private T[] items;
    //head and tail points to the next position where new item will be placed.
    private int head;
    private int tail;
    private int size;

    //Create an array deque with the starting size of 8.
    public ArrayDeque() {
        items = (T[]) new Object[8];
        size = 0;
        head = items.length - 1;
        tail = 0;
    }

    private int adjustIndex(int index) {
        if (index < 0) {
            return index + items.length;
        } else if (index >= items.length) {
            return index % items.length;
        }
        return index;
    }

    @Override
    public void addFirst(T item) {
        if (head == tail) {
            resize(items.length * 2);
        }
        items[head--] = item;
        size++;
        head = adjustIndex(head);
    }

    @Override
    public void addLast(T item) {
        if (head == tail) {
            resize(items.length * 2);
        }
        items[tail++] = item;
        size++;
        tail = adjustIndex(tail);
    }


    public void resize(int capacity) {
        T[] newArr = (T[]) new Object[capacity];
        int pos = 0;
        if (head >= tail) {
            for (int i = head + 1; i < items.length; i++, pos++) {
                newArr[pos] = items[i];
            }
            for (int i = 0; i < tail; i++, pos++) {
                newArr[pos] = items[i];
            }
        } else {
            for (int i = head + 1; i <= tail - 1; i++, pos++) {
                newArr[pos] = items[i];
            }
        }
        items = newArr;
        head = items.length - 1;
        tail = pos;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        if (head < tail) {
            for (int i = head + 1; i <= tail - 1; i++) {
                System.out.print(items[i] + " ");
            }
        } else {
            for (int i = head + 1; i < items.length; i++) {
                System.out.print(items[i] + " ");
            }
            for (int i = 0; i < tail; i++) {
                System.out.print(items[i] + " ");
            }
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (items.length >= 16 && size - 1 < (double) (items.length / 4)) {
            resize(items.length / 4);
        }
        head++;
        head = adjustIndex(head);
        T res = items[head];
        items[head] = null;
        size--;
        return res;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (items.length >= 16 && size - 1 < (double) (items.length / 4)) {
            resize(items.length / 4);
        }
        tail--;
        tail = adjustIndex(tail);
        T res = items[tail];
        items[tail] = null;
        size--;
        return res;
    }
    @Override
    public T get(int index) {
        if (index < 0 || index >= size || isEmpty()) {
            return null;
        }
        return items[adjustIndex(head + 1 + index)];
    }

    @Override
    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    private class ArrayDequeIterator implements Iterator<T> {
        int pos;
        @Override
        public boolean hasNext() {
            return pos < size;
        }

        @Override
        public T next() {
            return items[pos++];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<T> t = (ArrayDeque<T>) o;
        if (t.size() != size) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (t.get(i) != items[i]) {
                return false;
            }
        }
        return true;
    }

}
