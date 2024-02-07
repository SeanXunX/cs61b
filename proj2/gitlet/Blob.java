package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;

public class Blob implements Serializable {
    /**
     * Blob is the saved contents of files.
     */

    /**
     * Current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File add_DIR = join(GITLET_DIR, "stage", "addition");
    public static final File rm_DIR = join(GITLET_DIR, "stage", "removal");


    /**
     * File name.
     */
    private final String fileName;

    /**
     * The path of the file this blob saves.
     */
    private final File filePath;

    /**
     * This blob's id.
     */
    private final String id;

    /**
     * The files stored in the addition.
     * Id to name.
     */
    private static final HashMap<String, String> addFiles = Main.serialized.getAddFiles();

    /**
     * The files stored in the removal.
     * Id to name.
     */
    private static final HashMap<String, String> rmFiles = Main.serialized.getRmFiles();

    public static HashMap<String, String> getAddFiles() {
        return addFiles;
    }
    public static HashMap<String, String> getRmFiles() {
        return rmFiles;
    }
    public Blob(String fileName) {
        this.fileName = fileName;
        filePath = join(CWD, fileName);
        id = generateId();
    }

    /**
     * Generate id based on the file name and contents.
     */
    private String generateId() {
        String contents = readContentsAsString(filePath);
        return sha1(contents, fileName);
    }

    public String getId() {
        return id;
    }


    /**
     * Adds this blob to the addition using the id as its name
     * The contents is the file content.
     */
    public void toAdd() {
        File saveFile = join(add_DIR, id);
        if (!saveFile.exists()) {
            rmTarName(fileName);
            writeContents(saveFile, readContentsAsString(filePath));
            addFiles.put(id, fileName);
        }
    }

    public static void rmTarName(String fileName) {
        for (Map.Entry<String, String> entry : addFiles.entrySet()) {
            if (entry.getKey().equals(fileName)) {
                join(add_DIR, entry.getKey()).delete();
                addFiles.remove(entry.getKey());
            }
        }
    }

    /**
     * Add this blob to the removal.
     */
    public void toRm() {
        File saveFile = join(rm_DIR, id);
        writeContents(saveFile, filePath);
        rmFiles.put(id, fileName);
    }

    /**
     * returns if the blob has already existed in the addition
     */
    public boolean existsInAdd() {
        return join(add_DIR, id).exists();
    }

    /**
     * Removes the Blob from the addition if exists
     */
    public void removeFromAdd() {
        File addPath = join(add_DIR, id);
        if (existsInAdd()) {
            addPath.delete();
            addFiles.remove(id);
        }
    }

    public boolean existsInCurCommit() {
        Commit currentCommit = Commit.getHeadCommit();
        return currentCommit.hasBlob(this);
    }

}
