package com.sberstart.affid.banksystem.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sberstart.affid.banksystem.controller.Controller;
import com.sberstart.affid.banksystem.controller.UrlParams;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.json.schemas.CardGetBody;
import com.sberstart.affid.banksystem.service.BankService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Optional;

import static com.sberstart.affid.banksystem.controller.Responses.*;

public class CardRootController extends Controller {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("CARD ROOT");
        super.handle(exchange);
    }

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
                sendResponse(404, "", exchange);
                return;
            }
            String id = optionalId.get();
            try (BankService service = new BankService(DataSource.getConnection())) {
                sendResponseIfPresent(exchange, service.getCardInfo(id));
            } catch (SQLException e) {
                sendResponse(500, "", exchange);
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
            String id = mapper.readValue(exchange.getRequestBody(), CardGetBody.class).getId();
            try (BankService service = new BankService(DataSource.getConnection())) {
                sendResponseIfPresent(exchange, service.getCardInfo(id));
            } catch (SQLException e) {
                sendResponse(500, exchange);
            }
        } catch (JsonParseException e) {
            sendResponse(400, BAD_FORMATTING, exchange);
        } catch (JsonMappingException e) {
            sendResponse(400, BAD_CONTENT, exchange);
        }
    }
}
