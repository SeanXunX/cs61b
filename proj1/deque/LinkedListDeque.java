package deque;

public class LinkedListDeque<ElemType> implements Listproj1<ElemType>{

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
    public boolean isEmpty() {
        return size == 0;
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

}
