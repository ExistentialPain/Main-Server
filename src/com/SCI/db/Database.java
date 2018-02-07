package com.SCI.db;

import java.sql.SQLException;

public interface Database {
    Results execute(String query) throws SQLException;
    Results execute(Query query) throws SQLException;
}
