package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    private Comparator<T> cmp;

    public MaxArrayDeque(Comparator<T> c) {
        super(); //Automatically apply the super class's constructor.
        this.cmp = c;
    }

    public T max() {
        if (isEmpty()) {
            return null;
        }
        int maxDex = 0;
        for (int i = 0; i < this.size(); i++) {
            if (cmp.compare(this.get(i), this.get(maxDex)) > 0) {
                maxDex = i;
            }
        }
        return this.get(maxDex);
    }

    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }
        int maxDex = 0;
        for (int i = 0; i < this.size(); i++) {
            if (c.compare(this.get(i), this.get(maxDex)) > 0) {
                maxDex = i;
            }
        }
        return this.get(maxDex);
    }

}
