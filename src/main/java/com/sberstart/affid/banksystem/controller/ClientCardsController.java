package com.sberstart.affid.banksystem.controller;

import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.service.BankService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Optional;

import static com.sberstart.affid.banksystem.controller.Responses.BAD_QUERY;

public class ClientCardsController extends Controller {


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            get(exchange);
        } else {
            notAllowed(exchange);
        }
    }

    @Override
    public void get(HttpExchange exchange) throws IOException {
        try {
            UrlParams params = UrlParams.getParams(exchange.getRequestURI());
            if (!params.contains("id") && !params.contains("phone")) {
                sendResponse(400, BAD_QUERY, exchange);
                return;
            }
            if (params.contains("id")) {
                Optional<String> optionalId = params.getFirst("id");
                if (!optionalId.isPresent()) {
                    sendResponse(400, BAD_QUERY, exchange);
                    return;
                }
                long id = Long.parseLong(optionalId.get());
                try (BankService service = new BankService(DataSource.getConnection())) {
                    sendResponseIfPresent(exchange, service.getClientCards(id));
                } catch (SQLException e) {
                    sendResponse(500, exchange);
                }
            } else {
                Optional<String> optionalPhone = params.getFirst("phone");
                if (!optionalPhone.isPresent()) {
                    sendResponse(404, exchange);
                    return;
                }
                try (BankService service = new BankService(DataSource.getConnection())) {
                    sendResponseIfPresent(exchange, service.getClientCards(optionalPhone.get()));
                } catch (SQLException e) {
                    sendResponse(500, exchange);
                }
            }
        } catch (MalformedURLException e) {
            sendResponse(400, e.getMessage(), exchange);
        }
    }

    @Override
    public void post(HttpExchange exchange) throws IOException {

    }
}
