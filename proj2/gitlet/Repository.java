package gitlet;

//import com.google.common.collect.Maps;
//import com.sun.tools.javah.resources.version;
//import net.sf.saxon.trans.SymbolicName;
//import org.checkerframework.checker.units.qual.C;
//
//import net.sf.saxon.type.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.MyUtils.*;


/**
 * Represents a gitlet repository.
 * does at a high level.
 *
 * @author Sean
 */
public class Repository {

    /**The directory structure is like:
     *
     * CWD/.gitlet
     *      /objects
     *          /commits
     *          /blobs
     *      /stage
     *          /addition
     *          /removal
     *      /Head
     *      /refs
     *          /heads
     *          /remotes
     */

    /**
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The objects folder contains commits and blobs.
     */
    public static final File objects_DIR = join(GITLET_DIR, "objects");
    public static final File commits_DIR = join(GITLET_DIR, "objects", "commits");
    public static final File blobs_DIR = join(GITLET_DIR, "objects", "blobs");

    /**
     * The refs folder includes heads and remotes.
     */
    public static final File refs_DIR = join(GITLET_DIR, "refs");
    public static final File heads_DIR = join(GITLET_DIR, "refs", "heads");
    public static final File remotes_DIR = join(GITLET_DIR, "refs", "remotes");


    public static final File stage_DIR = join(GITLET_DIR, "stage");

    /**
     * Records the id to name mapping of addition and removal.
     */
    public static final File stageSerialized = join(GITLET_DIR, "Serialized");


    /**
     * The addition directory is for adding stage.
     */
    public static final File add_DIR = join(GITLET_DIR, "stage", "addition");

    /**
     * The removal directory is for removing stage.
     */
    public static final File rm_DIR = join(GITLET_DIR, "stage", "removal");


    /**
     * The HEAD file records the current commit's id.
     */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    private static String curBranchName = Main.serialized.getCurBranchName();


    /**
     * Creates a new Gitlet version-control system in the current directory.
     * <p>
     * This system will automatically start with one commit:
     * a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
     * <p>
     * It will have a single branch: master, which initially points to this initial commit, and master will be the current branch.
     * <p>
     * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose
     * for dates (this is called “The (Unix) Epoch”, represented internally by the time 0.)
     */
    public static void init() throws IOException {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists " +
                    "in the current directory.");
            System.exit(0);
        }

        //Create directories
        GITLET_DIR.mkdir();

        objects_DIR.mkdir();
        commits_DIR.mkdir();
        blobs_DIR.mkdir();

        refs_DIR.mkdir();
        heads_DIR.mkdir();
        remotes_DIR.mkdir();

        stage_DIR.mkdir();
        stageSerialized.createNewFile();
        add_DIR.mkdir();
        rm_DIR.mkdir();

        HEAD.createNewFile();

        //Create initial commit
        Commit newCommit = new Commit();
        //Create branch master
        File masterPath = join(heads_DIR, "master");
        curBranchName = "master";
        Main.serialized.setCurBranchName(curBranchName);
        writeContents(masterPath, newCommit.getId());
        //Update HEAD
        writeContentFromFile(masterPath, HEAD);
        //Serialized Save Commit.
        newCommit.saveCommit();
    }

    /**
     * Judges if already initialized.
     */
    private static boolean isInitialized() {
        return GITLET_DIR.exists();
    }

    private static void notInitializedError() {
        if (!isInitialized()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * Adds a copy of the file to the addition
     * <p>
     * If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a
     * file is changed, added, and then changed back to its original version).
     * <p>
     * If the file does not exist, print the error message File does not exist. and exit without changing anything.
     */
    public static void add(String fileName) {
        notInitializedError();
        File file = join(CWD, fileName);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob newBlob = new Blob(fileName);
        newBlob.toAdd();
        if (Blob.existsInCurCommit(newBlob.getId())) {
            Blob.removeFromAdd(newBlob.getId());
        }
    }

    /**
     * By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files
     * <p>
     * A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit,
     * in which case the commit will now include the version of the file that was staged instead of the version it got from its parent.
     * <p>
     * A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent.
     * <p>
     * Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
     * <p>
     * The staging area is cleared after a commit.
     * <p>
     * The commit just made becomes the “current commit”, and the head pointer now points to it. The previous head commit is this commit’s parent commit.
     * <p>
     * Each commit is identified by its SHA-1 id, which must include the file (blob) references of its files, parent reference, log message, and commit time.
     * <p>
     * <p>
     * If no files have been staged, abort. Print the message No changes added to the commit.
     * <p>
     * Every commit must have a non-blank message. If it doesn’t, print the error message Please enter a commit message.
     */
    public static void commit(String message) {
        notInitializedError();
        //Failure cases
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        if (Blob.getAddFiles().size() + Blob.getRmFiles().size() == 0) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        Commit newCommit = new Commit(message);
        writeContents(HEAD, newCommit.getId());
        writeContents(join(heads_DIR, curBranchName), newCommit.getId());
        newCommit.saveCommit();
    }

    /**
     * Commit after merge
     */
    private static void MergeCommit(String message, String second_parentId, String branName) {
        notInitializedError();
        //Failure cases
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Commit newCommit = new Commit(message, second_parentId);
        writeContents(HEAD, newCommit.getId());
        writeContents(join(heads_DIR, curBranchName), newCommit.getId());
        writeContents(join(heads_DIR, branName), newCommit.getId());
        newCommit.saveCommit();
    }

    /**
     * Unstage the file if it is currently staged for addition.
     * <p>
     * If the file is tracked in the current commit, stage it for removal
     * and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     * <p>
     * If the file is neither staged nor tracked by the head commit, print the error message No reason to remove the file.
     */
    public static void rm(String fileName) {
        notInitializedError();
        String id_Add = Blob.NameToIdInAddition(fileName);
        String id_Com = Commit.NameToIdInMappingCurCom(fileName);
        File cwdFile = join(CWD, fileName);
        if (id_Add == null && id_Com == null) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else {
            if (id_Add != null) {
                Blob.removeFromAdd(id_Add);
            }
            if (id_Com != null) {
                Blob.toRm(id_Com, fileName);
                if (cwdFile.exists()) {
                    cwdFile.delete();
                }
            }
        }
    }

    /**
     * Starting at the current head commit,
     * display information about each commit backwards along the commit tree until the initial commit,
     * following the first parent commit links, ignoring any second parents found in merge commits.
     * For every node in this history, the information it should display is the commit id,
     * the time the commit was made, and the commit message.
     * There is a === before each commit and an empty line after it
     * The timestamps displayed in the commits reflect the current timezone, not UTC; as a result,
     * the timestamp for the initial commit does not read Thursday, January 1st, 1970, 00:00:00,
     * but rather the equivalent Pacific Standard Time.
     * By the way, you’ll find that the Java classes java.util.Date and java.util.Formatter are useful for getting and formatting times.
     * For merge commits (those that have two parent commits), add a line just below the first, as in
     * Example
     * ===
     * commit 3e8bf1d794ca2e9ef8a4007275acf3751c7170ff
     * Merge: 4975af1 2c1ead1
     * Date: Sat Nov 11 12:30:00 2017 -0800
     * Merged development into master.
     * <p>
     * where the two hexadecimal numerals following “Merge:”
     * consist of the first seven digits of the first and second parents’ commit ids, in that order.
     * The first parent is the branch you were on when you did the merge; the second is that of the merged-in branch.
     */
    public static void log() {
        notInitializedError();
        Commit curCommit = Commit.getHeadCommit();
        while (curCommit != null) {
            printLog(curCommit);
            if (curCommit.getParent() == null) {
                break;
            } else {
                curCommit = curCommit.parentCommit();
            }
        }
    }

    /**
     * Like log, except displays information about all commits ever made
     */
    public static void global_log() {
        notInitializedError();
        Commit curCommit;
        List<String> commitNames = plainFilenamesIn(commits_DIR);
        if (commitNames != null) {
            for (String name : commitNames) {
                curCommit = readObject(join(commits_DIR, name), Commit.class);
                printLog(curCommit);
            }
        }
    }

    /**
     * Prints the log info.
     */
    private static void printLog(Commit curCommit) {
        System.out.print("===\n" + "commit " + curCommit.getId() + "\n");
        if (curCommit.getSecond_parent() != null) {
            System.out.println("Merge: " + sevenAbb(curCommit.getParent()) + " " + sevenAbb(curCommit.getSecond_parent()));
        }
        printDate(curCommit.getDate());
        System.out.println(curCommit.getMessage());
        System.out.println();
    }

    /**
     * First seven digits of id.
     */
    private static String sevenAbb(String id) {
        return id.substring(0, 7);
    }

    /**
     * Prints out the ids of all commits that have the given commit message, one per line.
     * If no such commit exists, prints the error message Found no commit with that message.
     */
    public static void find(String commitMessage) {
        notInitializedError();
        boolean hasMessage = false;
        Commit curCommit;
        List<String> commitNames = plainFilenamesIn(commits_DIR);
        if (commitNames != null) {
            for (String name : commitNames) {
                curCommit = readObject(join(commits_DIR, name), Commit.class);
                if (curCommit.getMessage().equals(commitMessage)) {
                    System.out.println(curCommit.getId());
                    hasMessage = true;
                }
            }
            if (!hasMessage) {
                System.out.println("Found no commit with that message.");
                System.exit(0);
            }
        } else {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /**
     * Creates a new branch with the given name, and points it at the current head commit.
     * <p>
     * If a branch with the given name already exists, print the error message A branch with that name already exists.
     */
    public static void branch(String branchName) {
        notInitializedError();
        List<String> branches = plainFilenamesIn(heads_DIR);
        if (branches != null) {
            for (String name : branches) {
                if (name.equals(branchName)) {
                    System.out.println("A branch with that name already exists.");
                    System.exit(0);
                }
            }
        }
        File newBranch = join(heads_DIR, branchName);
        Commit curCommit = Commit.getHeadCommit();
        writeContents(newBranch, curCommit.getId());
    }

    /**
     * Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been
     * staged for addition or removal.
     * <p>
     * Example:
     * <p>
     * === Branches ===
     * *master
     * other-branch
     * <p>
     * === Staged Files ===
     * wug.txt
     * wug2.txt
     * <p>
     * === Removed Files ===
     * goodbye.txt
     * <p>
     * === Modifications Not Staged For Commit ===
     * junk.txt (deleted)
     * wug3.txt (modified)
     * <p>
     * === Untracked Files ===
     * random.stuff
     * <p>
     * There is an empty line between sections, and the entire status ends in an empty line as well.
     * Entries should be listed in lexicographic order, using the Java string-comparison order (the asterisk doesn’t count).
     * <p>
     * A file in the working directory is “modified but not staged” if it is
     * <p>
     * Tracked in the current commit, changed in the working directory, but not staged; or
     * Staged for addition, but with different contents than in the working directory; or
     * Staged for addition, but deleted in the working directory; or
     * Not staged for removal, but tracked in the current commit and deleted from the working directory.
     * <p>
     * The final category (“Untracked Files”) is for files present in the working directory
     * but neither staged for addition nor tracked. This includes files that have been staged for removal,
     * but then re-created without Gitlet’s knowledge.
     */
    public static void status() {
        notInitializedError();

        //Branches
        printBranches();

        //file name
        List<String> stagedFs = new LinkedList<>();
        List<String> removedFs = new LinkedList<>();
        List<String> modNotStagedFs = new LinkedList<>();
        List<String> untrackedFs = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(CWD)));

        Commit curCommit = Commit.getHeadCommit();

        //Tracked in the current commit, changed in the working directory, but not staged.
        //Not staged for removal, but tracked in the current commit and deleted from the working directory.
        if (curCommit.getIdToName() != null) {
            for (Map.Entry<String, String> entry : curCommit.getIdToName().entrySet()) {
                String fileName = entry.getValue();
                String cwdId = cwdNameToId(fileName);
                if (cwdId == null) {
                    //not exists in CWD
                    if (Blob.getRmFiles().containsKey(entry.getKey())) {
                        //exists in the removal
                        removedFs.add(fileName);
                    } else {
                        //not exists in the removal
                        modNotStagedFs.add(fileName);
                    }
                } else if (!Blob.getAddFiles().containsKey(cwdId)) {
                    if (!cwdId.equals(entry.getKey())) {
                        modNotStagedFs.add(fileName);
                    }
                    //CWD is identical to the commit
                    untrackedFs.remove(fileName);
                }
            }
        }

        //Staged for addition, but with different contents than in the working directory
        //Staged for addition, but deleted in the working directory
        if (Blob.getAddFiles() != null) {
            for (Map.Entry<String, String> entry : Blob.getAddFiles().entrySet()) {
                String fileName = entry.getValue();
                String cwdId = cwdNameToId(fileName);
                if (cwdId == null) {
                    //null means not existing in CWD
                    modNotStagedFs.add(fileName);
                } else {
                    if (!cwdId.equals(entry.getKey())) {
                        modNotStagedFs.add(fileName);
                    } else {
                        stagedFs.add(fileName);
                    }
                    untrackedFs.remove(fileName);
                }
            }
        }

        System.out.println("=== Staged Files ===");
        printConditionFileNames(stagedFs);
        System.out.println("=== Removed Files ===");
        printConditionFileNames(removedFs);
        System.out.println("=== Modifications Not Staged For Commit ===");
        printConditionFileNames(modNotStagedFs);
        System.out.println("=== Untracked Files ===");
        printConditionFileNames(untrackedFs);

    }

    /**
     * Gets the CWD file's id, if not exists return null.
     */
    private static String cwdNameToId(String fileName) {
        File filePath = join(CWD, fileName);
        if (filePath.exists()) {
            String contents = readContentsAsString(filePath);
            return sha1(contents, fileName);
        }
        return null;
    }

    private static void printConditionFileNames(List<String> Fs) {
        if (Fs != null) {
            String[] FsArr = Fs.toArray(new String[0]);
            Arrays.sort(FsArr);
            for (String name : FsArr) {
                System.out.println(name);
            }
            System.out.println();
        }
    }

    private static void printBranches() {
        System.out.println("=== Branches ===");
        List<String> branches = plainFilenamesIn(heads_DIR);
        if (branches != null) {
            for (String name : branches) {
                if (name.equals(curBranchName)) {
                    System.out.print("*");
                }
                System.out.println(name);
            }
            System.out.println();
        }
    }


    /**
     * 1.java gitlet.Main checkout -- [file name]
     * <p>
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * <p>
     */
    public static void checkout_fileName(String fileName) {
        notInitializedError();
        Commit curCommit = Commit.getHeadCommit();
        String id = curCommit.NameToIdInMapping(fileName);
        if (id == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File src = join(blobs_DIR, id);
        File cwdPath = join(CWD, fileName);
        writeContents(cwdPath, readContentsAsString(src));
    }
    /**
     * 2.java gitlet.Main checkout [commit id] -- [file name]
     * <p>
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * <p>
     */
    public static void checkout_idAndFileName(String commitId, String fileName) {
        notInitializedError();
        Commit tarCommit = getTarCommitFromId(commitId);
        String id = tarCommit.NameToIdInMapping(fileName);
        if (id == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        OverwriteError(fileName);
        File src = join(blobs_DIR, id);
        File cwdPath = join(CWD, fileName);
        writeContents(cwdPath, readContentsAsString(src));
    }

    private static String AbbToFull(String abbId) {
        char[] abbs = abbId.toCharArray();
        List<String> commitIds = plainFilenamesIn(commits_DIR);
        for (String fullId : commitIds) {
            boolean flag = true;
            char[] temps = fullId.toCharArray();
            for (int i = 0; i < abbs.length; i++) {
                if (abbs[i] != temps[i]) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                return fullId;
            }
        }
        return null;
    }

    /**
     * According to the given id, which may be abbreviated, returns the target commit.
     */
    private static Commit getTarCommitFromId(String commitId) {
        commitId = AbbToFull(commitId);
        if (commitId == null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File tarCommitPath = join(commits_DIR, commitId);
        if (!tarCommitPath.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return readObject(tarCommitPath, Commit.class);
    }
    /**
     * Failure cases:
     * <p>
     * If the file does not exist in the previous commit, abort,
     * printing the error message File does not exist in that commit. Do not change the CWD.
     * <p>
     * If no commit with the given id exists, print No commit with that id exists.
     * Otherwise, if the file does not exist in the given commit,
     * print the same message as for failure case 1. Do not change the CWD.
     * <p>
     * If no branch with that name exists, print No such branch exists.
     * If that branch is the current branch, print No need to checkout the current branch.
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;
     * perform this check before doing anything else. Do not change the CWD.
     */

    /**
     * 3.java gitlet.Main checkout [branch name]
     * <p>
     * Takes all files in the commit at the head of the given branch, and puts them in the working directory,
     * overwriting the versions of the files that are already there if they exist. Also, at the end of this command,
     * the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
     * <p>
     */
    public static void checkout_branchName(String branchName) {
        notInitializedError();
        List<String> branchNames = plainFilenamesIn(heads_DIR);
        if (branchName.equals(curBranchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        if (branchNames == null || !branchNames.contains(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        Commit tarCommit = Commit.getHeadCommitOfBranch(branchName);
        if (tarCommit == null) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        UntrackedError(tarCommit);
        ClearCurTrackedCWD();
        AddTarTrackedCWD(tarCommit);

        ClearStaging();
        SetCurBranch(branchName);
        writeContents(HEAD, tarCommit.getId());
    }

    /**
     * Add the files which are tracked by the target Commit to CWD
     */
    private static void AddTarTrackedCWD(Commit tarCommit) {
        for (Map.Entry<String, String> entry : tarCommit.getIdToName().entrySet()) {
            File cwdPathCur = join(CWD, entry.getValue());
            writeContentFromFile(join(blobs_DIR, entry.getKey()), cwdPathCur);
        }
    }

    /**
     * Clear the tracked files in CWD.
     */
    private static void ClearCurTrackedCWD() {
        Commit curCommit = Commit.getHeadCommit();
        for (Map.Entry<String, String> entry : curCommit.getIdToName().entrySet()) {
            File cwdPathCur = join(CWD, entry.getValue());
            cwdPathCur.delete();
        }
    }

    /**
     * If a working file is untracked in the current branch and would be overwritten by the checkout,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;
     */
    private static void UntrackedError(Commit tarCommit) {
        List<String> untrackedFNs = getUntrackedFileNames(); //file name not id
        for (String name : tarCommit.getIdToName().values()) {
            if (untrackedFNs.contains(name)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private static void SetCurBranch(String branchName) {
        //Change the current branch
        curBranchName = branchName;
        Main.serialized.setCurBranchName(curBranchName);
    }

    private static void ClearStaging() {
        //Clear the staging area.
        clearStagingArea(add_DIR);
        Blob.getAddFiles().clear();
        clearStagingArea(rm_DIR);
        Blob.getRmFiles().clear();
    }

    /**
     * Delete the files in the area.
     */
    private static void clearStagingArea(File area) {
        List<String> fileIds = plainFilenamesIn(area);
        if (fileIds != null) {
            for (String id : fileIds) {
                join(area, id).delete();
            }
        }
    }
    private static List<String> getUntrackedFileNames() {
        List<String> untrackedFs = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(CWD)));
        Commit curCommit = Commit.getHeadCommit();

        for (Map.Entry<String, String> entry : curCommit.getIdToName().entrySet()) {
            String fileName = entry.getValue();
            String cwdId = cwdNameToId(fileName);
            if (cwdId != null && !Blob.getAddFiles().containsKey(cwdId)) {
                untrackedFs.remove(fileName);
            }
        }

        for (Map.Entry<String, String> entry : Blob.getAddFiles().entrySet()) {
            String fileName = entry.getValue();
            String cwdId = cwdNameToId(fileName);
            if (cwdId != null) {
                untrackedFs.remove(fileName);
            }
        }
        return untrackedFs;
    }


    /**
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch
     * <p>
     * If a branch with the given name does not exist, aborts. Print the error message
     * A branch with that name does not exist.
     * <p>
     * If you try to remove the branch you’re currently on, aborts, printing the error message
     * Cannot remove the current branch.
     */
    public static void rm_branch(String tarBranchName) {
        notInitializedError();
        File tarBranch = join(heads_DIR, tarBranchName);
        if (!tarBranch.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (tarBranchName.equals(curBranchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        tarBranch.delete();
    }

    /**
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * <p>
     * Also moves the current branch’s head to that commit node.
     * <p>
     * The [commit id] may be abbreviated as for checkout.
     * <p>
     * The staging area is cleared.
     * <p>
     * Failure cases:
     * If no commit with the given id exists,
     * print No commit with that id exists.
     * <p>
     * If a working file is untracked in the current branch and
     * would be overwritten by the reset,
     * print `There is an untracked file in the way; delete it, or add and commit it first.`
     * and exit; perform this check before doing anything else.
     */
    public static void reset(String commitId) {
        notInitializedError();
        Commit tarCommit = getTarCommitFromId(commitId);
        UntrackedError(tarCommit);
        ClearCurTrackedCWD();
        AddTarTrackedCWD(tarCommit);
        ClearStaging();
        writeContents(HEAD, tarCommit.getId());
    }

    /**
     * Merges files from the given branch into the current branch.
     * <p>
     * If the split point is the same commit as the given branch, then we do nothing;
     * the merge is complete, and the operation ends with the message Given branch is an ancestor of the current branch.
     * <p>
     * If the split point is the current branch, then the effect is to check out the given branch,
     * and the operation ends after printing the message Current branch fast-forwarded.
     * <p>
     * Otherwise:
     * <p>
     * 1. Any files that have been modified in the given branch since the split point,
     * but not modified in the current branch since the split point should be changed to
     * their versions in the given branch (checked out from the commit at the front of the given branch).
     * These files should then all be automatically staged.
     * To clarify, if a file is “modified in the given branch since the split point”
     * this means the version of the file as it exists in the commit at the front of
     * the given branch has different content from the version of the file at the split point.
     * Remember: blobs are content addressable!
     * <p>
     * 2. Any files that have been modified in the current branch but not in the given branch
     * since the split point should stay as they are.
     * <p>
     * 3. Any files that have been modified in both the current and given branch in the same way
     * (i.e., both files now have the same content or were both removed) are left unchanged
     * by the merge. If a file was removed from both the current and given branch,
     * but a file of the same name is present in the working directory,
     * it is left alone and continues to be absent (not tracked nor staged) in the merge.
     * <p>
     * 4. Any files that were not present at the split point and are present
     * only in the current branch should remain as they are.
     * <p>
     * 5. Any files that were not present at the split point and are present
     * only in the given branch should be checked out and staged.
     * <p>
     * 6. Any files present at the split point, unmodified in the current branch,
     * and absent in the given branch should be removed (and untracked).
     * <p>
     * 7. Any files present at the split point, unmodified in the given branch,
     * and absent in the current branch should remain absent.
     * <p>
     * <p>
     * Failure cases:
     * <p>
     * If there are staged additions or removals present,
     * print the error message You have uncommitted changes. and exit.
     * <p>
     * If a branch with the given name does not exist,
     * print the error message A branch with that name does not exist.
     * <p>
     * If attempting to merge a branch with itself,
     * print the error message Cannot merge a branch with itself.
     * <p>
     * If merge would generate an error because the commit that it does has no changes in it,
     * just let the normal commit error message for this go through.
     * <p>
     * If an untracked file in the current commit would be overwritten or deleted by the merge,
     * print There is an untracked file in the way; delete it, or add and commit it first. and exit;
     * perform this check before doing anything else.
     */
    public static void merge(String branchName) {
        notInitializedError();

        Commit splitPoint = getSplitPoint(branchName);
        Commit cur = Commit.getHeadCommit();
        Commit bran = Commit.getHeadCommitOfBranch(branchName);
        if (bran == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        UntrackedError(bran);

        if (Blob.getAddFiles().size() + Blob.getRmFiles().size() > 0) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        if (cur.getId().equals(bran.getId())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        } else {
            if (splitPoint.getId().equals(bran.getId())) {
                System.out.println("Given branch is an ancestor of the current branch.");
            } else if (splitPoint.getId().equals(cur.getId())) {
                checkout_branchName(branchName);
                System.out.println("Current branch fast-forwarded.");
            } else {
                boolean isConflicted = false;
                //Exists in the split point
                for (Map.Entry<String, String> entry : splitPoint.getIdToName().entrySet()) {
                    String fileName = entry.getValue();
                    String fileId = entry.getKey();

                    if (cur.hasBlob(fileId)) {
                        //Not modified in cur
                        if (bran.hasFile(fileName) && !bran.hasBlob(fileId)) {
                            //Exists in bran but modified
                            checkout_idAndFileName(bran.getId(), fileName);
                            add(fileName);
                        } else if (!bran.hasFile(fileName)) {
                            //Not exists in bran
                            rm(fileName);
                        }
                    }
                }

                //Exists in the bran
                for (Map.Entry<String, String> entry : bran.getIdToName().entrySet()) {
                    String fileName = entry.getValue();
                    String fileId = entry.getKey();
                    File cwdFile = join(CWD, fileName);

                    if (!splitPoint.hasFile(fileName)) {
                        //Not exists in the split point
                        if (!cur.hasFile(fileName)) {
                            //Not exists in the cur
                            checkout_idAndFileName(bran.getId(), fileName);
                            add(fileName);
                        }
                    }

                    //CONFLICTED
                    if (!splitPoint.hasFile(fileName)) {
                        //Absent in split point
                        if (cur.hasFile(fileName) && !cur.hasBlob(fileId)) {
                            //File exists in cur, but different with that in bran
                            String branCon = getContents(fileName, bran);
                            String curCon = getContents(fileName, cur);
                            writeContents(cwdFile, mergeContents(curCon, branCon));
                            add(fileName);
                            isConflicted = true;
                        }
                    } else if (!splitPoint.hasBlob(fileId)) {
                        //Original in split point, different from bran
                        if (cur.hasFile(fileName) && !cur.hasBlob(fileId) && !cur.hasBlob(splitPoint.NameToIdInMapping(fileName))) {
                            //File exists in cur, but different with that in bran and split point
                            String branCon = getContents(fileName, bran);
                            String curCon = getContents(fileName, cur);
                            writeContents(cwdFile, mergeContents(curCon, branCon));
                            add(fileName);
                            isConflicted = true;
                        } else if (!cur.hasFile(fileName)) {
                            String branCon = getContents(fileName, bran);
                            String curCon = "";
                            writeContents(cwdFile, mergeContents(curCon, branCon));
                            add(fileName);
                            isConflicted = true;
                        }
                    }
                }

                for (Map.Entry<String, String> entry : cur.getIdToName().entrySet()) {
                    String fileName = entry.getValue();
                    String fileId = entry.getKey();
                    File cwdFile = join(CWD, fileName);

                    if (!bran.hasFile(fileName) && splitPoint.hasFile(fileName) && !splitPoint.hasBlob(fileId)) {
                        //absent in bran, modified in cur, original in split point
                        String branCon = "";
                        String curCon = getContents(fileName, cur);
                        writeContents(cwdFile, mergeContents(curCon, branCon));
                        add(fileName);
                        isConflicted = true;
                    }
                }

                MergeCommit("Merged " + branchName + " into " + curBranchName + ".", bran.getId(), branchName);
                if (isConflicted) {
                    System.out.println("Encountered a merge conflict.");
                }
            }
        }
    }

    private static String mergeContents(String curCon, String branCon) {
        return "<<<<<<< HEAD\n" + curCon + "=======\n" + branCon + ">>>>>>>\n";
    }

    /**
     * returns the content of the file tracked by the commit
     */
    private static String getContents(String fileName, Commit commit) {
        String fileId = commit.NameToIdInMapping(fileName);
        if (fileId != null) {
            //file exists
            String contents = readContentsAsString(join(blobs_DIR, fileId));
            return contents;
        } else {
            return null;
        }
    }

    //todo : 1.BFS one of the heads. 2.BFS the other find the first
    //Wrong!!!
    private static Commit getSplitPoint(String branchName) {
        Commit cur = Commit.getHeadCommit();
        String curId = cur.getId();
        Commit bran = Commit.getHeadCommitOfBranch(branchName);
        if (bran == null) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String branId = bran.getId();
        while (!branId.equals(curId)) {
            cur = (cur.parentCommit() == null) ? Commit.getHeadCommitOfBranch(branchName) : cur.parentCommit();
            bran = (bran.parentCommit() == null) ? Commit.getHeadCommit() : bran.parentCommit();
            curId = cur.getId();
            branId = bran.getId();
        }
        return readObject(join(commits_DIR, curId), Commit.class);
    }

    private static void OverwriteError(String fileName) {
        List<String> untrackedFNs = getUntrackedFileNames(); //file name not id
        if (untrackedFNs.contains(fileName)) {
            System.out.println("There is an untracked file in the way; " +
                    "delete it, or add and commit it first.");
            System.exit(0);
        }
    }
}
