package com.primesys.adminservicemongodb.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "device_packet") // Maps to the MongoDB collection
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DevicePacketEntity {

    @Id
    private String id; // _id field from MongoDB (ObjectId)
    @Field("device_imei")
    private Long deviceImei; // device_imei field

    private String packet; // packet field

    private Long timestamp; // timestamp field
    @Field("packet_from")

    private String packetFrom; // packet_from field

    public String getId() {
        if (id != null)
            return id;
        else
            return null;
    }

    // public String getPacketType() {
    // if (packet == null || packet.isEmpty())
    // return "Un-Parsed";
    //
    // Map<String, String> packetPatterns = new LinkedHashMap<>();
    // packetPatterns.put("78782AA0", "GPS Location Packet over 4G (0xA0)");
    // packetPatterns.put("78781101", "Login Packet (0x01)");
    // packetPatterns.put("78780501", "Login Packet (0x01) Server Reply");
    // packetPatterns.put("78780D1F", "Time Calibration Packet (0x1F)");
    // packetPatterns.put("78780B1F", "Time Calibration Packet (0x1F) Reply");
    // packetPatterns.put("78781136", "Heartbeat Packet (0x36)");
    // packetPatterns.put("78780536", "Heartbeat Packet (0x36) Reply");
    //
    // List<String> detectedPackets = new ArrayList<>();
    //
    // for (Map.Entry<String, String> entry : packetPatterns.entrySet()) {
    // if (packet.contains(entry.getKey())) {
    // detectedPackets.add(entry.getValue());
    // }
    // }
    //
    // if (detectedPackets.isEmpty())
    // return "Un-Parsed";
    //
    // // Multiple packets detected in single payload
    // return String.join(" + ", detectedPackets);
    // }
    public String getPacketTypes() {
        if (packet == null || packet.length() < 8)
            return "Un-Parsed";

        List<String> types = new ArrayList<>();

        /* ================= LOGIN ================= */
        addIfPresent(types, "78781101", "Login Packet (0x01)");
        addIfPresent(types, "78780501", "Login Reply Packet (0x01)");

        /* ================= TIME CALIBRATION ================= */
        addIfPresent(types, "78780D1F", "Time Calibration Packet (0x1F)");
        addIfPresent(types, "78780B1F", "Time Calibration Reply Packet (0x1F)");

        /* ================= HEARTBEAT ================= */
        addIfPresent(types, "78781136", "Heartbeat Packet (0x36)");
        addIfPresent(types, "78780536", "Heartbeat Reply Packet (0x36)");
        addIfPresent(types, "78780A13", "2G Heartbeat Reply Packet (0x13)");

        /* ================= GPS / LBS / 4G ================= */
        addIfPresent(types, "78781F12", "GPS Location Packet (0x12)");
        addIfPresent(types, "78781F13", "LBS Location Packet (0x13)");
        addIfPresent(types, "78783B18", "2G LBS Location Packet (0x18)");
        addIfPresent(types, "78781F18", "Blind / LBS Location Packet (0x18)");
        addIfPresent(types, "78782AA0", "GPS Location Packet over 4G (0xA0)");
        addIfPresent(types, "78781910", "2G GPS Location Packet (0x10)");

        /* ================= ALARMS ================= */
        addIfPresent(types, "78781A16", "SOS Alarm Packet (0x16)");
        addIfPresent(types, "78781A04", "Low Battery Alarm Packet (0x04)");
        addIfPresent(types, "78781A11", "Overspeed Alarm Packet (0x11)");
        addIfPresent(types, "78781A09", "Power Cut Alarm Packet (0x09)");

        /* ================= STATUS ================= */
        addIfPresent(types, "78781294", "Device Information Packet (0x94)");
        addIfPresent(types, "78781213", "Terminal Status Packet (0x13)");
        addIfPresent(types, "78781215", "GSM Signal Strength Packet (0x15)");

        if (types.isEmpty())
            return "Un-Parsed";

        return String.join(" + ", types);
    }

    private void addIfPresent(List<String> types, String hex, String label) {
        int count = countOccurrences(packet, hex);
        if (count > 0) {
            types.add(label + " x" + count);
        }
    }

    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    // /* ================= LOGIN ================= */
    // if (packet.contains("78781101"))
    // types.add("Login Packet (0x01) – Device → Server");
    // if (packet.contains("78780501"))
    // types.add("Login Reply Packet (0x01) – Server → Device");
    //
    // /* ================= TIME CALIBRATION ================= */
    // if (packet.contains("78780D1F"))
    // types.add("Time Calibration Packet (0x1F) – Device → Server");
    // if (packet.contains("78780B1F"))
    // types.add("Time Calibration Reply Packet (0x1F) – Server → Device");
    //
    // /* ================= HEARTBEAT ================= */
    // if (packet.contains("78781136"))
    // types.add("Heartbeat Packet (0x36) – Device → Server");
    // if (packet.contains("78780536"))
    // types.add("Heartbeat Reply Packet (0x36) – Server → Device");
    //
    // /* ================= GPS / LBS / 4G ================= */
    // if (packet.contains("78781F12"))
    // types.add("GPS Location Packet (0x12)");
    // if (packet.contains("78781F13"))
    // types.add("LBS Location Packet (0x13)");
    // if (packet.contains("78781F18"))
    // types.add("Blind / LBS Location Packet (0x18)");
    // if (packet.contains("78782AA0"))
    // types.add("GPS Location Packet over 4G (0xA0)");
    //
    // /* ================= ALARMS ================= */
    // if (packet.contains("78781A16"))
    // types.add("SOS Alarm Packet (0x16)");
    // if (packet.contains("78781A04"))
    // types.add("Low Battery Alarm Packet (0x04)");
    // if (packet.contains("78781A11"))
    // types.add("Overspeed Alarm Packet (0x11)");
    // if (packet.contains("78781A09"))
    // types.add("Power Cut Alarm Packet (0x09)");
    //
    // /* ================= STATUS ================= */
    // if (packet.contains("78781294"))
    // types.add("Device Information Packet (0x94)");
    // if (packet.contains("78781213"))
    // types.add("Terminal Status Packet (0x13)");
    // if (packet.contains("78781215"))
    // types.add("GSM Signal Strength Packet (0x15)");
    //
    // if (types.isEmpty())
    // types.add("Un-Parsed");
    // return String.join(" + ", types);

    // 🔐 Login
    // Packet Hex Pattern Direction
    // Login 78781101 Device → Server
    // Login Reply 78780501 Server → Device
    // 📍 GPS / LBS / 4G Location
    // Packet Hex Pattern Description
    // GPS Location 78781F12 GPS packet
    // LBS Location 78781F13 LBS packet
    // Blind / LBS 78781F18 Blind location
    // 4G GPS 78782AA0 4G GPS packet
    // ⏱ Time Calibration (0x1F)
    // Packet Hex Pattern Direction
    // Time Request 78780D1F Device → Server
    // Time Reply 78780B1F Server → Device
    //
    // ✅ Your reply 78780B1F695FAC3F000000044A820D0A is CORRECT as per doc
    //
    // ❤️ Heartbeat
    // Packet Hex Pattern Direction
    // Heartbeat 78781136 Device → Server
    // Heartbeat Reply 78780536 Server → Device
    // ⚠ Alarms
    // Packet Hex Pattern
    // SOS Alarm 78781A16
    // Low Battery 78781A04
    // Overspeed 78781A11
    // Power Cut 78781A09
    // 📡 Status / Info
    // Packet Hex Pattern
    // Device Info 78781294
    // Terminal Status 78781213
    // GSM Signal 78781215

}
