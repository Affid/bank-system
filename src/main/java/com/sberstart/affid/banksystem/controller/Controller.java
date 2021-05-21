package com.sberstart.affid.banksystem.controller;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public abstract class Controller {

    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                get(exchange);
                break;
            case "POST":
                post(exchange);
                break;
            default:
                notAllowed(exchange);
                break;
        }
    }

    public void notAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
        exchange.getResponseBody().close();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected void sendResponseIfPresent(HttpExchange exchange,
                                         Optional<String> optionalResponse) throws IOException {
        if (optionalResponse.isPresent()) {
            sendResponse(200, optionalResponse.get(), exchange);
        } else {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        }
    }

    protected void sendResponse(int rCode, String message, HttpExchange exchange) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(rCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendResponse(int rCode, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(rCode, -1);
        exchange.close();
    }

    public abstract void get(HttpExchange exchange) throws IOException;

    public abstract void post(HttpExchange exchange) throws IOException;

}
