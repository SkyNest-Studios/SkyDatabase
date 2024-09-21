package dev.skynest.xyz.interfaces;

public interface IDataManipulator<T extends IData> {

    String inString(T user);
    T fromString(String data);
    T create(String name);

}
