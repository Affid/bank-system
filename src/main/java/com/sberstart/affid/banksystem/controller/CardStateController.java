package com.sberstart.affid.banksystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.json.schemas.ChangeCardStateBody;
import com.sberstart.affid.banksystem.service.BankService;
import com.sberstart.affid.banksystem.validation.CardBodyValidator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import javax.management.InstanceNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import static com.sberstart.affid.banksystem.controller.Responses.*;

public class CardStateController extends Controller {
    @Override
    public void get(HttpExchange exchange) throws IOException {

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
            sendResponse(415, exchange);
            return;
        }
        try {
            ChangeCardStateBody body = mapper.readValue(exchange.getRequestBody(), ChangeCardStateBody.class);
            if(!CardBodyValidator.validateCardNum(body.getId())){
                sendResponse(400, String.format(BAD_NUMBER, "card"), exchange);
                return;
            }
            BankService service = new BankService(DataSource.getConnection());
            Optional<String> card;
            switch (body.getState()){
                case 1:
                    card = service.activateCard(body.getId());
                    break;
                case 3:
                    card = service.closeCard(body.getId());
                    break;
                default:
                    sendResponse(400, BAD_CONTENT, exchange);
                    return;
            }
            if(!card.isPresent()){
                sendResponse(500,exchange);
                return;
            }
            sendResponse(200, card.get(), exchange);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            sendResponse(500, exchange);
        } catch (InstanceNotFoundException | IllegalStateException e) {
            sendResponse(400, e.getMessage(), exchange);
        }
    }
}
