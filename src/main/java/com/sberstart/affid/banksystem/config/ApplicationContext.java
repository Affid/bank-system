package com.sberstart.affid.banksystem.config;

import com.sberstart.affid.banksystem.controller.Controller;
import com.sberstart.affid.banksystem.controller.handler.DefaultHandler;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ApplicationContext {
    private final Map<String, Constructor<? extends DefaultHandler>> handlerConstructorMap = new HashMap<>();
    private final Map<String, Constructor<? extends Controller>> controllerConstructorMap = new HashMap<>();

    private final Path config;

    private List<HandlerBean> handlerBeans;

    private List<ControllerBean> controllerBeans;

    public ApplicationContext(Path path) {
        this.config = path;
        this.handlerBeans = new ArrayList<>();
        this.controllerBeans = new ArrayList<>();
    }

    public void load() {
        try {
            Serializer serializer = new Persister();

            BeanConfig beanConfig = serializer.read(BeanConfig.class, config.toFile());

            handlerBeans = beanConfig.getHandlerBeans();
            controllerBeans = beanConfig.getControllerBeans();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<HandlerBean> getHandlerBeans() {
        return handlerBeans;
    }

    private List<ControllerBean> getControllerBeans() {
        return controllerBeans;
    }

    public Map<String, DefaultHandler> getHandlers() throws ClassNotFoundException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Map<String, ControllerBean> controllerBeans = new HashMap<>();
        for (ControllerBean bean : this.controllerBeans) {
            controllerBeans.put(bean.getId(), bean);
        }
        Map<String, DefaultHandler> handlers = new HashMap<>();
        for (HandlerBean bean : this.handlerBeans) {
            DefaultHandler handler = createHandler(bean);
            for (Map.Entry<String, String> control : bean.getControllers().entrySet()) {
                Controller controller = createController(controllerBeans.get(control.getValue()).getControllerClass());
                handler.registerController(control.getKey(), controller);
            }
            handlers.put(bean.getPath(), handler);
        }
        return handlers;
    }

    private DefaultHandler createHandler(HandlerBean bean) throws ClassNotFoundException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        String handler = bean.getHandlerClass();
        if (handlerConstructorMap.containsKey(handler)) {
            Logger.getLogger("BankServer").log(Level.INFO,"USE CASHED CONSTRUCTOR: " + handler);
            return handlerConstructorMap.get(handler).newInstance();
        } else {
            Class<?> klass = Class.forName(handler);
            if (DefaultHandler.class.isAssignableFrom(klass)) {
                Class<? extends DefaultHandler> clazz = klass.asSubclass(DefaultHandler.class);
                Constructor<? extends DefaultHandler> constructor =
                        (Constructor<? extends DefaultHandler>) clazz.getDeclaredConstructors()[0];
                DefaultHandler o = constructor.newInstance();
                handlerConstructorMap.put(handler, constructor);
                return o;
            } else {
                throw new ClassCastException("INCOMPATIBLE TYPES: " +
                        DefaultHandler.class.getName() + " and  " + handler);
            }
        }
    }

    private Controller createController(String name) throws InvocationTargetException,
            InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (controllerConstructorMap.containsKey(name)) {
            Logger.getLogger("BankServer").log(Level.INFO,"USE CASHED CONSTRUCTOR: " + name);
            return controllerConstructorMap.get(name).newInstance();
        } else {
            Class<?> klass = Class.forName(name);
            if (Controller.class.isAssignableFrom(klass)) {
                Class<? extends Controller> clazz = klass.asSubclass(Controller.class);
                Constructor<? extends Controller> constructor = (Constructor<? extends Controller>) clazz.getDeclaredConstructors()[0];
                Controller o = constructor.newInstance();
                controllerConstructorMap.put(name, constructor);
                return o;
            } else {
                throw new ClassCastException("INCOMPATIBLE TYPES: " + Controller.class.getName() + " and  " + name);
            }
        }
    }
}
