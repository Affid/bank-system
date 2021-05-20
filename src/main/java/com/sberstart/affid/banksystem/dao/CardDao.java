package com.sberstart.affid.banksystem.dao;

import com.sberstart.affid.banksystem.model.Card;
import com.sberstart.affid.banksystem.model.CardState;
import com.sberstart.affid.banksystem.model.PaymentSystem;

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
    private final Connection connection;
    private boolean isClosed;
    public CardDao(Connection connection) {
        this.connection = connection;
        this.isClosed = false;
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
