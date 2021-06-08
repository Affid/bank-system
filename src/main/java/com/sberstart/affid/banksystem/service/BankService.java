package com.sberstart.affid.banksystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.sberstart.affid.banksystem.controller.Responses;
import com.sberstart.affid.banksystem.dao.AccountDao;
import com.sberstart.affid.banksystem.dao.CardDao;
import com.sberstart.affid.banksystem.dao.ClientDao;
import com.sberstart.affid.banksystem.dao.PassportDao;
import com.sberstart.affid.banksystem.json.AccountSerializer;
import com.sberstart.affid.banksystem.json.CardSerializer;
import com.sberstart.affid.banksystem.json.ClientSerializer;
import com.sberstart.affid.banksystem.json.PassportSerializer;
import com.sberstart.affid.banksystem.json.schemas.CreateAccountBody;
import com.sberstart.affid.banksystem.json.schemas.CreateCardBody;
import com.sberstart.affid.banksystem.model.*;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.relation.InvalidRelationIdException;
import java.io.Closeable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BankService implements Closeable {

    private final Connection connection;
    private final ObjectMapper mapper;
    private final CardDao cardDao;
    private final PassportDao passportDao;
    private final AccountDao accountDao;
    private final ClientDao clientDao;

    private boolean disableAutoCommit() {
        try {
            connection.setAutoCommit(false);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean enableAutoCommit() {
        try {
            connection.setAutoCommit(true);
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean startTransaction() {
        return disableAutoCommit();
    }

    private boolean stopTransaction() {
        try {
            connection.commit();
            return enableAutoCommit();
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean interruptTransaction() {
        try {
            connection.rollback();
            return enableAutoCommit();
        } catch (SQLException e) {
            return false;
        }
    }

    public BankService(Connection connection) {
        this.connection = connection;
        this.cardDao = new CardDao(connection);
        this.passportDao = new PassportDao(connection);
        this.accountDao = new AccountDao(connection, cardDao);
        this.clientDao = new ClientDao(connection, passportDao, accountDao);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Passport.class, new PassportSerializer());
        module.addSerializer(Card.class, new CardSerializer());
        module.addSerializer(Account.class, new AccountSerializer());
        module.addSerializer(Client.class, new ClientSerializer());
        this.mapper = new ObjectMapper();
        mapper.registerModule(module);
    }

    public Optional<String> createCard(CreateCardBody card) throws InvalidRelationIdException, InstanceAlreadyExistsException {
        String number = card.getNumber();
        int paySystem = Integer.parseInt(number.substring(0, 1));
        String bankId = number.substring(1, 6);
        String id = number.substring(6, 15);
        int control = Integer.parseInt(number.substring(15, 16));
        String cvv = card.getCvv();
        String accountNum = card.getAccount();
        Optional<Account> account = getAccountByParsedNum(accountNum);
        if (!account.isPresent()) {
            throw new InvalidRelationIdException("Account doesn't exist");
        }
        long accountId = account.get().getId();
        String validity = card.getValidity();
        BigDecimal balance = account.get().getBalance();
        CardState state = CardState.PROCESS;
        Optional<Card> createdCard = cardDao.add(paySystem, bankId, id, cvv, accountId, validity, control, balance, state.ordinal() + 1);
        if (!createdCard.isPresent()) {
            return Optional.empty();
        }
        try {
            String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createdCard.get());
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public boolean refillAccount(String number, BigDecimal amount) {
        Optional<Account> account = getAccountByParsedNum(number);
        return account.filter(value -> accountDao.refill(value.getId(), amount)).isPresent();
    }

    public boolean refillCard(String number, BigDecimal amount) {
        startTransaction();
        cardDao.refill(number.substring(6,15), amount);
        Optional<Card> card = cardDao.getById(number.substring(6,15));
        if (card.isPresent() && accountDao.refill(Long.parseLong(card.get().getAccount()), amount)) {
            stopTransaction();
            return true;
        }
        interruptTransaction();
        return false;
    }

    public boolean refillPhone(String number, BigDecimal amount) {
        Optional<Client> clientOptional = clientDao.getByPhone(number);
        if(!clientOptional.isPresent()){
            return false;
        }
        Client client = clientOptional.get();
        if(client.getAccounts().size() > 0){
            return accountDao.refill(client.getAccounts().get(0).getId(), amount);
        }
        return false;
    }

    public Optional<String> closeCard(String number) throws InstanceNotFoundException {
        String id = number.substring(6, 15);
        Optional<Card> card = cardDao.getById(id);
        if (!card.isPresent()) {
            throw new InstanceNotFoundException(String.format(Responses.NOT_EXISTS, "Card"));
        }
        Card card1 = card.get();
        if (card1.getState().equals(CardState.CLOSED)) {
            throw new IllegalStateException(String.format(Responses.CARD_ALREADY_STATE, "closed"));
        }
        card = cardDao.closeCard(id);
        if (!card.isPresent()) {
            return Optional.empty();
        }
        card1 = card.get();
        try {
            String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(card1);
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<String> activateCard(String number) throws InstanceNotFoundException {
        String id = number.substring(6, 15);
        Optional<Card> card = cardDao.getById(id);
        if (!card.isPresent()) {
            throw new InstanceNotFoundException(String.format(Responses.NOT_EXISTS, "Card"));
        }
        Card card1 = card.get();
        if (card1.getState().equals(CardState.CLOSED)) {
            throw new IllegalStateException(String.format(Responses.CARD_ALREADY_STATE, "closed"));
        }
        if (card1.getState().equals(CardState.ACTIVE)) {
            throw new IllegalStateException(String.format(Responses.CARD_ALREADY_STATE, "activated"));
        }
        card = cardDao.activate(id);
        if (!card.isPresent()) {
            return Optional.empty();
        }
        card1 = card.get();
        try {
            String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(card1);
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<String> getPassportInfo(String id) {
        try {
            Optional<Passport> optionalPassport = passportDao.get(id);
            if (optionalPassport.isPresent()) {
                Passport passport = optionalPassport.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(passport);
                return Optional.of(response);
            }
        } catch (JsonProcessingException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getCardInfo(String id) {
        id = id.substring(6, 15);
        try {
            Optional<Card> optionalCard = cardDao.getById(id);
            if (optionalCard.isPresent()) {
                Card card = optionalCard.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(card);
                return Optional.of(response);
            }
        } catch (JsonProcessingException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getCardsByAccount(long id) {
        try {
            List<Card> cards = cardDao.getByAccount(id);
            StringBuilder builder = new StringBuilder();
            ArrayNode node = mapper.createArrayNode();
            for (Card card : cards) {
                RawValue value = new RawValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(card));
                node.addRawValue(value);
            }
            return Optional.of(node.toPrettyString());
        } catch (JsonProcessingException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getAccount(long id) {
        try {
            Optional<Account> optionalAccount = accountDao.getById(id);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
                return Optional.of(response);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getClient(long id) {
        try {
            Optional<Client> optionalClient = clientDao.getById(id);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(client);
                return Optional.of(response);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getClient(String phone) {
        try {
            Optional<Client> optionalClient = clientDao.getByPhone(phone);
            if (optionalClient.isPresent()) {
                Client client = optionalClient.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(client);
                return Optional.of(response);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<String> getAccountInfo(String accountNum) {
        try {
            Optional<Account> optionalAccount = getAccountByParsedNum(accountNum);
            if (optionalAccount.isPresent()) {
                Account account = optionalAccount.get();
                String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(account);
                return Optional.of(response);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<Account> getAccountByParsedNum(String accountNum) {
        String accountB = accountNum.substring(0, 5);
        String currency = accountNum.substring(5, 8);
        int key = Integer.parseInt(accountNum.substring(8, 9));
        String division = accountNum.substring(9, 13);
        String personalAcc = accountNum.substring(13, 20);
        return accountDao.getByNum(accountB, currency, key, division, personalAcc);
    }

    public Optional<String> createAccount(CreateAccountBody body) throws InvalidRelationIdException, InstanceAlreadyExistsException {
        String accountNum = body.getAccount();
        String accountB = accountNum.substring(0, 5);
        String currency = accountNum.substring(5, 8);
        int key = Integer.parseInt(accountNum.substring(8, 9));
        String division = accountNum.substring(9, 13);
        String personalAcc = accountNum.substring(13, 20);
        Optional<Client> owner = clientDao.getByPhone(body.getOwner());
        if (!owner.isPresent()) {
            throw new InvalidRelationIdException(String.format(Responses.NOT_EXISTS, "Client"));
        }
        Client client = owner.get();
        Optional<Account> createdAcc = accountDao.add(accountB, currency, key, division, personalAcc, client.getId());
        if (!createdAcc.isPresent()) {
            return Optional.empty();
        }
        try {
            String response = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(createdAcc.get());
            return Optional.of(response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Optional<String> getClientCards(String phone) {
        try {
            Optional<Client> optionalClient = clientDao.getByPhone(phone);
            if (optionalClient.isPresent()) {
                return getClientCards(optionalClient.get());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private Optional<String> getClientCards(Client client) throws JsonProcessingException {
        ArrayNode array = mapper.createArrayNode();
        for (Account account : client.getAccounts()) {
            ObjectNode acc = array.addObject();
            acc.put("account", account.getAccount());
            ArrayNode node = acc.putArray("cards");
            for (Card card : account.getCards()) {
                node.addRawValue(new RawValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(card)));
            }
        }
        String response = array.toPrettyString();
        return Optional.of(response);
    }

    public Optional<String> getClientCards(long id) {
        try {
            Optional<Client> optionalClient = clientDao.getById(id);
            if (optionalClient.isPresent()) {
                return getClientCards(optionalClient.get());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void close() {
        cardDao.close();
        passportDao.close();
        accountDao.close();
    }

}
