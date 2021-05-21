package com.sberstart.affid.banksystem;

import com.sberstart.affid.banksystem.config.ApplicationContext;
import com.sberstart.affid.banksystem.config.Bean;
import com.sberstart.affid.banksystem.controller.Controller;
import com.sberstart.affid.banksystem.controller.handler.AbstractHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Application {

    private final Map<String, Constructor<? extends AbstractHandler>> handlerConstructorMap = new HashMap<>();
    private final Map<String, Constructor<? extends Controller>> controllerConstructorMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        Application application = new Application();
        application.run();
    }

    public void run() throws IOException {
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(8541), 5);
        try {
            ApplicationContext ctx = new ApplicationContext(Paths.get("src/main/resources/config.xml"));
            ctx.load();
            Map<String, AbstractHandler> handlers = ctx.getHandlers();
            for (Map.Entry<String, AbstractHandler> handlerInst : handlers.entrySet()) {
                System.out.println("CREATED CONTEXT: " + server.createContext(handlerInst.getKey(), handlerInst.getValue()).getPath());
            }
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            System.out.println("SERVER Started");
        } catch (ClassNotFoundException e) {
            System.out.println("CLASS NOT FOUND: " + e.getMessage());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("INSTANTIATION ERROR: " + e.getMessage());
        } catch (ClassCastException e) {
            System.out.println(e.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }


    }
}
