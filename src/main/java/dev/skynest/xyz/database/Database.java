package dev.skynest.xyz.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.skynest.xyz.interfaces.IQuery;
import dev.skynest.xyz.interfaces.IData;
import dev.skynest.xyz.database.auth.Auth;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Database<T extends IData> {

    private final Auth auth;
    private final IQuery<T> query;
    private HikariDataSource dataSource;

    public Database(Auth auth, IQuery<T> query) {
        this.auth = auth;
        this.query = query;
        connect();
    }

    private void connect() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(auth.getUrl());
        config.setUsername(auth.getUsername());
        config.setPassword(auth.getPassword());

        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        createTable();
    }

    public List<T> getDatas() {
        try (Connection connection = dataSource.getConnection()) {
            return query.getDatas(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Arrays.asList();
    }

    public void setDatas(List<T> list) {
        try (Connection connection = dataSource.getConnection()) {
            query.setDatas(list, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(String id) {
        try (Connection connection = dataSource.getConnection()) {
            query.remove(id, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void remove(List<String> list) {
        try (Connection connection = dataSource.getConnection()) {
            query.removeMultiple(list, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        try (Connection connection = dataSource.getConnection()) {
            query.clear(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void set(String id, T data) {
        try (Connection connection = dataSource.getConnection()) {
            query.set(id, data, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void replace(T toReplace, T theReplace) {
        try (Connection connection = dataSource.getConnection()) {
            query.replace(toReplace, theReplace, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable() {
        try (Connection connection = dataSource.getConnection()) {
            query.createTable(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}