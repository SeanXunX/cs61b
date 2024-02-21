package gitlet;


import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

/**
 * Represents a gitlet commit object.
 * <p>
 * 1.log message
 * 2.timestamp
 * 3.a mapping of file names to blob references
 * 4.a parent reference (sha1 id of parent commit)
 * 5.a second parent reference (for merge)
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /**
     * The objects path where the serialized content stores.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File HEAD = join(GITLET_DIR, "HEAD");
    public static final File commits_DIR = join(GITLET_DIR, "objects", "commits");
    public static final File blobs_DIR = join(GITLET_DIR, "objects", "blobs");
    public static final File add_DIR = join(GITLET_DIR, "stage", "addition");
    public static final File rm_DIR = join(GITLET_DIR, "stage", "removal");
    public static final File heads_DIR = join(GITLET_DIR, "refs", "heads");


    /**
     * The message of this Commit.
     */
    private String message;

    /**
     * The date
     */
    private Date date;

    /**
     * The ids of this commit's parents. The first is the original parent.
     */
    private String parent;
    private String second_parent;

    /**
     * The blobs that this commit tracks. Key is sha1. Value is the original file name.
     * For instance, key is a9s8d...9dsf, and value is text1.txt.
     */
    private HashMap<String, String> idToName;

    /**
     * The reference, as well as the sha1 id, of this commit.
     */
    private String id;


    /**
     * initial commit
     */
    public Commit() {
        message = "initial commit";
        date = new Date(0);
        parent = null;
        second_parent = null;
        idToName = new HashMap<>();
        id = generateIdInit();
    }

    /**
     * Create new commit after HEAD.
     */
    public Commit(String message) {
        Commit headCommit = getHeadCommit();
        this.message = message;
        date = new Date();
        parent = headCommit.id;
        second_parent = null;
        idToName = new HashMap<>();
        idToName.putAll(headCommit.getIdToName());
        id = generateId();
        addToObjects();
        rmFromMapping();
    }

    public Commit(String message, String second_parentId) {
        Commit headCommit = getHeadCommit();
        this.message = message;
        date = new Date();
        parent = headCommit.id;
        second_parent = second_parentId;
        idToName = new HashMap<>();
        idToName.putAll(headCommit.getIdToName());
        id = generateId();
        addToObjects();
        rmFromMapping();
    }

    /**
     * Moves the files in the addition to the object.
     */
    private void addToObjects() {

        Iterator<Map.Entry<String, String>> iterator = Blob.getAddFiles().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            // Removes the old version in the idToName mapping and updates.
            idToName.entrySet().removeIf(entryTracked -> entryTracked.getValue().equals(entry.getValue()));
            idToName.put(entry.getKey(), entry.getValue());
            File src = join(add_DIR, entry.getKey());
            File tar = join(blobs_DIR, entry.getKey());
            move(src, tar);
            // Removes the processed entry from Blob.getAddFiles().
            iterator.remove();
        }
    }

    /**
     * According to the given name, returns the id in the mapping.
     */
    public String NameToIdInMapping(String fileName) {
        for (Map.Entry<String, String> entryTracked : idToName.entrySet()) {
            if (entryTracked.getValue().equals(fileName)) {
                return entryTracked.getKey();
            }
        }
        return null;
    }


    public static String NameToIdInMappingCurCom(String fileName) {
        Commit curCommit = getHeadCommit();
        return curCommit.NameToIdInMapping(fileName);
    }

    /**
     * Removes the files that are tracked in the removal from the current blob mapping.
     */
    private void rmFromMapping() {
        Iterator<Map.Entry<String, String>> iterator = Blob.getRmFiles().entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            idToName.remove(entry.getKey());
            join(rm_DIR, entry.getKey()).delete();
            iterator.remove();
        }
    }

    /**
     * Each commit is identified by its SHA-1 id,
     * which must include the file (blob) references of its files, parent reference, log message, and commit time.
     * Generates the id according to message, date, parent, second_parent, idToName
     */
    private String generateId() {
        return sha1(message, date.toString(), parent, Objects.requireNonNullElse(second_parent, ""), idToName.toString());
    }

    private String generateIdInit() {
        return sha1(message, date.toString(), "", "", idToName.toString());
    }

    /**
     * Get id.
     */
    public String getId() {
        return id;
    }

    /**
     * Saves this commit to the object.
     */
    public void saveCommit() {
        writeObject(join(commits_DIR, id), this);
    }

    /**
     * returns if the blob with the id (blobId) has exits in this commit
     */
    public boolean hasBlob(String id) {
        return idToName.containsKey(id);
    }

    /**
     * Returns if the commit has the file with fileName.
     */
    public boolean hasFile(String fileName) {
        for (Map.Entry<String, String> entry : idToName.entrySet()) {
            if (entry.getValue().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * gets the commit to which HEAD points
     */
    public static Commit getHeadCommit() {
        File headCommitPath = join(commits_DIR, readContentsAsString(HEAD));
        return readObject(headCommitPath, Commit.class);
    }

    /**
     * Get the given branch's HEAD points
     */
    public static Commit getHeadCommitOfBranch(String branchName) {
        File branHead = join(heads_DIR, branchName);
        if (branHead.exists()) {
            File headCommitPath = join(commits_DIR, readContentsAsString(branHead));
            if (!headCommitPath.exists()) {
                return null;
            }
            return readObject(headCommitPath, Commit.class);
        }
        return null;
    }

    public String getMessage() {
        return message;
    }
    public Date getDate() {
        return date;
    }
    public String getParent() {
        return parent;
    }
    public String getSecond_parent() {
        return second_parent;
    }
    public HashMap<String, String> getIdToName() {
        return idToName;
    }
    public Commit parentCommit() {
        if (parent != null) {
            File parentFile = join(commits_DIR, parent);
            return readObject(parentFile, Commit.class);
        }
        return null;
    }
}
