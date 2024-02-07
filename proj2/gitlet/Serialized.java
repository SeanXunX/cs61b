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
    public Serialized() {
        addFiles = new HashMap<>();
        rmFiles = new HashMap<>();
    }
    public HashMap<String, String> getAddFiles() {
        return addFiles;
    }
    public HashMap<String, String> getRmFiles() {
        return rmFiles;
    }
}
