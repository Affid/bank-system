package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Passport;

import java.io.Closeable;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PassportDao implements Closeable {

    private static final String ONE_BY_ID = "SELECT * FROM PASSPORT WHERE ID = ?;";
    private static final String ALL = "SELECT * FROM PASSPORT;";
    private static final DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
    private final Connection connection;
    private boolean isClosed;

    public PassportDao(Connection connection) {
        this.connection = connection;
        this.isClosed = false;
    }

    public Optional<Passport> get(String id) {
        if (isClosed) {
            throw new IllegalStateException("Connection already closed!");
        }
        try (PreparedStatement stat = connection.prepareStatement(ONE_BY_ID)) {
            stat.setString(1, id);
            ResultSet set = stat.executeQuery();
            if (set.next()) {
                return Optional.of(readPassport(set, id));
            }
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public List<Passport> getAll() {
        if (isClosed) {
            throw new IllegalStateException("Connection already closed!");
        }
        List<Passport> passports = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet set = statement.executeQuery(ALL)) {
            while (set.next()) {
                String id = set.getString(1);
                passports.add(readPassport(set, id));
            }
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
        return passports;
    }

    private Passport readPassport(ResultSet set, String id) throws SQLException {
        String lastName = set.getString(2);
        String firstName = set.getString(3);
        String secondName = set.getString(4);
        LocalDate issueDate = LocalDate.parse(set.getString(5), format);
        LocalDate birthDate = LocalDate.parse(set.getString(6), format);
        return new Passport(id, lastName, firstName, secondName, birthDate, issueDate);
    }

    @Override
    public void close() {
        if (isClosed) {
            return;
        }
        try {
            connection.close();
            isClosed = true;
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
        }
    }
}
