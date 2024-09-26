package dev.skynest.xyz.container.tmp.models.txt;

import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.container.debug.DebugManager;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.TMP;
import dev.skynest.xyz.utils.AsyncManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TMPTXTReplace<T extends IData> implements TMP<T> {

    private String patch;
    private IDataManipulator<T> userManipulator;
    private DatabaseContainer<T> databaseContainer;
    private List<String> lines;
    private boolean enabledAsync;
    private boolean enabledDebug;
    private AsyncManager async;
    private DebugManager debug;

    @Override
    public List<String> load(String patch, IDataManipulator<T> userManipulator, DatabaseContainer<T> databaseContainer, AsyncManager async, DebugManager debug, boolean enabledAsync, boolean enabledDebug) {
        this.patch = patch;
        this.userManipulator = userManipulator;
        this.databaseContainer = databaseContainer;
        this.lines = new ArrayList<>();
        this.enabledAsync = enabledAsync;
        this.enabledDebug = enabledDebug;
        this.async = async;
        this.debug = debug;

        loadLinesFromFile();
        return loadLastTmpFile();
    }

    @Override
    public void exit() {
        getLastTMP().delete();
    }

    @Override
    public void remove(T data) {
        markAsRemoved(data.getName());
    }

    @Override
    public void save(T data) {
        saveToTmpFile(data);
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

    private File getLastTMP() {
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

    private List<String> loadLastTmpFile() {
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


    private void markAsRemoved(String playerName) {
        if (enabledAsync) async.run(() -> markAsRemoved0(playerName));
        else markAsRemoved0(playerName);
    }

    private void saveToTmpFile(T user) {
        if (enabledAsync) async.run(() -> saveToTmpFile0(user));
        else saveToTmpFile0(user);
    }

    private void removeMarkAsRemoved(String playerName) {
        lines.removeIf(line -> line.equals("REMOVE " + playerName));
        saveLinesToFile();
        if (enabledDebug) debug.push(DebugManager.Type.UPDATE);
    }

    private void saveToTmpFile0(T user) {
        removeMarkAsRemoved(user.getName());

        String userString = userManipulator.inString(user);
        if (!lines.contains(userString)) {
            lines.add(userString);
            saveLinesToFile();
        }
        if (enabledDebug) debug.push(DebugManager.Type.SAVE);
    }

    private void markAsRemoved0(String playerName) {
        String removeEntry = "REMOVE " + playerName;
        if (!lines.contains(removeEntry)) {
            lines.add(removeEntry);
            saveLinesToFile();
        }
        if (enabledDebug) debug.push(DebugManager.Type.REMOVE);
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
