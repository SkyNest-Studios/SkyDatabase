package dev.skynest.xyz.exeptions;

public class DatabaseArgsWrong extends Exception{

    public DatabaseArgsWrong() {
        super("Database args are invalid! (Auth, Query, UserManipulator are required)");
    }

}
