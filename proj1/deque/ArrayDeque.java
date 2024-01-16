package deque;

public class ArrayDeque<ElemType> implements Listproj1<ElemType>{

    private ElemType[] items;
    //head and tail points to the next position where new item will be placed.
    private int head;
    private int tail;
    private int size;

    //Create an array deque with the starting size of 8.
    public ArrayDeque() {
        items = (ElemType[]) new Object[8];
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
    public void addFirst(ElemType item) {
        if (head == tail) {
            resize(items.length * 2);
        }
        items[head--] = item;
        size++;
        head = adjustIndex(head);
    }

    @Override
    public void addLast(ElemType item) {
        if (head == tail) {
            resize(items.length * 2);
        }
        items[tail++] = item;
        size++;
        tail = adjustIndex(tail);
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    public void resize(int capacity) {
        ElemType[] newArr = (ElemType[]) new Object[capacity];
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
    public ElemType removeFirst() {
        if (isEmpty()) {
            return null;
        }
        if (items.length >= 16 && size - 1 < (double)(items.length / 4)) {
            resize(items.length / 4);
        }
        head++;
        head = adjustIndex(head);
        ElemType res = items[head];
        items[head] = null;
        size--;
        return res;
    }

    @Override
    public ElemType removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (items.length >= 16 && size - 1 < (double)(items.length / 4)) {
            resize(items.length / 4);
        }
        tail--;
        tail = adjustIndex(tail);
        ElemType res = items[tail];
        items[tail] = null;
        size--;
        return res;
    }

    @Override
    public ElemType get(int index) {
        if (index < 0 || index >= size || isEmpty()) {
            return null;
        }
        return items[adjustIndex(head + 1 + index)];
    }

}
