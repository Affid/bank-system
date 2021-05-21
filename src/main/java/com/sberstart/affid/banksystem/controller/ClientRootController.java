package com.sberstart.affid.banksystem.controller;

import com.sberstart.affid.banksystem.controller.UrlParams;
import com.sberstart.affid.banksystem.controller.handler.AbstractHandler;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.service.BankService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Optional;

import static com.sberstart.affid.banksystem.controller.Responses.*;

public class ClientRootController extends Controller {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        System.out.println("CLIENT ROOT");
        super.handle(exchange);
    }

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
            long id = Long.parseLong(optionalId.get());
            try (BankService service = new BankService(DataSource.getConnection())) {
                sendResponseIfPresent(exchange, service.getClient(id));
            } catch (SQLException e) {
                sendResponse(500, "", exchange);
            }
        }catch (MalformedURLException e){
            sendResponse(400, e.getMessage(), exchange);
        }
    }

    public void post(HttpExchange exchange) throws IOException {
        sendResponse(204, exchange);
    }
}
