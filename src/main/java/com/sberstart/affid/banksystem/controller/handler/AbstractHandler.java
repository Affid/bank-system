package com.sberstart.affid.banksystem.controller.handler;

import com.sberstart.affid.banksystem.controller.Controller;
import com.sberstart.affid.banksystem.controller.UrlParams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public abstract class AbstractHandler implements HttpHandler {

    protected HashMap<String, Controller> controllers;

    public AbstractHandler() {
        controllers = new HashMap<>();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().
                    getPath().replace(exchange.getHttpContext().getPath(),"");
            if(!controllers.containsKey(path)){
                exchange.sendResponseHeaders(404,-1);
                exchange.close();
                return;
            }
            Controller controller = controllers.get(path);
            controller.handle(exchange);
    }

    public void registerController(String path, Controller controller){
        controllers.put(path, controller);
    }

    public void removeController(String path){
        controllers.remove(path);
    }


}
