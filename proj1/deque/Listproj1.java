package deque;

public interface Listproj1<ElemType> {

    public void addFirst(ElemType item);

    public void addLast(ElemType item);

    public boolean isEmpty();

    public int size();

    public void printDeque();

    public ElemType removeFirst();

    public ElemType removeLast();

    public ElemType get(int index);

}
