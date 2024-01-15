package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testGetLast() {
        AListNoResizing<Integer> noReList = new AListNoResizing<>();
        BuggyAList<Integer> bugList = new BuggyAList<>();
        for (int i = 1; i < 8; i *= 2) {
            noReList.addLast(i);
            bugList.addLast(i);
        }
        for (int i = 1; i < 8; i *= 2) {
            assertEquals(noReList.removeLast(), bugList.removeLast());
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> B = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                B.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int sizeb = B.size();
                assertEquals(size, sizeb);
                System.out.println("size: " + size);
            } else if (operationNumber == 2 && L.size() > 0) {
                // getLast
                int last = L.getLast();
                int lastb = B.getLast();
                assertEquals(last, lastb);
                System.out.println("getLast:" + last);
            } else if (operationNumber == 3 && L.size() > 0) {
                // removeLast
                int last = L.removeLast();
                int lastb = B.removeLast();
                assertEquals(last, lastb);
                System.out.println("removeLast: " + last);
            }
        }
    }
}
