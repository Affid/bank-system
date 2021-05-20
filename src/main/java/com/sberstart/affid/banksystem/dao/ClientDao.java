package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Client;
import com.sberstart.affid.banksystem.model.Passport;

import java.io.Closeable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientDao implements Closeable {
    private final Connection connection;
    private final PassportDao passportDao;
    private final AccountDao accountDao;
    private final String ALL = "SELECT * FROM CLIENT;";
    private final String BY_ID = "SELECT * FROM CLIENT WHERE ID = ?;";
    private boolean isClosed;

    public ClientDao(Connection connection, PassportDao passportDao, AccountDao accountDao) {
        this.connection = connection;
        this.passportDao = passportDao;
        this.accountDao = accountDao;
        this.isClosed = false;
    }

    public Optional<Client> getById(long id) {
        try (PreparedStatement stat = connection.prepareStatement(BY_ID)) {
            stat.setLong(1, id);
            ResultSet resultSet = stat.executeQuery();
            if (resultSet.next()) {
                Optional<Passport> optionalPassport = passportDao.get(resultSet.getString("PASSPORT"));
                if (optionalPassport.isPresent()) {
                    return Optional.of(readClient(resultSet, id, optionalPassport.get()));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Client> getAll() {
        List<Client> clients = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet result = stat.executeQuery(ALL)) {
            while (result.next()) {
                long id = result.getLong("ID");
                Optional<Passport> optionalPassport = passportDao.get(result.getString("PASSPORT"));
                if (optionalPassport.isPresent()) {
                    clients.add(readClient(result, id, optionalPassport.get()));
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return clients;
    }

    private Client readClient(ResultSet result, long id, Passport passport) throws SQLException {
        String phone = result.getString("PHONE");
        ArrayList<Account> accounts = new ArrayList<>(accountDao.getByOwner(id));
        return new Client(id, passport, phone, accounts);
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
