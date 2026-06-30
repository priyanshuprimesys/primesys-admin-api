package com.primesys.adminserviceserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeviceTypeService {

    public enum DeviceType {

        KEYMAN("Keyman", 1), PATROLMAN("Patrolman", 2), USFD("USFD", 3), MATE("Mate", 4), GATEMITRA("Gatemitra", 5),
        STATIONERY_WATCHMAN("Stationery Watchman", 6), TRD_PATROLMAN("TRD PatrolMan", 7),

        KEYMAN_TRIPWISE("Keyman Tripwise", 8), PATROLMAN_TRIPWISE("Patrolman Tripwise", 9), MOPPER("Mopper", 10),
        SWEEPER("Sweeper", 11), PWI("PWI", 12);

        private final String name;
        private final int id;

        DeviceType(String name, int id) {
            this.name = name;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        /**
         * Normalize input and match safely
         */
        public static DeviceType fromName(String input) {
            if (input == null) {
                return null;
            }

            String normalizedInput = normalize(input);

            for (DeviceType type : DeviceType.values()) {
                if (normalize(type.getName()).equals(normalizedInput)) {
                    return type;
                }
            }
            return null;
        }

        private static String normalize(String value) {
            return value.trim().toLowerCase().replace("_", "").replace(" ", "");
        }
    }

    public Integer getDeviceTypeId(String deviceType) {
        DeviceType type = DeviceType.fromName(deviceType);
        return type != null ? type.getId() : 0;
    }
}
