package com.udacity.security.model;

public class Sensor {
    private String name;
    private SensorType type;
    private boolean active;

    public Sensor(String name, SensorType type) {
        this.name = name;
        this.type = type;
        this.active = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SensorType getType() {
        return type;
    }

    public void setType(SensorType type) {
        this.type = type;
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}