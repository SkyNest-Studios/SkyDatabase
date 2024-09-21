package dev.skynest.xyz.container.tmp;

import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.container.debug.DebugManager;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.utils.AsyncManager;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TMPUtils<T extends IData> {

    public final String patch;
    public final IDataManipulator<T> userManipulator;
    public final DatabaseContainer<T> databaseContainer;
    private final AsyncManager async;
    private final boolean asyncMode;
    private final boolean debug;
    private DebugManager saveDebug;
    private final List<String> lines;

    public TMPUtils(String patch, IDataManipulator<T> userManipulator, DatabaseContainer<T> databaseContainer, boolean async, boolean debug) {
        this.patch = patch;
        this.userManipulator = userManipulator;
        this.databaseContainer = databaseContainer;
        this.async = new AsyncManager(1, "tmp-writer");
        this.asyncMode = async;
        this.debug = debug;
        this.lines = new ArrayList<>();
        if (debug) {
            this.saveDebug = new DebugManager();
        }
        loadLinesFromFile();
    }

    private void loadLinesFromFile() {
        File tmpFile = getLastTMP();
        if (tmpFile != null) {
            try {
                this.lines.clear();
                this.lines.addAll(Files.readAllLines(tmpFile.toPath()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> loadLastTmpFile() {
        List<String> toRemove = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith("REMOVE ")) {
                toRemove.add(line.replace("REMOVE ", ""));
            } else {
                T user = userManipulator.fromString(line);
                if (user != null) {
                    databaseContainer.getDatas().put(user.getName(), user);
                }
            }
        }
        return toRemove;
    }

    public File getLastTMP() {
        File dir = new File(patch);
        if (!dir.exists()) dir.mkdirs();

        File[] tmpFiles = dir.listFiles((d, name) -> name.startsWith("tmp-") && name.endsWith(".txt"));

        if (tmpFiles != null && tmpFiles.length > 0) {
            File latest = tmpFiles[0];
            for (File file : tmpFiles) {
                if (file.lastModified() > latest.lastModified()) {
                    latest = file;
                }
            }
            return latest;
        } else {
            return createNewTmpFile(dir);
        }
    }

    private File createNewTmpFile(File dir) {
        File newTmpFile = new File(dir, "tmp-" + System.currentTimeMillis() + ".txt");
        try {
            if (newTmpFile.createNewFile()) {
                System.out.println("Created new temporary file: " + newTmpFile.getAbsolutePath());
            } else {
                System.out.println("Temporary file already exists: " + newTmpFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating temporary file: " + e.getMessage());
        }
        return newTmpFile;
    }

    public void markAsRemoved(String playerName) {
        if (asyncMode) async.run(() -> markAsRemoved0(playerName));
        else markAsRemoved0(playerName);
    }

    public void saveToTmpFile(T user) {
        if (asyncMode) async.run(() -> saveToTmpFile0(user));
        else saveToTmpFile0(user);
    }

    public void removeMarkAsRemoved(String playerName) {
        lines.removeIf(line -> line.equals("REMOVE " + playerName));
        saveLinesToFile();
        if (debug) saveDebug.push(DebugManager.Type.UPDATE);
    }

    private void saveToTmpFile0(T user) {
        removeMarkAsRemoved(user.getName());

        String userString = userManipulator.inString(user);
        if (!lines.contains(userString)) {
            lines.add(userString);
            saveLinesToFile();
        }
        if (debug) saveDebug.push(DebugManager.Type.SAVE);
    }

    private void markAsRemoved0(String playerName) {
        String removeEntry = "REMOVE " + playerName;
        if (!lines.contains(removeEntry)) {
            lines.add(removeEntry);
            saveLinesToFile();
        }
        if (debug) saveDebug.push(DebugManager.Type.REMOVE);
    }

    private void saveLinesToFile() {
        File tmpFile = getLastTMP();
        try {
            Files.write(tmpFile.toPath(), lines, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
