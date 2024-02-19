package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Utils.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File stageSerialized = join(GITLET_DIR, "Serialized");

    public static Serialized serialized;
    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        //Get the serialized
        if (stageSerialized.exists()) {
            serialized = readObject(stageSerialized, Serialized.class);
        } else {
            serialized = new Serialized();
        }

        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                try {
                    Repository.init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add":
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(args, 1);
                Repository.global_log();
                break;
            case "find":
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "checkout":
                //args.length 2 3 4
                switch (args.length) {
                    case 3:
                        if (!args[1].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout_fileName(args[2]);
                        break;
                    case 4:
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                            System.exit(0);
                        }
                        Repository.checkout_idAndFileName(args[1], args[3]);
                        break;
                    case 2:
                        Repository.checkout_branchName(args[1]);
                        break;
                    default:
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                        break;
                }
                break;
            case "branch":
                validateNumArgs(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs(args, 2);
                Repository.rm_branch(args[1]);
                break;
            case "reset":
                validateNumArgs(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                validateNumArgs(args, 2);
                Repository.merge(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
        writeObject(stageSerialized, serialized);
    }

    public static void validateNumArgs(String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
