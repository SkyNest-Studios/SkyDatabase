package dev.skynest.xyz.user.query;

import dev.skynest.xyz.interfaces.IQuery;
import dev.skynest.xyz.user.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseQuery implements IQuery<UserData> {

    @Override
    public List<UserData> getDatas(Connection connection) {
        List<UserData> players = new ArrayList<>();
        String query = "SELECT * FROM users";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                int money = rs.getInt("money");
                players.add(new UserData(name, money));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }

    @Override
    public void setDatas(List<UserData> list, Connection connection) {
        String query = "INSERT INTO users (name, money) VALUES (?, ?) ON DUPLICATE KEY UPDATE money =?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            for (UserData user : list) {
                stmt.setString(1, user.getName());
                stmt.setInt(2, user.getMoney());
                stmt.setInt(3, user.getMoney());
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String name, Connection connection) {
        String query = "DELETE FROM users WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clear(Connection connection) {
        String query = "DELETE FROM users";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void set(String name, UserData data, Connection connection) {
        String query = "UPDATE users SET money = ? WHERE name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, data.getMoney());
            stmt.setString(2, name);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTable(Connection connection) {
        String query = "CREATE TABLE IF NOT EXISTS users (name VARCHAR(255) PRIMARY KEY, money INT)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
