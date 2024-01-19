package deque;

import afu.org.checkerframework.checker.oigj.qual.O;

import java.util.Iterator;

public class LinkedListDeque<ElemType> implements Iterable<ElemType>, Deque<ElemType> {

    /**
     * Node of linked list deque
     */
    private class LNode {
        ElemType item;
        LNode prev;
        LNode next;

        public LNode(ElemType item, LNode prev, LNode next) {
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

    public LinkedListDeque(ElemType x) {
        size = 1;
        sentinel = new LNode(null, null, null);
        LNode newNode = new LNode(x, sentinel, sentinel);
        sentinel.next = newNode;
        sentinel.prev = newNode;
    }

    @Override
    public void addFirst(ElemType item) {
        LNode newNode = new LNode(item, sentinel, sentinel.next);
        sentinel.next.prev = newNode;
        sentinel.next = newNode;
        size++;
    }

    @Override
    public void addLast(ElemType item) {
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
    public ElemType removeFirst() {
        if (isEmpty()) {
            return null;
        }
        ElemType res = sentinel.next.item;
        sentinel.next.next.prev = sentinel;
        sentinel.next = sentinel.next.next;
        size--;
        return res;
    }

    @Override
    public ElemType removeLast() {
        if (isEmpty()) {
            return null;
        }
        ElemType res = sentinel.prev.item;
        sentinel.prev.prev.next = sentinel;
        sentinel.prev = sentinel.prev.prev;
        size--;
        return res;
    }

    @Override
    public ElemType get(int index) {
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

    private ElemType getRecursive(int index, LNode t) {
        if (index == 0) {
            return t.item;
        }
        return getRecursive(index - 1, t.next);
    }

    public ElemType getRecursive(int index) {
        if (index < 0 || index > size - 1 || isEmpty()) {
            return null;
        }
        return getRecursive(index, sentinel.next);
    }

    @Override
    public Iterator<ElemType> iterator() {
        return new LinkedListDequeIterator();
    }

    private class LinkedListDequeIterator implements Iterator<ElemType> {
        LNode t = sentinel;

        @Override
        public boolean hasNext() {
            return t.next == sentinel;
        }

        @Override
        public ElemType next() {
            ElemType res = t.next.item;
            t = t.next;
            return res;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<ElemType> t = (LinkedListDeque<ElemType>) o;
        if (t.size() != size) {
            return false;
        }
        LNode otherNode = t.sentinel, myNode = sentinel;
        while (myNode.next != sentinel) {
            if (otherNode.next.item != myNode.next.item) {
                return false;
            }
            otherNode = otherNode.next;
            myNode = myNode.next;
        }
        return true;
    }
}
