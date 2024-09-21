package dev.skynest.xyz.exeptions;

public class DatabaseNotArealLoaded extends Exception{

    public DatabaseNotArealLoaded() {
        super("The database is not total loaded, please wait.");
    }

}
