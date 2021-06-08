package com.sberstart.affid.banksystem.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.json.schemas.CreateAccountBody;
import com.sberstart.affid.banksystem.service.BankService;
import com.sberstart.affid.banksystem.validation.AccountBodyValidator;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import javax.management.InstanceAlreadyExistsException;
import javax.management.relation.InvalidRelationIdException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Optional;

import static com.sberstart.affid.banksystem.controller.Responses.*;

public class AccountRootController extends Controller{

    @Override
    public void get(HttpExchange exchange) throws IOException {
        try{
            UrlParams params = UrlParams.getParams(exchange.getRequestURI());
            if (!params.contains("id")) {
                sendResponse(400, BAD_QUERY, exchange);
                return;
            }
            Optional<String> optionalId = params.getFirst("id");
            if (!optionalId.isPresent()) {
                sendResponse(404, exchange);
                return;
            }
            String id = optionalId.get();
            if(!AccountBodyValidator.validateNumber(id)){
                sendResponse(400, String.format(BAD_NUMBER, "account"), exchange);
                return;
            }
            try (BankService service = new BankService(DataSource.getConnection())) {
                sendResponseIfPresent(exchange, service.getAccountInfo(id));
            } catch (SQLException e) {
                sendResponse(500, exchange);
            }
        }catch (MalformedURLException e){
            sendResponse(400, e.getMessage(), exchange);
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
            CreateAccountBody body = mapper.readValue(exchange.getRequestBody(), CreateAccountBody.class);
            AccountBodyValidator validator = new AccountBodyValidator();
            try{
                if(!validator.validate(body)) {
                    sendResponse(400, BAD_CONTENT, exchange);
                    return;
                }
            }catch (IllegalStateException e){
                sendResponse(400, e.getMessage(),exchange);
            }
            BankService service = new BankService(DataSource.getConnection());
            Optional<String> response = service.createAccount(body);
            if(response.isPresent()) {
                sendResponse(201, response.get(), exchange);
            }
            sendResponse(500, exchange);
        } catch (JsonParseException e) {
            sendResponse(400, BAD_FORMATTING, exchange);
        } catch (JsonMappingException e) {
            sendResponse(400, BAD_CONTENT, exchange);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            sendResponse(500, exchange);
        } catch (InstanceAlreadyExistsException e) {
            sendResponse(400, ALREADY_EXISTS, exchange);
        } catch (InvalidRelationIdException e) {
            sendResponse(400, String.format(RELATION_NOT_FOUND, "Account"), exchange);
        }
    }
}
