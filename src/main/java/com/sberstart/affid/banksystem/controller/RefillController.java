package com.sberstart.affid.banksystem.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.json.schemas.RefillBody;
import com.sberstart.affid.banksystem.model.Account;
import com.sberstart.affid.banksystem.service.BankService;
import com.sberstart.affid.banksystem.validation.AccountBodyValidator;
import com.sberstart.affid.banksystem.validation.CardBodyValidator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sberstart.affid.banksystem.controller.Responses.*;

public class RefillController extends Controller{

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            post(exchange);
        } else {
            notAllowed(exchange);
        }
    }

    @Override
    public void post(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getRequestHeaders();
        ObjectMapper mapper = new ObjectMapper();
        if (!headers.containsKey(CONTENT_TYPE) || headers.containsKey(CONTENT_LENGTH) &&
                headers.getFirst(CONTENT_LENGTH).length() == 0) {
            sendResponse(400, EMPTY, exchange);
            return;
        }
        if (!headers.get(CONTENT_TYPE).contains(JSON_TYPE)) {
            sendResponse(415,  exchange);
            return;
        }
        try {
            RefillBody body = mapper.readValue(exchange.getRequestBody(), RefillBody.class);
            BankService service = new BankService(DataSource.getConnection());
            switch (body.getType()) {
                case "phone":
                    if(!body.getTo().matches("\\d{11}")){
                        sendResponse(400, String.format(BAD_NUMBER, "phone"), exchange);
                        return;
                    }
                    if(service.refillPhone(body.getTo(), body.getAmount())){
                        sendResponse(204,exchange);
                        return;
                    }
                    sendResponse(500, FAIL, exchange);
                    return;
                case "card":
                    if(!CardBodyValidator.validateCardNum(body.getTo())){
                        sendResponse(400, String.format(BAD_NUMBER, "card"), exchange);
                        return;
                    }
                    if (service.refillCard(body.getTo(), body.getAmount())) {
                        sendResponse(204,exchange);
                        return;
                    }
                    sendResponse(500, FAIL, exchange);
                    return;
                case "account":
                    if(!AccountBodyValidator.validateNumber(body.getTo())){
                        sendResponse(400, String.format(BAD_NUMBER, "account"), exchange);
                        return;
                    }
                    if (service.refillAccount(body.getTo(), body.getAmount())) {
                        sendResponse(204,exchange);
                        return;
                    }
                    sendResponse(500, FAIL, exchange);
                    return;
                default:
                    sendResponse(400, BAD_CONTENT, exchange);
                    break;
            }
            service.close();
        } catch (JsonParseException e) {
            sendResponse(400, BAD_FORMATTING, exchange);
        } catch (JsonMappingException e) {
            Logger.getLogger("BankServer").log(Level.WARNING,"JSON MAPPING ERROR");
            sendResponse(400, BAD_CONTENT, exchange);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            sendResponse(500, exchange);
        }
    }


    @Override
    public void get(HttpExchange exchange) throws IOException {

    }
}
