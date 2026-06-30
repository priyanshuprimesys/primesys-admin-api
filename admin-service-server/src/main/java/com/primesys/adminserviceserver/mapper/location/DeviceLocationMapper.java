package com.primesys.adminserviceserver.mapper.location;

import com.primesys.adminservicemongodb.entity.DeviceLocation;
import com.primesys.adminserviceserver.dtos.location.DeviceLocationDTO;
import com.primesys.adminserviceserver.dtos.location.GeoLocationDTO;
import com.primesys.adminserviceserver.dtos.location.NearestRdpsDTO;
import com.primesys.adminserviceserver.dtos.location.StatusDTO;

import java.util.List;
import java.util.Map;

public final class DeviceLocationMapper {

    private static GeoLocationDTO mapGeoLocation(Object geoObj) {

        if (!(geoObj instanceof Map<?, ?> geoMap))
            return null;

        List<?> coords = (List<?>) geoMap.get("coordinates");

        double[] coordinates = coords == null ? null
                : coords.stream().mapToDouble(v -> ((Number) v).doubleValue()).toArray();

        return new GeoLocationDTO((String) geoMap.get("type"), coordinates);
    }

    private static StatusDTO mapStatus(Object statusObj) {

        if (!(statusObj instanceof Map<?, ?> statusMap))
            return null;

        return new StatusDTO(castInteger(statusMap.get("gps_real_time")), castInteger(statusMap.get("gps_position")),
                (String) statusMap.get("lon_direction"), (String) statusMap.get("lat_direction"),
                castInteger(statusMap.get("cource")) // DB typo handled here
        );
    }

    private static NearestRdpsDTO mapNearestRdps(Object rdpsObj) {

        if (!(rdpsObj instanceof Map<?, ?> rdpsMap))
            return null;

        return new NearestRdpsDTO(mapGeoLocation(rdpsMap.get("geo_location")), (String) rdpsMap.get("feature_detail"),
                castInteger(rdpsMap.get("kilometer")), castInteger(rdpsMap.get("distance")),
                castDouble(rdpsMap.get("distance_diff")));
    }

    private static Integer castInteger(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private static Double castDouble(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }
}
