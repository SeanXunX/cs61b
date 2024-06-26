package gh2;
import edu.princeton.cs.algs4.StdAudio;
import edu.princeton.cs.algs4.StdDraw;

/**
 * A client that uses the synthesizer package to replicate a plucked guitar string sound
 */
public class GuitarHero {
    public static final double CONCERT_A = 440.0;

    public static void main(String[] args) {
        /* create two guitar strings, for concert A and C */

        String keyboard = "q2we4r5ty7u8i9op-[=zxdcfvgbnjmk,.;/' ";
        GuitarString[] strings = new GuitarString[37];
        for (int i = 0; i < 37; i++) {
            strings[i] = new GuitarString(CONCERT_A * 2 * Math.pow(2, (double) (i - 24) / 12));
        }
        int index = 0;
        while (true) {

            /* check if the user has typed a key; if so, process it */
            if (StdDraw.hasNextKeyTyped()) {
                char key = StdDraw.nextKeyTyped();
                index = keyboard.indexOf(key);
                strings[index].pluck();
            }
            /* compute the superposition of samples */
            double sample = strings[index].sample();

            /* play the sample on standard audio */
            StdAudio.play(sample);

            /* advance the simulation of each guitar string by one step */
            strings[index].tic();

        }
    }
}

