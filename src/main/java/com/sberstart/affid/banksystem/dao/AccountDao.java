package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Card;
import com.sberstart.affid.banksystem.model.Currency;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import javax.management.InstanceAlreadyExistsException;
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
    private static final String BY_NUM = "SELECT * FROM ACCOUNT WHERE " +
            "ACCOUNT_B = ? AND CURRENCY = ? AND KEY = ? AND DIVISION = ? AND PERSONAL_ACC = ?";
    private static final String CREATE = "INSERT INTO ACCOUNT(account_b, currency, key, division, personal_acc, owner, balance) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?);";

    private static final String FILL = "UPDATE ACCOUNT SET BALANCE = BALANCE + ? WHERE ID = ?;";
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

    public Optional<Account> getByNum(String accountB, String currency, int control, String division, String personalAcc) {
        try (PreparedStatement stat = connection.prepareStatement(BY_NUM)) {
            stat.setString(1, accountB);
            stat.setString(2, currency);
            stat.setInt(3, control);
            stat.setString(4, division);
            stat.setString(5, personalAcc);
            ResultSet result = stat.executeQuery();
            if (result.next()) {
                long id = result.getLong("ID");
                return Optional.of(readAccount(result, id));
            }
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return Optional.empty();
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

    public Optional<Account> add(String accountB, String currency, int control, String division,
                                 String personalAcc, long owner)
            throws InstanceAlreadyExistsException {
        try (PreparedStatement stat = connection.prepareStatement(CREATE)) {
            stat.setString(1, accountB);
            stat.setString(2, currency);
            stat.setInt(3, control);
            stat.setString(4, division);
            stat.setString(5, personalAcc);
            stat.setLong(6, owner);
            stat.setBigDecimal(7, BigDecimal.ZERO);
            stat.executeUpdate();
            return getByNum(accountB, currency, control, division, personalAcc);
        } catch (JdbcSQLIntegrityConstraintViolationException e) {
            throw new InstanceAlreadyExistsException("Account already exists");
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return Optional.empty();
    }

    public boolean refill(long number, BigDecimal amount) {
        try (PreparedStatement accountFill = connection.prepareStatement(FILL)) {
            accountFill.setBigDecimal(1, amount);
            accountFill.setLong(2, number);

            return (accountFill.executeUpdate() == 1);
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return false;
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
