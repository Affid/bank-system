package com.sberstart.affid.banksystem.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sberstart.affid.banksystem.service.BankService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Optional;

public class ClientController extends AbstractController {

    public void get(HttpExchange exchange) throws IOException {
        System.out.println("GET Request accepted..");
        System.out.println("THREAD: " + Thread.currentThread().getName());
        ObjectMapper mapper = new ObjectMapper();
        if (exchange.getRequestHeaders().get("Content-Type").contains("application/json")) {
            JsonNode body = mapper.readTree(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8));
            System.out.println("REQUEST BODY : " + body.toString());
            long id = body.get("id").asLong();
            PrintWriter writer = new PrintWriter(exchange.getResponseBody());
            try (BankService service = new BankService(DataSource.getConnection())) {
                Optional<String> optionalResponse = service.getClient(id);
                sendResponseIfPresent(exchange, optionalResponse);
            } catch (SQLException e) {
                exchange.sendResponseHeaders(500, 0);
                System.out.println("500 INTERNAL ERROR");
            }
            writer.close();
        } else {
            exchange.sendResponseHeaders(415, 0);
            exchange.getResponseBody().close();
            System.out.println("415 UNSUPPORTED MEDIA TYPE");
        }
    }

    public void post(HttpExchange exchange) throws IOException {
        System.out.println("POST METHOD REQUESTED");
        exchange.sendResponseHeaders(204, -1);
        exchange.getResponseBody().close();
    }
}
