package com.sberstart.affid.banksystem.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.Root;

import java.util.Map;

@Root(name="handler")
public class HandlerBean {

    @Element(name = "id")
    private String id;

    @Element(name = "class")
    private String handlerClass;

    @Element(name = "path")
    private String path;

    @ElementMap(name = "map", key = "path", entry = "entity" ,attribute = true,
            keyType = String.class, valueType = String.class, empty = false)
    private Map<String, String> controllers;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getControllers() {
        return controllers;
    }

    public void setControllers(Map<String, String> controllers) {
        this.controllers = controllers;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "HandlerBean{" +
                "id='" + id + '\'' +
                ", handlerClass='" + handlerClass + '\'' +
                ", path='" + path + '\'' +
                ", controllers=" + controllers +
                '}';
    }
}
