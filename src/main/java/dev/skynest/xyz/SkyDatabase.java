package dev.skynest.xyz;

import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.database.Database;
import dev.skynest.xyz.database.auth.Auth;
import dev.skynest.xyz.exeptions.DatabaseArgsWrong;
import dev.skynest.xyz.exeptions.DatabaseNotArealLoaded;
import dev.skynest.xyz.interfaces.IQuery;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.Type;
import dev.skynest.xyz.listeners.AsyncJoinEvent;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class SkyDatabase<T extends IData> {

    private Auth auth;
    private IQuery<T> query;
    private Database<T> database;
    private DatabaseContainer<T> container;
    private IDataManipulator<T> dataManipulator;
    private Type type;
    private boolean async;

    // Bukkit - facultative | Is only for MC plugin developer
    private Plugin plugin;

    public SkyDatabase(Plugin plugin, Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, String path, Type type, boolean async, boolean debug) {
        this.auth = auth;
        this.query = query;
        this.database = new Database(auth, query);
        this.dataManipulator = dataManipulator;
        this.type = type;

        String finalpath = (plugin != null) ? "./plugins/" + plugin.getName() + "/" + path : path;
        this.container = new DatabaseContainer<>(database, dataManipulator, finalpath, async, debug, type);
        this.plugin = plugin;
        this.async = async;

        if (plugin != null) Bukkit.getPluginManager().registerEvents(new AsyncJoinEvent(container), plugin);
    }

    public SkyDatabase(Plugin plugin, Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, Type type, boolean async, boolean debug) {
        this(plugin, auth, query, dataManipulator, "tmp", type, async, debug);
    }

    public SkyDatabase(Plugin plugin, Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, Type type) {
        this(plugin, auth, query, dataManipulator, "tmp", type, false, false);
    }

    public SkyDatabase(Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, String path, Type type, boolean async, boolean debug) {
        this(null, auth, query, dataManipulator, path, type, async, debug);
    }

    public SkyDatabase(Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, String path, Type type) {
        this(null, auth, query, dataManipulator, path, type, false, false);
    }

    public SkyDatabase(Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, boolean async, boolean debug, Type type) {
        this(null, auth, query, dataManipulator, "./tmp", type, async, debug);
    }

    public SkyDatabase(Auth auth, IQuery<T> query, IDataManipulator<T> dataManipulator, Type type) {
        this(null, auth, query, dataManipulator, "./tmp", type, false, false);
    }




    public void exit() {
        if (checkLoad()) {
            System.out.println("Load check completed. Exiting without further operations.");
            return;
        }
        System.out.println("Closing the container...");
        container.exit();

        System.out.println("Clearing the database...");
        database.clear();

        System.out.println("Setting data in the database...");
        database.setDatas(new ArrayList<>(container.getDatas().values()));

        System.out.println("Closing the database...");
        database.close();

        System.out.println("Exit completed successfully.");
    }

    public void save(T data) {
        if(checkLoad()) return;
        container.save(data);
    }

    public T getOrCreate(String name) {
        if(checkLoad()) return null;

        T data = get(name);
        if(data == null) {
            data = dataManipulator.create(name);
            container.getDatas().put(name, data);
        }
        container.save(data);
        return data;
    }

    public void add(T data) {
        if(checkLoad()) return;

        container.getDatas().remove(data.getName());
        container.getDatas().put(data.getName(), data);
        container.save(data);
    }

    public void remove(String name) {
        if(checkLoad()) return;
        container.remove(get(name));
    }

    public T get(String name) {
        if(checkLoad()) return null;
        return container.getDatas().get(name);
    }

    public List<T> get() {
        if(checkLoad()) return null;
        return new ArrayList<>(container.getDatas().values());
    }

    public boolean isLoaded() {
        return container.isLoaded();
    }

    private boolean checkLoad() {
        if(!isLoaded()) {
            new DatabaseNotArealLoaded().printStackTrace();
            return true;
        }
        return false;
    }

    public static <T extends IData> SkyBuilder<T> builder() {
        return new SkyBuilder<T>();
    }

    public static class SkyBuilder<A extends IData> {

        private Auth auth;
        private IQuery<? extends IData> query;
        private String path;
        private IDataManipulator<? extends IData> dataManipulator;
        private Type type;
        private boolean async;
        private boolean debug;
        private Plugin plugin;

        public SkyBuilder() {
            this.auth = null;
            this.query = null;
            this.path = null;
            this.dataManipulator = null;
            this.type = Type.TXT_APPEND;
            this.async = false;
            this.debug = false;
            this.plugin = null;
        }

        public SkyBuilder<A> auth(Auth auth) {
            this.auth = auth;
            return this;
        }

        public <T extends IData> SkyBuilder<A> query(IQuery<T> query) {
            this.query = query;
            return this;
        }

        public SkyBuilder<A> path(String path) {
            this.path = path;
            return this;
        }

        public <T extends IData> SkyBuilder<A> manipulator(IDataManipulator<T> dataManipulator) {
            this.dataManipulator = dataManipulator;
            return this;
        }

        public SkyBuilder<A> type(Type type) {
            this.type = type;
            return this;
        }

        public SkyBuilder<A> async(boolean async) {
            this.async = async;
            return this;
        }

        public SkyBuilder<A> debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public SkyBuilder<A> plugin(Plugin plugin) {
            this.plugin = plugin;
            return this;
        }

        public SkyDatabase<A> build() {
            if(auth == null || query == null || dataManipulator == null) {
                new DatabaseArgsWrong().printStackTrace();
                return null;
            }

            return plugin == null ? new SkyDatabase<A>(
                    null,
                    auth,
                    (IQuery<A>) query,
                    (IDataManipulator<A>) dataManipulator,
                    path == null ? "./tmp" : path,
                    type,
                    async,
                    debug
            ) : new SkyDatabase<A>(
                    plugin,
                    auth,
                    (IQuery<A>) query,
                    (IDataManipulator<A>) dataManipulator,
                    path == null ? "tmp" : path,
                    type,
                    async,
                    debug
            );
        }

    }

}
