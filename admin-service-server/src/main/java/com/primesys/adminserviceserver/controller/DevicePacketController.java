package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.model.DevicePacketDto;
import com.primesys.adminservicemongodb.repository.DevicePacketRepository;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.service.DataPacketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping("/v2/packets")

public class DevicePacketController {

    @Autowired
    private DevicePacketRepository devicePacketRepository;
    @Autowired
    private DataPacketService dataPacketService;

    // @GetMapping("/device-packets")
    // public List<DevicePacketDto> getDevicePackets(@RequestParam Long imei, @RequestParam Long startTime,
    // @RequestParam Long endTime) {
    // List<DevicePacketDto> packets = dataPacketService.getDevicePackets(imei, startTime, endTime);
    //
    // return packets.stream().sorted(Comparator.comparing(DevicePacketDto::getTimestamp)) // Sort by timestamp
    // .map(packet -> new DevicePacketDto(packet.getId(), packet.getDeviceImei(), packet.getPacket(),
    // packet.getTimestamp(), packet.getPacketFrom(), packet.getPacketType() // Compute packet type
    // )).collect(Collectors.toList());
    //
    // }
    @GetMapping("/device-packets")
    public HttpApiResponse<Page<DevicePacketDto>> getDevicePackets(@RequestParam Long imei,
            @RequestParam Long startTime, @RequestParam Long endTime, Pageable pageable // <--- pagination support
    ) {
        Page<DevicePacketDto> packets = dataPacketService.getDevicePackets(imei, startTime, endTime, pageable);

        HttpApiResponse<Page<DevicePacketDto>> result = new HttpApiResponse<>(packets);
        return result;
        // return packets.map(packet -> new DevicePacketDto(packet.getId(), packet.getDeviceImei(), packet.getPacket(),
        // packet.getTimestamp(), packet.getPacketFrom(), packet.getPacketType()));
    }

}