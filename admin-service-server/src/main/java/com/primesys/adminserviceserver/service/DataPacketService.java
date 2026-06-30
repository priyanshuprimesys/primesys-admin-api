package com.primesys.adminserviceserver.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.primesys.adminservicemongodb.entity.DevicePacketEntity;
import com.primesys.adminservicemongodb.model.DevicePacketDto;
import com.primesys.adminservicemongodb.repository.DevicePacketRepository;
import com.primesys.adminservicemongodb.repository.SecondaryDevicePacketRepositoryImpl;
import com.primesys.adminserviceserver.utility.HexDecoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class DataPacketService {

    private final MongoTemplate primaryMongoTemplate;
    private final MongoTemplate secondaryMongoTemplate;
    @Autowired
    private SecondaryDevicePacketRepositoryImpl secondaryDevicePacketRepositoryImpl;
    @Autowired
    private final DevicePacketRepository devicePacketRepository;
    @Autowired
    private HexDecoder hexDecoder;

    public DataPacketService(@Value("${spring.data.mongodb.uri}") String primaryUri,
            @Value("${spring.data.mongodb.backup.uri}") String secondaryUri,
            DevicePacketRepository devicePacketRepository) {
        this.devicePacketRepository = devicePacketRepository;

        MongoClient primaryClient = MongoClients.create(primaryUri);
        MongoClient secondaryClient = MongoClients.create(secondaryUri);

        this.primaryMongoTemplate = new MongoTemplate(primaryClient, "primesystrack");
        this.secondaryMongoTemplate = new MongoTemplate(secondaryClient, "primesystrack");
    }

    public Page<DevicePacketDto> getDevicePackets(Long imei, Long startTime, Long endTime, Pageable pageable) {

        Page<DevicePacketDto> entityPage = devicePacketRepository
                .findByDeviceImeiAndTimestampBetween(imei, startTime, endTime,
                        PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                                Sort.by(Sort.Order.asc("timestamp"), Sort.Order.asc("packetFrom"))))
                .map(entity -> new DevicePacketDto(entity.getId(), entity.getDeviceImei(), entity.getPacket(),
                        entity.getTimestamp(), entity.getPacketFrom(), entity.getPacketTypes(),
                        hexDecoder.getAsciiData(entity.getPacket())));

        return entityPage; // already sorted and already a Page<DevicePacketDto>
    }

}
