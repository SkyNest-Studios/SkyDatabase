package dev.skynest.xyz.user.manipulator;

import dev.skynest.xyz.interfaces.IDataManipulator;
import dev.skynest.xyz.user.UserData;

public class UserManipulator implements IDataManipulator<UserData> {
    @Override
    public String inString(UserData user) {
        return user.getName() + ";" + user.getMoney();
    }

    @Override
    public UserData fromString(String data) {
        String[] format = data.split(";");
        return new UserData(format[0], Integer.parseInt(format[1]));
    }

    @Override
    public UserData create(String name) {
        return new UserData(name, 1);
    }

}
