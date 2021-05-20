package com.sberstart.affid.banksystem.dao;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DataSource {

    private static final BasicDataSource DATA_SOURCE = createPool();

    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }

    public static BasicDataSource getDataSource() {
        return DATA_SOURCE;
    }


    private static BasicDataSource createPool() {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(Paths.get("src/main/resources/database.properties"))) {
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Pool not initialized");
        }
        String drivers = props.getProperty("jdbc.drivers");
        if (drivers != null) {
            System.setProperty("jdbc.drivers", drivers);
        }
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        BasicDataSource source = new BasicDataSource();
        source.setDriverClassName(drivers);
        source.setUrl(url);
        source.setUsername(username);
        source.setPassword(password);
        return source;
    }
}
