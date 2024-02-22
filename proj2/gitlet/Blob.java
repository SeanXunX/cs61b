package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

public class Blob implements Serializable {
    /**
     * Blob is the saved contents of files.
     */

    /**
     * Current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File ADD_DIR = join(GITLET_DIR, "stage", "addition");
    public static final File RM_DIR = join(GITLET_DIR, "stage", "removal");
    public static final File BLOBS_DIR = join(GITLET_DIR, "objects", "blobs");


    /**
     * File name.
     */
    private final String fileName;

    /**
     * The path of the file this blob saves in stage folder (addition, removal).
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
        File saveFile = join(ADD_DIR, id);
        if (!saveFile.exists()) {
            rmTarName(fileName);
            writeContents(saveFile, readContentsAsString(filePath));
            addFiles.put(id, fileName);
        }
    }

    private static void rmTarName(String fileName) {
        for (Map.Entry<String, String> entry : addFiles.entrySet()) {
            if (entry.getKey().equals(fileName)) {
                join(ADD_DIR, entry.getKey()).delete();
                addFiles.remove(entry.getKey());
            }
        }
    }

    /**
     * Adds this blob to the removal.
     */
    public static void toRm(String id, String fileName) {
        File rmPath = join(RM_DIR, id);
        File filePath = join(BLOBS_DIR, id);
        copy(filePath, rmPath);
        rmFiles.put(id, fileName);
    }

    /**
     * returns if the blob has already existed in the addition
     */
    public static boolean existsInAdd(String id) {
        return join(ADD_DIR, id).exists();
    }

    /**
     * Removes the Blob from the addition if exists
     */
    public static void removeFromAdd(String id) {
        if (existsInAdd(id)) {
            File addPath = join(ADD_DIR, id);
            addPath.delete();
            addFiles.remove(id);
        }
    }

    public static boolean existsInCurCommit(String id) {
        Commit currentCommit = Commit.getHeadCommit();
        return currentCommit.hasBlob(id);
    }

    public static String NameToIdInAddition(String fileName) {
        for (Map.Entry<String, String> entryTracked : addFiles.entrySet()) {
            if (entryTracked.getValue().equals(fileName)) {
                return entryTracked.getKey();
            }
        }
        return null;
    }

}
