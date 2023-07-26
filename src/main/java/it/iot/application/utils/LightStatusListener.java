package it.iot.application.utils;

public interface LightStatusListener {
    void onLightsStatusUpdated(int lightsOnCount, int lightsOffCount);
}

