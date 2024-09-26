# SkyDatabase

SkyDatabase is an API for managing databases using MySQL, featuring automatic caching and temporary storage in case of crashes. It is developed by **SkyNest Studios**.

### Features:
- Automatic caching
- Temporary storage in case of crashes
- Automatic CRUD (Create, Read, Update, Delete) operations on MySQL databases
- Special constructor function for bukkit plugin developers

### Compatibility
This project is created in Java 8 (1.8) for compatibility with all versions of Java up to 8.

### Why You Should Use SkyDatabase

SkyDatabase's automatic caching system stores data locally, eliminating the need for constant database queries. Instead, it makes one query at the start to load data and another at the end to update it. This ensures that, even when a client is connected to the server, the client's ping or latency will not increase, as there are no continuous queries running throughout the operation.

SkyDatabase is also perfect for developers looking to implement systems such as team management, player statistics tracking, and other related features in their applications.

Moreover, **bukkit plugin developers** benefit from a dedicated function within the constructor, available at the bottom of the file, which allows easy integration and customization specific to their plugins. This makes the API highly adaptable and developer-friendly, providing additional control and flexibility for custom plugin development.

---

## Setup

### 1. Adding SkyDatabase to your project

#### Maven

Add the following repository and dependency to your `pom.xml` file:

```xml
<repository>
    <id>skynest-repo</id>
    <url>https://repo.skynest.xyz/releases</url>
</repository>

<dependency>
    <groupId>dev.skynest.xyz</groupId>
    <artifactId>SkyDatabase</artifactId>
    <version>1.2-BETA</version>
</dependency>
```

#### Gradle

For Gradle, add the following lines to your `build.gradle` file:

```groovy
repositories {
    maven {
        url 'https://repo.skynest.xyz/releases'
    }
}

dependencies {
    implementation 'dev.skynest.xyz:SkyDatabase:1.2-BETA'
}
```

#### Groovy (for Gradle Groovy DSL)

```groovy
repositories {
    maven {
        url 'https://repo.skynest.xyz/releases'
    }
}

dependencies {
    compile group: 'dev.skynest.xyz', name: 'SkyDatabase', version: '1.2-BETA'
}
```

---

## Usage

### 1. Creating a Data Handler for User Data

To use SkyDatabase, you need to define a data handler that implements the `IDataManipulator<T>` interface.

```java
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
```

### 2. Creating a Class for SQL Queries

You need to implement the `IQuery<T>` interface to perform database operations.

```java
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
        String query = "INSERT INTO users (name, money) VALUES (?, ?) ON DUPLICATE KEY UPDATE money = ?";
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
```

### 3. Defining the `UserData` Class

```java
package dev.skynest.xyz.user;

import dev.skynest.xyz.interfaces.IData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserData extends IData {

    private int money;

    public UserData(String name, int money) {
        super(name);
        this.money = money;
    }

    @Override
    protected void save() {
        // create the instance and save this
        // like Main.getInstance().getSkyDatabase().save(this);
    }
}
```

### 4. Initializing the Database

### Constructor

To initialize the `SkyDatabase`, you need to provide the following parameters:
- **Plugin instance** (optional for bukkit plugin developer)
- **Database Authentication Information**
- **Query Implementation**
- **User Manipulator**
- **Async Mode** (optional, default is `false`)
- **Debug Mode** (optional, default is `false`)
- **Temporary Storage Directory** (optional, default is `./tmp`)

Here’s an example of how to initialize `SkyDatabase`:

```java
SkyDatabase<UserData> skyDatabase = new SkyDatabase<>(
    new Auth("sd", "localhost", 3306, "root", ""),  // DB connection
    new DatabaseQuery(),                            // Query implementation
    new UserManipulator(),                          // Data manipulator
    false,                                          // Enable async (Recommended false)
    false,                                          // Enable Debug
    "./tmp"                                         // Temporary storage directory (optional)
);

// Perform operations with SkyDatabase...

// On program exit
skyDatabase.exit();
```

This is the constructor plugin developer (Recommended Constructor)
```java
SkyDatabase<UserData> skyDatabase = new SkyDatabase<>(
        plugin,                                         // Put here the instance of the plugin
        new Auth("sd", "localhost", 3306, "root", ""),  // DB connection
        new DatabaseQuery(),                            // Query implementation
        new UserManipulator()                           // Data manipulator
);

```

Sure! Here's a revised version of your README section that improves clarity and readability for users on GitHub:

---

### 5. Using the Database

You can interact with the database using the following methods:

#### Retrieve or Create User Data
To retrieve user data by ID (e.g., "test"), and create it if it does not exist, use:
```java
UserData userData = skyDatabase.getOrCreate("test");
```

#### Save User Data
To save user data to the database, you can use:
```java
skyDatabase.save(userData);
```
If you have an abstract save method implemented, you can call:
```java
defaultUser.save();
```

#### Remove User Data
To remove user data by username, use:
```java
skyDatabase.remove(userName);
```

#### Retrieve All User Data
To get a list of all user data, use:
```java
List<T> allUsers = skyDatabase.get();
```

#### Retrieve a Single User Data
To retrieve a specific user by ID, use:
```java
T user = skyDatabase.get("id");
```

### Error Handling
If you attempt to create or save data when the database is not fully loaded, an error will occur:
```
DatabaseNotArealLoaded
```

To avoid this error, check if the database is loaded using:
```java
boolean isLoaded = skyDatabase.isLoaded();
```

---

## License

Project developed by **SkyNest Studios**.

Discord: discord.skynest.xyz

---




