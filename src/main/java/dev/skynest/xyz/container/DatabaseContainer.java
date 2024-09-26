package dev.skynest.xyz.container;

import dev.skynest.xyz.container.tmp.base.TMPUtils;
import dev.skynest.xyz.database.Database;
import dev.skynest.xyz.exeptions.DatabaseArealLoaded;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.Type;
import dev.skynest.xyz.utils.AsyncManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseContainer<T extends IData> {

    private final AsyncManager async;
    private final Database<T> database;
    private final IDataManipulator<T> userManipulator;
    private final String patch;
    private final TMPUtils<T> tmp;

    @Getter private final Map<String, T> datas;
    @Getter private boolean loaded;

    public DatabaseContainer(Database<T> database, IDataManipulator<T> userManipulator, String patch, boolean async, boolean debug, Type type) {
        this.async = new AsyncManager(1, "operation");
        this.database = database;
        this.userManipulator = userManipulator;
        this.datas = new ConcurrentHashMap<>();
        this.patch = patch;
        this.tmp = new TMPUtils(patch, userManipulator, this, type, async, debug);

        init();
    }

    private void init() {
        if (loaded) {
            new DatabaseArealLoaded().printStackTrace();
            return;
        }

        async.run(() -> {
            List<String> toRemove = tmp.init();
            database.setDatas(new ArrayList<>(datas.values()));

            List<T> dPlayers = database.getDatas();
            dPlayers.forEach(data -> {
                if (!datas.containsKey(data.getName())) {
                    datas.put(data.getName(), data);
                }
            });
            for (String s : toRemove) datas.remove(s);
        }).whenComplete((a, t) -> loaded = true);
    }

    public void exit() {
        tmp.exit();
    }

    public void save(T newUserData) {
        String playerName = newUserData.getName();

        datas.remove(playerName);
        datas.put(playerName, newUserData);

        tmp.save(newUserData);
    }

    public void remove(T data) {
        datas.remove(data.getName());
        tmp.remove(data);
    }

}