package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.Gps;

import java.util.Map;

public interface GPSService {
    // GPS 데이터 DB 저장
    public int saveGps(Gps gps);

    // 위도, 경도, 거리 업데이트
    public Map<String, Double> updateLocationData(Map<String, Double> locationData, double latitude, double longitude);
}
