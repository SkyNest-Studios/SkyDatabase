package dev.skynest.xyz.container.tmp.models.cvs;

import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRecord;
import de.siegmar.fastcsv.writer.CsvWriter;
import dev.skynest.xyz.container.DatabaseContainer;
import dev.skynest.xyz.container.debug.DebugManager;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.interfaces.TMP;
import dev.skynest.xyz.utils.AsyncManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class TMPCVSReplace<T extends IData> implements TMP<T> {

    private String patch;
    private IDataManipulator<T> userManipulator;
    private DatabaseContainer<T> databaseContainer;
    private List<String> lines = new ArrayList<>();
    private boolean enabledAsync;
    private boolean enabledDebug;
    private AsyncManager async;
    private DebugManager debug;

    @Override
    public List<String> load(String patch, IDataManipulator<T> userManipulator, DatabaseContainer<T> databaseContainer, AsyncManager async, DebugManager debug, boolean enabledAsync, boolean enabledDebug) {
        this.patch = patch;
        this.userManipulator = userManipulator;
        this.databaseContainer = databaseContainer;
        this.enabledAsync = enabledAsync;
        this.enabledDebug = enabledDebug;
        this.async = async;
        this.debug = debug;

        loadLinesFromFile();
        return processLoadedLines();
    }

    @Override
    public void exit() {
        File lastFile = getLastCSVFile();
        if (lastFile != null && lastFile.exists()) {
            lastFile.delete();
        }
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
        File csvFile = getLastCSVFile();
        if (csvFile != null) {
            try (CsvReader<CsvRecord> reader = CsvReader.builder().ofCsvRecord(csvFile.toPath())) {
                lines.clear();
                reader.forEach(record -> lines.add(record.getField(0)));
            } catch (IOException e) {
                System.err.println("Error loading lines from file: " + e.getMessage());
            }
        }
    }

    private List<String> processLoadedLines() {
        List<String> toRemove = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("REMOVE ")) {
                toRemove.add(line.substring(7));
            } else {
                T user = userManipulator.fromString(line);
                if (user != null) {
                    databaseContainer.getDatas().put(user.getName(), user);
                }
            }
        }

        return toRemove;
    }

    private File getLastCSVFile() {
        File dir = new File(patch);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("Failed to create directory: " + patch);
            return null;
        }

        File[] csvFiles = dir.listFiles((d, name) -> name.startsWith("tmp-") && name.endsWith(".csv"));
        return (csvFiles != null && csvFiles.length > 0) ? getLatestFile(csvFiles) : createNewCsvFile(dir);
    }

    private File getLatestFile(File[] files) {
        File latest = files[0];
        for (File file : files) {
            if (file.lastModified() > latest.lastModified()) {
                latest = file;
            }
        }
        return latest;
    }

    private File createNewCsvFile(File dir) {
        File newCsvFile = new File(dir, "tmp-" + System.currentTimeMillis() + ".csv");
        try {
            if (newCsvFile.createNewFile()) {
                System.out.println("Created new temporary CSV file: " + newCsvFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Error creating temporary CSV file: " + e.getMessage());
        }
        return newCsvFile;
    }

    private void markAsRemoved(String playerName) {
        if (enabledAsync) {
            async.run(() -> markAsRemovedInternal(playerName));
        } else {
            markAsRemovedInternal(playerName);
        }
    }

    private void saveToTmpFile(T user) {
        if (enabledAsync) {
            async.run(() -> saveToTmpFileInternal(user));
        } else {
            saveToTmpFileInternal(user);
        }
    }

    private void removeMarkAsRemoved(String playerName) {
        lines.removeIf(line -> line.equals("REMOVE " + playerName));
        if (enabledDebug) {
            debug.push(DebugManager.Type.UPDATE);
        }
    }

    private void saveToTmpFileInternal(T user) {
        removeMarkAsRemoved(user.getName());

        String userString = userManipulator.inString(user);
        if (!lines.contains(userString)) {
            lines.add(userString);
        }
        saveAllLinesToFile();

        if (enabledDebug) {
            debug.push(DebugManager.Type.SAVE);
        }
    }

    private void markAsRemovedInternal(String playerName) {
        String removeEntry = "REMOVE " + playerName;
        if (!lines.contains(removeEntry)) {
            lines.add(removeEntry);
        }
        saveAllLinesToFile();

        if (enabledDebug) {
            debug.push(DebugManager.Type.REMOVE);
        }
    }

    private void saveAllLinesToFile() {
        File csvFile = getLastCSVFile();
        if (csvFile == null) return;

        try (CsvWriter writer = CsvWriter.builder().build(csvFile.toPath(), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (String line : lines) {
                writer.writeRecord(line.split(","));
            }
        } catch (IOException e) {
            System.err.println("Error saving lines to CSV file: " + e.getMessage());
        }
    }
}
