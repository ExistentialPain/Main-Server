package com.SCI.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQL implements Database {
    static {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public MySQL(String url, String login, String password, String database) throws SQLException {
        connection = DriverManager.getConnection(url, login, password);
        stm = connection.createStatement();
        stm.execute("use " + database);
    }

    @Override
    public synchronized Results execute(String query) throws SQLException {
        stm.execute(query, Statement.RETURN_GENERATED_KEYS);
        return new Results(stm.getResultSet(), stm.getGeneratedKeys());
    }

    @Override
    public Results execute(Query query) throws SQLException {
        return execute(query.toString());
    }

    private Connection connection;
    private Statement stm;
}
