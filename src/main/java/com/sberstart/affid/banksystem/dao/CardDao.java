package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Card;
import com.sberstart.affid.banksystem.model.CardState;
import com.sberstart.affid.banksystem.model.PaymentSystem;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import javax.management.InstanceAlreadyExistsException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CardDao {

    private static final DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd").toFormatter();
    private static final String ALL = "SELECT * FROM CARD;";
    private static final String BY_ID = "SELECT * FROM CARD WHERE ID = ?;";
    private static final String BY_ACC = "SELECT * FROM CARD WHERE ACCOUNT_ID = ?;";
    private static final String ADD = "INSERT INTO Card (pay_system, bank_id, id, cvv, account_id, validity, control, balance, state)" +
            " values (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String ACTIVATE = "UPDATE CARD SET STATE = 1 WHERE ID = ?;";
    private static final String CLOSE = "UPDATE CARD SET STATE = 3 WHERE ID = ?;";
    private static final String BY_ID_ACCOUNT = "SELECT ACCOUNT_ID FROM CARD WHERE ID = ?;";
    private static final String FILL = "UPDATE CARD SET BALANCE = BALANCE + ? WHERE ID = ?;";

    private final Connection connection;
    private boolean isClosed;

    public CardDao(Connection connection) {
        this.connection = connection;
        this.isClosed = false;
    }

    public Optional<Card> add(int paySystem, String bankId, String id,
                       String cvv, long accountId, String validity, int control, BigDecimal balance, int state) throws InstanceAlreadyExistsException {
        try (PreparedStatement stat = connection.prepareStatement(ADD)) {
            stat.setInt(1, paySystem);
            stat.setString(2, bankId);
            stat.setString(3, id);
            stat.setString(4, cvv);
            stat.setLong(5, accountId);
            stat.setString(6, validity);
            stat.setInt(7, control);
            stat.setBigDecimal(8, balance);
            stat.setInt(9, state);
            stat.executeUpdate();
            return getById(id);
        } catch (JdbcSQLIntegrityConstraintViolationException e) {
            throw new InstanceAlreadyExistsException("Card already exists");
        } catch (SQLException e) {
            for (Throwable t : e) {
                t.printStackTrace();
            }
            return Optional.empty();
        }

    }

    public Optional<Card> activate(String id) {
        try (PreparedStatement stat = connection.prepareStatement(ACTIVATE)) {
            stat.setString(1, id);
            int count = stat.executeUpdate();
            if (count != 1) {
                return Optional.empty();
            }
            return getById(id);
        } catch (SQLException e) {
            for(Throwable t: e){
                t.printStackTrace();
            }
            return Optional.empty();
        }
    }

    public Optional<Card> closeCard(String id){
        try (PreparedStatement stat = connection.prepareStatement(CLOSE)) {
            stat.setString(1, id);
            int count = stat.executeUpdate();
            if (count != 1) {
                return Optional.empty();
            }
            return getById(id);
        } catch (SQLException e) {
            for(Throwable t: e){
                t.printStackTrace();
            }
            return Optional.empty();
        }
    }

    public List<Card> getAll() {
        List<Card> cards = new ArrayList<>();
        try (Statement stat = connection.createStatement();
             ResultSet result = stat.executeQuery(ALL)) {
            while (result.next()) {
                String id = result.getString("ID");
                cards.add(readCard(result, id));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return cards;
    }

    public List<Card> getByAccount(long accId) {
        List<Card> cards = new ArrayList<>();
        try (PreparedStatement stat = connection.prepareStatement(BY_ACC)) {
            stat.setLong(1, accId);
            ResultSet result = stat.executeQuery();
            while (result.next()) {
                String id = result.getString("ID");
                cards.add(readCard(result, id));
            }
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return cards;
    }

    public Optional<Card> getById(String id) {
        try (PreparedStatement stat = connection.prepareStatement(BY_ID)) {
            stat.setString(1, id);
            ResultSet result = stat.executeQuery();
            if (result.next()) {
                return Optional.of(readCard(result, id));
            }
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return Optional.empty();
    }

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

    public boolean refill(String number, BigDecimal amount){
        try (PreparedStatement cardFill = connection.prepareStatement(FILL);
            PreparedStatement account = connection.prepareStatement(BY_ID_ACCOUNT)) {
            cardFill.setBigDecimal(1, amount);
            cardFill.setString(2, number);
            return (cardFill.executeUpdate() == 1);
        } catch (SQLException throwables) {
            for (Throwable t : throwables) {
                t.printStackTrace();
            }
        }
        return false;
    }


    private Card readCard(ResultSet result, String id) throws SQLException {
        String bankId = result.getString("BANK_ID");
        PaymentSystem system = PaymentSystem.of(result.getInt("PAY_SYSTEM"));
        int control = result.getInt("CONTROL");
        String cvv = result.getString("CVV");
        String account = result.getString("ACCOUNT_ID");
        LocalDate validity = LocalDate.parse(result.getString("VALIDITY"), format);
        BigDecimal balance = result.getBigDecimal("BALANCE");
        CardState state = CardState.values()[result.getInt("STATE") - 1];
        Card card = new Card(id, system, bankId, control, cvv, account, validity, state);
        card.plusBalance(balance);
        return card;
    }
}
