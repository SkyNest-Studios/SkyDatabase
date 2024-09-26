package dev.skynest.xyz.container.tmp.base;

import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.container.debug.DebugManager;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.TMP;
import dev.skynest.xyz.interfaces.Type;
import dev.skynest.xyz.utils.AsyncManager;

import java.util.*;

public class TMPUtils<T extends IData> {

    public final String patch;
    public final IDataManipulator<T> userManipulator;
    public final DatabaseContainer<T> databaseContainer;
    private final AsyncManager async;
    private final boolean asyncMode;
    private final boolean debug;
    private DebugManager saveDebug;
    private TMP<T> tmp;

    public TMPUtils(String patch, IDataManipulator<T> userManipulator, DatabaseContainer<T> databaseContainer, Type type, boolean async, boolean debug) {
        this.patch = patch;
        this.userManipulator = userManipulator;
        this.databaseContainer = databaseContainer;
        this.async = new AsyncManager(1, "tmp-writer");
        this.asyncMode = async;
        this.debug = debug;
        if (debug) {
            this.saveDebug = new DebugManager();
        }
        this.tmp = (TMP<T>) type.getTemporarySystem();
    }

    public List<String> init() {
        return tmp.load(patch, userManipulator, databaseContainer, async, saveDebug, asyncMode, debug);
    }

    public void exit() {
        tmp.exit();
    }

    public void save(T data) {
        tmp.save(data);
    }

    public void remove(T data) {
        tmp.remove(data);
    }


}
