package gitlet;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static gitlet.Utils.*;
public class MyUtils {

    /**
     * Writes the contents of src to tar. (String)
     */
    public static void writeContentFromFile(File src, File tar) {
        writeContents(tar, readContentsAsString(src));
    }

    /**
     * Copy a file to the target position.
     */
    public static void copy(File src, File tar) {
        if (!tar.exists() && src.exists()) {
            writeContentFromFile(src, tar);
        }
    }

    /**
     * Moves a file to the target position.
     * Deletes the source file.
     */
    public static void move(File src, File tar) {
        copy(src, tar);
        src.delete();
    }

    /**
     * Prints date as required.
     */
    public static void printDate(Date date) {
        System.out.println("Date: " + dateToTimeStamp(date));
    }

    private static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

}
