package gitlet;

import java.io.Serializable;
import java.util.HashMap;


public class Serialized implements Serializable {

    /**
     * The files stored in the addition.
     * Id to name.
     */
    private HashMap<String, String> addFiles;

    /**
     * The files stored in the removal.
     * Id to name.
     */
    private HashMap<String, String> rmFiles;

    /**
     * Records the curBranchName
     */
    private String curBranchName;

    public Serialized() {
        addFiles = new HashMap<>();
        rmFiles = new HashMap<>();
    }
    public String getCurBranchName() {
        return curBranchName;
    }
    public void setCurBranchName(String curBranchName) {
        this.curBranchName = curBranchName;
    }
    public HashMap<String, String> getAddFiles() {
        return addFiles;
    }
    public HashMap<String, String> getRmFiles() {
        return rmFiles;
    }
}
