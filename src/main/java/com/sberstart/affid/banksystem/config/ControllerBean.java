package com.sberstart.affid.banksystem.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="controller")
public class ControllerBean {

    @Element(name = "id", type = String.class)
    private String id;

    @Element(name = "class", type = String.class)
    private String controllerClass;

    public String getControllerClass() {
        return controllerClass;
    }

    public void setControllerClass(String controllerClass) {
        this.controllerClass = controllerClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ControllerBean{" +
                "id='" + id + '\'' +
                ", controllerClass='" + controllerClass + '\'' +
                '}';
    }
}
