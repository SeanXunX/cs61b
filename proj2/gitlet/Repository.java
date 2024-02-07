package gitlet;

//import com.google.common.collect.Maps;
//import com.sun.tools.javah.resources.version;
//import net.sf.saxon.trans.SymbolicName;
//import org.checkerframework.checker.units.qual.C;

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
    public static final File stageSerialized = join(GITLET_DIR, "stage", "Serialized");


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

    private static String curBranchName;



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
        if (newBlob.existsInCurCommit()) {
            newBlob.removeFromAdd();
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
        Blob rmBlob = new Blob(fileName);
        if (!rmBlob.existsInAdd() && !rmBlob.existsInCurCommit()) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        } else {
            if (rmBlob.existsInAdd()) {
                rmBlob.removeFromAdd();
            }
            if (rmBlob.existsInCurCommit()) {
                rmBlob.toRm();
                File cwdFile = join(CWD, fileName);
                if (cwdFile.exists()) {
                    restrictedDelete(cwdFile);
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
            curCommit = curCommit.parentCommit();
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
            System.out.println("Merge: " + curCommit.getParent() + " " + curCommit.getSecond_parent());
        }
        printDate(curCommit.getDate());
        System.out.println(curCommit.getMessage());
        System.out.println();
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
        List<String> untrackedFs = plainFilenamesIn(CWD);

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
            return sha1("blob", contents, fileName);
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
        File tarCommitPath = join(commits_DIR, commitId);
        if (!tarCommitPath.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit tarCommit = readObject(tarCommitPath, Commit.class);
        String id = tarCommit.NameToIdInMapping(fileName);
        if (id == null) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File src = join(blobs_DIR, id);
        File cwdPath = join(CWD, fileName);
        writeContents(cwdPath, readContentsAsString(src));
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
        Commit curCommit = Commit.getHeadCommit();
        List<String> untrackedFNs = getUntrackedFileNames();
        if (untrackedFNs.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        //Clear the files which are tracked by the current Commit in CWD
        for (Map.Entry<String, String> entry : curCommit.getIdToName().entrySet()) {
            File cwdPathCur = join(CWD, entry.getValue());
            restrictedDelete(cwdPathCur);
        }
        //Add the files which are tracked by the target Commit to CWD
        for (Map.Entry<String, String> entry : tarCommit.getIdToName().entrySet()) {
            File cwdPathCur = join(CWD, entry.getValue());
            writeContents(cwdPathCur, join(blobs_DIR, entry.getKey()));
        }

        //Clear the staging area.
        clearStagingArea(add_DIR);
        Blob.getAddFiles().clear();
        clearStagingArea(rm_DIR);
        Blob.getRmFiles().clear();

        //Change the current branch
        curBranchName = branchName;
        writeContents(HEAD, tarCommit.getId());
    }

    /**
     * Delete the files in the area.
     */
    private static void clearStagingArea(File area) {
        List<String> fileIds = plainFilenamesIn(area);
        if (fileIds != null) {
            for (String id : fileIds) {
                restrictedDelete(join(area, id));
            }
        }
    }
    private static List<String> getUntrackedFileNames() {
        List<String> untrackedFs = plainFilenamesIn(CWD);
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
        restrictedDelete(tarBranch);
    }

    /**
     *
     */
    public static void reset(String commitId) {

    }


}
