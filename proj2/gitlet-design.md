# Gitlet Design Document

**Name**: Sean

## Classes and Data Structures

### Main

This is the entry point to the program. It takes in arguments from the command 
line and based on the command(the first element of the args array) calls the corresponding
command in Repository which will actually execute the logic of the 
command. It also validates the arguments based on the command to ensure that
enough arguments were passed in.

#### Fields
This class has no fields and hence no associated state: it simply
does what reads above.


### Repository

This is where the man logic of our program will live. This file will handle
all the actual commands by reading/writing from/to the correct file, setting up persistence,
and additional error checking.

It will also be responsible for setting up all persistence within gitlet.
This includes creating the .gitlet folder where we store all the folders and files.

#### Fields

1. **static final File CWD = new File(System.getProperty("user.dir"))**
2. **public static final File GITLET_DIR = join(CWD, ".gitlet")**

### Data Structure




## Algorithms

## Persistence

