package com.sberstart.affid.banksystem.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root(name = "root")
public class BeanConfig {

    @ElementList(name = "controllers", entry = "controller", type = ControllerBean.class, empty = false)
    private List<ControllerBean> controllerBeans;

    @ElementList(name = "handlers", entry = "handler", type = HandlerBean.class, empty = false)
    private List<HandlerBean> handlerBeans;

    public List<ControllerBean> getControllerBeans() {
        return controllerBeans;
    }

    public void setControllerBeans(List<ControllerBean> controllerBeans) {
        this.controllerBeans = controllerBeans;
    }

    public List<HandlerBean> getHandlerBeans() {
        return handlerBeans;
    }

    public void setHandlerBeans(List<HandlerBean> handlerBeans) {
        this.handlerBeans = handlerBeans;
    }
}
