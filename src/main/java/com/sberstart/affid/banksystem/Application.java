package com.sberstart.affid.banksystem;

import com.sberstart.affid.banksystem.config.ApplicationContext;
import com.sberstart.affid.banksystem.controller.Controller;
import com.sberstart.affid.banksystem.controller.handler.DefaultHandler;
import com.sberstart.affid.banksystem.dao.DataSource;
import com.sun.net.httpserver.HttpServer;
import org.h2.tools.RunScript;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {

    public static void main(String[] args) throws IOException, SQLException{
        Application application = new Application();
        application.run(8541);
    }

    public void run(int port) throws IOException, SQLException{
        createDataBase();
        Logger.getLogger("BankServer").log(Level.INFO,"DATABASE CREATED");
        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 5);
        try {
            ApplicationContext ctx = new ApplicationContext(Paths.get("src/main/resources/config.xml"));
            ctx.load();
            Map<String, DefaultHandler> handlers = ctx.getHandlers();
            for (Map.Entry<String, DefaultHandler> handlerInst : handlers.entrySet()) {
                Logger.getLogger("BankServer").log(Level.INFO,"CREATED CONTEXT: " +
                        server.createContext(handlerInst.getKey(), handlerInst.getValue()).getPath());
            }
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
            Logger.getLogger("BankServer").log(Level.INFO,"SERVER Started on port " + port);
        } catch (ClassNotFoundException e) {
            Logger.getLogger("BankServer").log(Level.WARNING,"CLASS NOT FOUND: " + e.getMessage());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            Logger.getLogger("BankServer").log(Level.WARNING,"INSTANTIATION ERROR: " + e.getMessage());
        } catch (ClassCastException e) {
            Logger.getLogger("BankServer").log(Level.WARNING,"CONFIGURATION ERROR: " + e.getMessage());
        } catch (RuntimeException e) {
            Logger.getLogger("BankServer").log(Level.WARNING, "RUNTIME ERROR" + e.getMessage());
        }
    }

    private void createDataBase() throws IOException, SQLException {
        Scanner scanner = new Scanner(new FileInputStream("src/main/resources/initializeDB/sequence.txt"));
        Connection con = DataSource.getConnection();
        while (scanner.hasNextLine()){
            String script = scanner.nextLine();
            FileReader reader = new FileReader(script);
            RunScript.execute(con, reader);
            reader.close();
            Logger.getLogger("BankServer").log(Level.INFO,script + " : EXECUTED");
        }
        scanner.close();
    }
}
