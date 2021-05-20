package com.sberstart.affid.banksystem;

import com.sberstart.affid.banksystem.controller.CardController;
import com.sberstart.affid.banksystem.controller.ClientController;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Application {

    public static void main(String[] args) throws IOException {
        Application application = new Application();
        application.run();
    }

    public void run() throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8541), 5);

        HttpContext context = server.createContext("/api/v1/client", new ClientController());
        HttpContext context1 = server.createContext("/api/v1/card", new CardController());
        //context.setAuthenticator(new Auth());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("SERVER Started");
    }
}
