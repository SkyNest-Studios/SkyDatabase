package dev.skynest.xyz.interfaces;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

public interface IQuery<T extends IData> {


    // For get ALL datas in only one connection
    List<T> getDatas(Connection connection);
    void setDatas(List<T> list, Connection connection);

    // Remove only one user
    void remove(String name, Connection connection);
    void removeMultiple(List<String> names, Connection connection);
    // Remove all datas for database
    void clear(Connection connection);


    // Set only one player
    void set(String name, T data, Connection connection);
    // Replace
    default void replace(T toReplace, T theReplace, Connection connection) {
        remove(toReplace.getName(), connection);
        setDatas(Arrays.asList(theReplace), connection);
    }

    // Create table
    void createTable(Connection connection);

}
