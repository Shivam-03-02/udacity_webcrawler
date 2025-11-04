package com.udacity.security.data;

import java.util.Set;

import com.udacity.security.model.AlarmStatus;
import com.udacity.security.model.ArmingStatus;
import com.udacity.security.model.Sensor;

public interface SecurityRepository {
    void addSensor(Sensor sensor);
    void removeSensor(Sensor sensor);
    void updateSensor(Sensor sensor);
    Set<Sensor> getSensors();
    AlarmStatus getAlarmStatus();
    void setAlarmStatus(AlarmStatus status);
    ArmingStatus getArmingStatus();
    void setArmingStatus(ArmingStatus status);
}