package com.sberstart.affid.banksystem.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bean {
    private final String beanClass;

    private final Map<String, String> properties;

    private final List<String> dependencies;

    public Bean(String beanClass) {
        this.beanClass = beanClass;
        this.dependencies = new ArrayList<>();
        this.properties = new HashMap<>();
    }

    public void put(String key, String value){
        properties.put(key, value);
    }

    public String getProperty(String key){
        return properties.get(key);
    }

    public void addAll(List<String> dep){
        this.dependencies.addAll(dep);
    }

    public void add(String dep){
        this.dependencies.add(dep);
    }

    public String getBeanClass() {
        return beanClass;
    }

    public List<String> getDependencies() {
        return dependencies;
    }
}
