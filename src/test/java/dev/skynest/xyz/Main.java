package dev.skynest.xyz;

import dev.skynest.xyz.graph.GraphGUI;
import dev.skynest.xyz.interfaces.Type;
import dev.skynest.xyz.user.UserData;
import dev.skynest.xyz.user.query.DatabaseQuery;
import dev.skynest.xyz.database.auth.Auth;
import dev.skynest.xyz.user.manipulator.UserManipulator;

import javax.swing.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static class Profiler {
        private long startTime;
        private MemoryMXBean memoryBean;

        public Profiler() {
            memoryBean = ManagementFactory.getMemoryMXBean();
        }

        public void start() {
            startTime = System.currentTimeMillis();
        }

        public long stop() {
            return System.currentTimeMillis() - startTime;
        }

        public long getUsedMemory() {
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
            return heapMemoryUsage.getUsed() / 1024 / 1024; // Memoria in MB
        }

        public String getMemoryStats() {
            MemoryUsage heapMemoryUsage = memoryBean.getHeapMemoryUsage();
            long usedMemory = heapMemoryUsage.getUsed() / 1024 / 1024;
            long maxMemory = heapMemoryUsage.getMax() / 1024 / 1024;
            return String.format("Memory: %d MB used, %d MB max", usedMemory, maxMemory);
        }
    }

    public static void main(String[] args) {
        UserManipulator userManipulator = new UserManipulator();
        SkyDatabase<UserData> skyDatabase = new SkyDatabase<>(
                new Auth("sd", "localhost", 3306, "root", ""),
                new DatabaseQuery(),
                userManipulator,
                Type.TXT_APPEND
        );

        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");
            command = scanner.nextLine().trim();

            String[] parts = command.split(" ");
            String action = parts[0].toLowerCase();

            switch (action) {
                case "exit":
                    if (parts.length == 2 && parts[1].equalsIgnoreCase("--force")) {
                        break;
                    }
                    skyDatabase.exit();
                    System.out.println("Successful exit!");
                    scanner.close();
                    return;

                case "create":
                    handleCreate(parts, skyDatabase, userManipulator);
                    break;

                case "edit":
                    handleEdit(parts, skyDatabase, userManipulator);
                    break;

                case "delete":
                    handleDelete(parts, skyDatabase);
                    break;

                case "get":
                    handleGet(parts, skyDatabase, userManipulator);
                    break;

                case "list":
                    handleList(skyDatabase);
                    break;

                case "test":
                    handleTest(parts, skyDatabase);
                    break;

                case "profile":
                    handleProfile();
                    break;

                default:
                    System.out.println("Unknown command: " + command);
                    break;
            }
        }
    }

    private static void handleCreate(String[] parts, SkyDatabase<UserData> skyDatabase, UserManipulator userManipulator) {
        if (parts.length > 1) {
            String userName = parts[1];
            UserData newUser = skyDatabase.getOrCreate(userName);
            skyDatabase.save(newUser);
            System.out.println("Created data: " + userManipulator.inString(newUser));
        } else {
            System.out.println("Insufficient arguments. Usage: create <username>");
        }
    }

    private static void handleEdit(String[] parts, SkyDatabase<UserData> skyDatabase, UserManipulator userManipulator) {
        if (parts.length > 2) {
            String userName = parts[1];
            try {
                int money = Integer.parseInt(parts[2]);
                UserData user = skyDatabase.get(userName);
                if (user != null) {
                    user.setMoney(money);
                    skyDatabase.save(user);
                    System.out.println("Edited data: " + userManipulator.inString(user));
                } else {
                    System.out.println("User not found: " + userName);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid money amount. Usage: edit <username> <amount>");
            }
        } else {
            System.out.println("Insufficient arguments. Usage: edit <username> <amount>");
        }
    }

    private static void handleDelete(String[] parts, SkyDatabase<UserData> skyDatabase) {
        if (parts.length > 1) {
            String userName = parts[1];
            skyDatabase.remove(userName);
            System.out.println("Deleted successfully!");
        } else {
            System.out.println("Insufficient arguments. Usage: delete <username>");
        }
    }

    private static void handleGet(String[] parts, SkyDatabase<UserData> skyDatabase, UserManipulator userManipulator) {
        if (parts.length > 1) {
            String userName = parts[1];
            UserData user = skyDatabase.get(userName);
            if (user != null) {
                System.out.println(userManipulator.inString(user));
            } else {
                System.out.println("User not found: " + userName);
            }
        } else {
            System.out.println("Insufficient arguments. Usage: get <username>");
        }
    }

    private static void handleList(SkyDatabase<UserData> skyDatabase) {
        List<UserData> users = skyDatabase.get();
        for (UserData data : users) {
            System.out.println(data.getName());
        }
        System.out.println(users.size() + " users found.");
    }

    private static void handleTest(String[] parts, SkyDatabase<UserData> skyDatabase) {
        if (parts.length > 1) {
            try {
                int testnum = Integer.parseInt(parts[1]);
                Profiler profiler = new Profiler();
                List<UUID> uuids = new ArrayList<>(testnum);

                System.out.println("Generating UUIDs...");
                for (int i = 0; i < testnum; i++) {
                    uuids.add(UUID.randomUUID());
                }

                profiler.start();
                for (UUID uuid : uuids) {
                    skyDatabase.getOrCreate(uuid.toString());
                }
                long saveTime = profiler.stop();
                System.out.println(String.format("Save Time: %d ms (%.2f ms/op)", saveTime, (double) saveTime / testnum));

                profiler.start();
                for (UUID uuid : uuids) {
                    skyDatabase.remove(uuid.toString());
                }
                long deleteTime = profiler.stop();
                System.out.println(String.format("Delete Time: %d ms (%.2f ms/op)", deleteTime, (double) deleteTime / testnum));

                // Memory Profiling
                System.out.println("Memory stats: " + profiler.getMemoryStats());
                System.out.println("Test completati.");

            } catch (NumberFormatException e) {
                System.out.println("Invalid test number. Usage: test <number>");
            }
        } else {
            System.out.println("Insufficient arguments. Usage: test <number>");
        }
    }

    private static void handleProfile() {
        Profiler profiler = new Profiler();
        System.out.println("Profiling memory...");
        System.out.println(profiler.getMemoryStats());
    }
}
