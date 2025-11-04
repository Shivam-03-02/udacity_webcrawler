package com.udacity.security.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.udacity.image.service.ImageAnalysisResult;
import com.udacity.image.service.ImageService;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.model.AlarmStatus;
import com.udacity.security.model.ArmingStatus;
import com.udacity.security.model.Sensor;
import com.udacity.security.model.SensorType;

class SecurityServiceTest {

    private SecurityService securityService;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;

    private Set<Sensor> sensors;
    private Sensor testSensor;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        sensors = new HashSet<>();
        testSensor = new Sensor("Test Sensor", SensorType.DOOR);
        sensors.add(testSensor);
        
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    void whenArmedAndSensorActivated_systemPending() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        
        securityService.changeSensorActivationStatus(testSensor, true);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    void whenArmedAndSensorActivatedAndSystemPending_systemAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        
        securityService.changeSensorActivationStatus(testSensor, true);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenPendingAlarmAndAllSensorsInactive_returnToNoAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        testSensor.setActive(false);
        
        securityService.changeSensorActivationStatus(testSensor, false);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void whenAlarmActive_changeInSensorShouldNotAffectAlarmState() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        
        securityService.changeSensorActivationStatus(testSensor, true);
        securityService.changeSensorActivationStatus(testSensor, false);
        
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void whenSensorActivatedWhileActiveAndPending_changeToAlarmState() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        testSensor.setActive(true);
        
        securityService.changeSensorActivationStatus(testSensor, true);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenSensorDeactivatedWhileInactive_noChangesToAlarmState() {
        testSensor.setActive(false);
        
        securityService.changeSensorActivationStatus(testSensor, false);
        
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    @Test
    void whenCatDetectedAndSystemArmedHome_changeToAlarmState() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Map<String, Float> catMap = Collections.singletonMap("cat", 0.98f);
        when(imageService.analyzeImage(any())).thenReturn(ImageAnalysisResult.builder().setDetectedObjects(catMap).build());
        
        securityService.processImage(new byte[0]);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    void whenNoCatDetectedAndNoSensorsActive_changeToNoAlarm() {
        testSensor.setActive(false);
        when(imageService.analyzeImage(any())).thenReturn(ImageAnalysisResult.builder().setDetectedObjects(Collections.emptyMap()).build());
        
        securityService.processImage(new byte[0]);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(ArmingStatus.class)
    void whenSystemDisarmed_statusNoAlarm(ArmingStatus status) {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    void whenSystemArmed_resetSensorsToInactive() {
        testSensor.setActive(true);
        
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        
        assertFalse(testSensor.getActive());
    }

    @Test
    void whenArmedHomeWithCatDetected_changeToAlarmState() {
        Map<String, Float> catMap2 = Collections.singletonMap("cat", 0.95f);
        when(imageService.analyzeImage(any())).thenReturn(ImageAnalysisResult.builder().setDetectedObjects(catMap2).build());
        
        securityService.processImage(new byte[0]);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }
}