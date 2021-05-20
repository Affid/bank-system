package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Card;
import com.sberstart.affid.banksystem.model.Currency;

import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountDao implements Closeable {

    private static final DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
    private static final String ALL = "SELECT * FROM ACCOUNT;";
    private static final String BY_ID = "SELECT * FROM ACCOUNT WHERE ID = ?;";
    private static final String BY_OWNER = "SELECT * FROM ACCOUNT WHERE OWNER = ?;";
    private final Connection connection;
    private final CardDao cardDao;
    private boolean isClosed;

    public AccountDao(Connection connection, CardDao cardDao) {
        this.connection = connection;
        this.isClosed = false;
        this.cardDao = cardDao;
    }

    public List<Account> getByOwner(long owner) {
        List<Account> cards = new ArrayList<>();
        try (PreparedStatement stat = connection.prepareStatement(BY_OWNER)) {
            stat.setLong(1, owner);
            ResultSet result = stat.executeQuery();
            while (result.next()) {
                long id = result.getLong("ID");
                cards.add(readAccount(result, id));
            }
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return cards;
    }

    public Optional<Account> getById(long id) {
        try (PreparedStatement stat = connection.prepareStatement(BY_ID)) {
            stat.setLong(1, id);
            ResultSet result = stat.executeQuery();
            if (result.next()) {
                return Optional.of(readAccount(result, id));
            }
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public List<Account> all() {
        List<Account> accounts = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet result = stat.executeQuery(ALL)) {
            while (result.next()) {
                long id = result.getLong("ID");
                accounts.add(readAccount(result, id));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return accounts;
    }

    private Account readAccount(ResultSet result, long id) throws SQLException {
        String balancingAccount = result.getString("ACCOUNT_B");
        Currency currency = Currency.of(result.getInt("CURRENCY"));
        int control = result.getInt("KEY");
        String division = result.getString("DIVISION");
        String personalAcc = result.getString("PERSONAL_ACC");
        String owner = result.getString("OWNER");
        BigDecimal balance = result.getBigDecimal("BALANCE");
        ArrayList<Card> cards = new ArrayList<>(cardDao.getByAccount(id));
        return new Account(balancingAccount, currency, division, control, personalAcc, owner, id, balance, cards);
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        isClosed = true;
    }
}
