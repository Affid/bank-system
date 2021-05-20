package com.sberstart.affid.banksystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.util.RawValue;
import com.sberstart.affid.banksystem.dao.AccountDao;
import com.sberstart.affid.banksystem.dao.CardDao;
import com.sberstart.affid.banksystem.dao.ClientDao;
import com.sberstart.affid.banksystem.dao.PassportDao;
import com.sberstart.affid.banksystem.json.AccountSerializer;
import com.sberstart.affid.banksystem.json.CardSerializer;
import com.sberstart.affid.banksystem.json.ClientSerializer;
import com.sberstart.affid.banksystem.json.PassportSerializer;
import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.model.Card;
import com.sberstart.affid.banksystem.model.Client;
import com.sberstart.affid.banksystem.model.Passport;

import java.io.Closeable;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;

public class BankService implements Closeable {

    private final ObjectMapper mapper;
    private final CardDao cardDao;
    private final PassportDao passportDao;
    private final AccountDao accountDao;
    private final ClientDao clientDao;

    public BankService(Connection connection) {
        cardDao = new CardDao(connection);
        passportDao = new PassportDao(connection);
        accountDao = new AccountDao(connection, cardDao);
        clientDao = new ClientDao(connection, passportDao, accountDao);
        SimpleModule module = new SimpleModule();
        module.addSerializer(Passport.class, new PassportSerializer());
        module.addSerializer(Card.class, new CardSerializer());
        module.addSerializer(Account.class, new AccountSerializer());
        module.addSerializer(Client.class, new ClientSerializer());
        this.mapper = new ObjectMapper();
        mapper.registerModule(module);
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

    @Override
    public void close() {
        cardDao.close();
        passportDao.close();
        accountDao.close();

    }

}
