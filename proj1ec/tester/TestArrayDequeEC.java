package tester;

import static org.junit.Assert.*;

import edu.princeton.cs.introcs.StdRandom;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {

    @Test
    public void testAddRemove() {
        StudentArrayDeque<Integer> sad_buggy = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> sad_correct = new ArrayDequeSolution<>();

        StringBuilder message = new StringBuilder();

        for (int i = 0; i < 100; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.25) {
                sad_correct.addLast(i);
                sad_buggy.addLast(i);
                message.append("addLast(").append(i).append(")\n");
            } else if (numberBetweenZeroAndOne < 0.5) {
                sad_correct.addFirst(i);
                sad_buggy.addFirst(i);
                message.append("addFirst(").append(i).append(")\n");
            } else if (numberBetweenZeroAndOne < 0.75) {
                if (!sad_buggy.isEmpty() && !sad_correct.isEmpty()) {
                    Integer val_buggy = sad_buggy.removeFirst();
                    Integer val_correct = sad_correct.removeFirst();
                    message.append("removeFirst()\n");
                    assertEquals(message.toString(), val_correct, val_buggy);
                }
            } else if (numberBetweenZeroAndOne < 1) {
                if (!sad_buggy.isEmpty() && !sad_correct.isEmpty()) {
                    Integer val_buggy = sad_buggy.removeLast();
                    Integer val_correct = sad_correct.removeLast();
                    message.append("removeLast()\n");
                    assertEquals(message.toString(), val_correct, val_buggy);
                }
            }
        }

    }
}
