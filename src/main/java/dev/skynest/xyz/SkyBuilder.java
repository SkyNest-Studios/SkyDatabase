package dev.skynest.xyz;

import dev.skynest.xyz.database.auth.Auth;
import dev.skynest.xyz.exeptions.DatabaseArgsWrong;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.IQuery;
import dev.skynest.xyz.interfaces.Type;
import org.bukkit.plugin.Plugin;

public class SkyBuilder<A extends IData> {

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