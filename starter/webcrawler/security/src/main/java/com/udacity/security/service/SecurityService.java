package com.udacity.security.service;

import com.google.inject.Inject;
import com.udacity.image.service.ImageService;
import com.udacity.image.service.ImageAnalysisResult;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.model.AlarmStatus;
import com.udacity.security.model.ArmingStatus;
import com.udacity.security.model.Sensor;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that processes security events and manages the overall state of the security system.
 */
public class SecurityService {
    private final SecurityRepository securityRepository;
    private final ImageService imageService;
    private boolean catDetected = false;

    @Inject
    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Change the activation status of a sensor and update system status accordingly.
     */
    public void changeSensorActivationStatus(Sensor sensor, boolean active) {
        // If alarm already active, ignore sensor state changes
        if (securityRepository.getAlarmStatus() == AlarmStatus.ALARM) {
            return;
        }

        // Activation requested
        if (active) {
            // sensor becoming active now
            if (!sensor.getActive()) {
                handleSensorActivated();
            } else {
                // sensor already active; if system pending, escalate to alarm
                if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM) {
                    securityRepository.setAlarmStatus(AlarmStatus.ALARM);
                }
            }
        } else {
            // Deactivation requested
            if (sensor.getActive()) {
                handleSensorDeactivated();
            } else {
                // If sensor already inactive, but system is pending and all sensors inactive,
                // return to NO_ALARM (some callers may re-check state by calling deactivate)
                if (securityRepository.getAlarmStatus() == AlarmStatus.PENDING_ALARM && allSensorsInactive()) {
                    securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
                }
            }
            // if sensor already inactive, do nothing
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }

    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return;
        }
        
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> securityRepository.setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    private void handleSensorDeactivated() {
        switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> {
                if(allSensorsInactive()) {
                    securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
                }
            }
        }
    }

    /**
     * Process an image from the security camera.
     */
    public void processImage(byte[] currentCameraImage) {
        ImageAnalysisResult result = imageService.analyzeImage(currentCameraImage);
        boolean containsCat = result != null && result.getDetectedObjects().containsKey("cat");
        catDetected = containsCat;

        if (containsCat && securityRepository.getArmingStatus() == ArmingStatus.ARMED_HOME) {
            securityRepository.setAlarmStatus(AlarmStatus.ALARM);
        } else if (!containsCat && !anySensorActive()) {
            securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        }
    }

    /**
     * Change the arming status of the system and update sensors and alarm accordingly.
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            securityRepository.setAlarmStatus(AlarmStatus.NO_ALARM);
        } else {
            resetSensors();
            if(catDetected && armingStatus == ArmingStatus.ARMED_HOME) {
                securityRepository.setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    private boolean anySensorActive() {
        return securityRepository.getSensors().stream().anyMatch(Sensor::getActive);
    }

    private boolean allSensorsInactive() {
        return securityRepository.getSensors().stream().noneMatch(Sensor::getActive);
    }

    private void resetSensors() {
        securityRepository.getSensors().forEach(sensor -> changeSensorActivationStatus(sensor, false));
    }
}