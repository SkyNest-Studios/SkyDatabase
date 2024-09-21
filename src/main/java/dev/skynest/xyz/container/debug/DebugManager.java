package dev.skynest.xyz.container.debug;

import dev.skynest.xyz.utils.AsyncManager;

import java.util.EnumMap;
import java.util.concurrent.TimeUnit;

public class DebugManager {

    private final EnumMap<Type, Integer> operationsPerSecond = new EnumMap<>(Type.class);

    private final AsyncManager saves;
    public DebugManager() {
        this.saves = new AsyncManager(1, "saves");
        saves.runTaskTimer(this::resetOperationsPerSecond, 1, 1, TimeUnit.SECONDS);
        for (Type type : Type.values()) operationsPerSecond.put(type, 0);
    }

    public synchronized void push(Type type) {
        operationsPerSecond.put(type, operationsPerSecond.get(type) + 1);
        System.out.println("Doing " + operationsPerSecond.get(type) + "/s - Type: " + type);
    }


    private synchronized void resetOperationsPerSecond() {
        for (Type type : Type.values()) {
            operationsPerSecond.put(type, 0);
        }
    }

    public enum Type {
        SAVE,
        REMOVE,
        UPDATE;
    }
}
