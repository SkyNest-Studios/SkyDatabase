package dev.skynest.xyz.interfaces;

import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.container.debug.DebugManager;
import dev.skynest.xyz.utils.AsyncManager;

import java.util.List;

public interface TMP<T extends IData> {


    List<String> load(String patch, IDataManipulator<T> userManipulator, DatabaseContainer<T> databaseContainer, AsyncManager async, DebugManager debug, boolean enabledAsync, boolean enabledDebug);
    void exit();
    void remove(T data);
    void save(T data);

}
