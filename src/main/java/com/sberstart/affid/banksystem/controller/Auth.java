package com.sberstart.affid.banksystem.controller;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.util.HashMap;
import java.util.List;

public class Auth extends Authenticator {

    private final HashMap<String, List<String>> properties;

    public Auth(HashMap<String, List<String>> properties) {
        this.properties = properties;
    }

    @Override
    public Result authenticate(HttpExchange exch) {
        if (exch.getRequestHeaders().containsKey("Authorization")) {
            String auth = exch.getRequestHeaders().getFirst("Authorization");
            if (properties.get(auth).contains(exch.getRequestURI().toString())) {
                return new Success(new HttpPrincipal(auth, properties.get(auth).toString()));
            }
        }
        return new Failure(501);
    }
}
