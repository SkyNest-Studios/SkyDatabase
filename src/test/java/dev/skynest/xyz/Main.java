package dev.skynest.xyz;

import dev.skynest.xyz.graph.GraphGUI;
import dev.skynest.xyz.user.UserData;
import dev.skynest.xyz.user.query.DatabaseQuery;
import dev.skynest.xyz.database.auth.Auth;
import dev.skynest.xyz.user.manipulator.UserManipulator;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {


    public static void main(String[] args) {
        UserManipulator userManipulator = new UserManipulator();
        SkyDatabase<UserData> skyDatabase = new SkyDatabase<>(
                new Auth("sd", "localhost", 3306, "root", ""),
                new DatabaseQuery(),
                userManipulator
        );


        //UserData defaultUser = skyDatabase.getOrCreate("test1");
        //defaultUser.setMoney(1);
        //skyDatabase.save(defaultUser);

        Scanner scanner = new Scanner(System.in);
        String command;

        while (true) {
            System.out.print("> ");
            command = scanner.nextLine().trim();

            String[] parts = command.split(" ");
            String action = parts[0].toLowerCase();

            // 5fc3d85f-56e5-4813-b1fc-ec9f845f5c52

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
                    if (parts.length > 1) {
                        String userName = parts[1];
                        UserData newUser = skyDatabase.getOrCreate(userName);
                        skyDatabase.save(newUser);
                        System.out.println("Created data: " + userManipulator.inString(newUser));
                    } else {
                        System.out.println("Insufficient arguments. Usage: create <username>");
                    }
                    break;
                case "edit":
                    if (parts.length > 2) {
                        String userName = parts[1];
                        UserData newUser = skyDatabase.get(userName);
                        newUser.setMoney(Integer.parseInt(parts[2]));
                        skyDatabase.save(newUser);
                        System.out.println("Edited data: " + userManipulator.inString(newUser));
                    } else {
                        System.out.println("Insufficient arguments. Usage: edit <username>");
                    }
                    break;
                case "delete":
                    if (parts.length > 1) {
                        String userName = parts[1];
                        skyDatabase.remove(userName);
                        System.out.println("Deleted successfully!");
                    } else {
                        System.out.println("Insufficient arguments. Usage: delete <username>");
                    }
                    break;

                case "get":
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
                    break;

                case "list":
                    for (UserData data : skyDatabase.get()) {
                        System.out.println(data.getName());
                    }
                    break;
                case "loaded":
                    System.out.println(skyDatabase.isLoaded());
                    break;
                case "test":
                    if (parts.length > 1) {
                        int testnum = Integer.parseInt(parts[1]);
                        List<UUID> uuids = new ArrayList<>(testnum);
                        List<Long> saveTimes = new ArrayList<>();
                        List<Long> removeTimes = new ArrayList<>();

                        System.out.println("Loading uuid...");
                        for (int i = 0; i < testnum; i++) {
                            uuids.add(UUID.randomUUID());
                        }
                        System.out.println("Testing...");

                        long startTime = System.currentTimeMillis();
                        // Test per save
                        for (int i = 0; i < testnum; i++) {
                            skyDatabase.getOrCreate(uuids.get(i).toString());
                            long endTime = System.currentTimeMillis();
                            saveTimes.add(endTime - startTime);
                        }
                        // 4d474d2a-5f17-4513-a474-e665f5591eed
                        long startTime2 = System.currentTimeMillis();
                        // Test per remove
                        for (int i = 0; i < testnum; i++) {
                            skyDatabase.remove(uuids.get(i).toString());
                            long endTime = System.currentTimeMillis();
                            removeTimes.add(endTime - startTime2);
                        }

                        System.out.println("Test completati.");
                        SwingUtilities.invokeLater(() -> new GraphGUI(saveTimes, removeTimes).setVisible(true));
                    } else {
                        System.out.println("Insufficient arguments. Usage: test <power>");
                    }
                    break;


                default:
                    System.out.println("Unknown command: " + command);
                    break;
            }
        }
    }




}
