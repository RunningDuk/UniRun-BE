package com.runningduk.unirun.api.service;

import com.runningduk.unirun.domain.entity.Gps;
import com.runningduk.unirun.domain.repository.GpsRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GPSServiceImpl implements GPSService {

    private final GpsRepository gpsRepository;

    public int saveGps(Gps gps) {
        Gps newGps = gpsRepository.save(gps);
        return newGps.getGpsId();
    }

    public Map<String, Double> updateLocationData(Map<String, Double> locationData, double latitude, double longitude) {


        // 이전 위도, 경도, 거리 값 가져오기
        Double lastLatitude = locationData.get("latitude");
        Double lastLongitude = locationData.get("longitude");
        Double lastDistance = locationData.get("distance");

        System.out.println("lastLatitude : " + lastLatitude + ", lastLongitude : " + lastLongitude + ", distance : " + lastDistance);

        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);

        double updatedDistance;
        if (lastLatitude == null && lastLongitude == null && lastDistance == null) {
            updatedDistance = 0.0;
        } else if (lastLatitude == null && lastLongitude == null && lastDistance != null) {
            updatedDistance = lastDistance;
        } else {
            final double EARTH_RADIUS = 6371.0;

            // 위도와 경도를 라디안 단위로 변환
            double dLat = Math.toRadians(latitude - lastLatitude);
            double dLon = Math.toRadians(longitude - lastLongitude);
            lastLatitude = Math.toRadians(lastLatitude);
            latitude = Math.toRadians(latitude);

            // 하버사인 공식 적용
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lastLatitude) * Math.cos(latitude);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            // 이전 거리와 현재 계산한 거리를 더해 갱신
            updatedDistance = lastDistance + EARTH_RADIUS * c;
        }

        locationData.put("distance", updatedDistance);

        return locationData;
    }
}
