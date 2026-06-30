package com.primesys.adminserviceserver.service.impl;

import com.primesys.adminservicecommon.dto.DeviceCommandDto;
import com.primesys.adminservicecommon.dto.DeviceCommandHistoryDto;
import com.primesys.adminservicemongodb.entity.DeviceCommandHistoryEntity;
import com.primesys.adminservicemongodb.entity.DeviceEntity;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.PendingWhitelistEntity;
import com.primesys.adminservicemongodb.repository.DeviceCommandHistoryRepository;
import com.primesys.adminservicemongodb.repository.DeviceCommandRepository;
import com.primesys.adminservicemongodb.repository.DeviceRepository;
import com.primesys.adminservicemongodb.repository.DivisionLoginRepository;
import com.primesys.adminservicemongodb.repository.PendingWhitelistRepository;
import com.primesys.adminserviceserver.service.DeviceCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j

@RequiredArgsConstructor
public class DeviceCommandServiceImpl implements DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceCommandHistoryRepository deviceCommandHistoryRepository;
    private final PendingWhitelistRepository pendingWhitelistRepository;
    private final DeviceRepository deviceRepository;

    @Value("${primesys.device.command.host}")
    private String commandSocketHost;

    @Value("${primesys.device.command.port}")
    private int commandSocketPort;

    private static final int THREAD_POOL_SIZE = 15;
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @Override
    public List<DeviceCommandDto> getAllDeviceCommands() {
        return deviceCommandRepository.findAll().stream().filter(s -> s.isActiveStatus() == true)
                .map(deviceCommandEntity -> DeviceCommandDto.builder().id(deviceCommandEntity.getCommandId())
                        .activeStatus(deviceCommandEntity.isActiveStatus()).command(deviceCommandEntity.getCommand())
                        .title(deviceCommandEntity.getTitle()).reply(deviceCommandEntity.getReply())
                        .description(deviceCommandEntity.getDescription()).isCustom(deviceCommandEntity.isCustom())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceCommandHistoryDto> getAllDeviceCommandHistoryForDate(long startTime, long endTime) {
        List<DeviceCommandHistoryEntity> deviceCommandHistoryEntities = deviceCommandHistoryRepository
                .findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
        if (CollectionUtils.isEmpty(deviceCommandHistoryEntities)) {
            return Collections.emptyList();
        }
        log.info(deviceCommandHistoryEntities.get(0).getDeviceName());
        return deviceCommandHistoryEntities.stream().map(deviceCommandHistoryEntity -> DeviceCommandHistoryDto.builder()
                .deviceImei(deviceCommandHistoryEntity.getDeviceImei()).command(deviceCommandHistoryEntity.getCommand())
                // .commandId(deviceCommandHistoryEntity.getCommandId())
                .deviceCommandResponse(deviceCommandHistoryEntity.getDeviceResponse())
                .deviceName(deviceCommandHistoryEntity.getDeviceName())
                .timestamp(deviceCommandHistoryEntity.getTimestamp())
                .deviceResponseTime(deviceCommandHistoryEntity.getDeviceResponseTime())
                .loginName(deviceCommandHistoryEntity.getLoginName()).isResend(deviceCommandHistoryEntity.isResend())
                .resentAt(deviceCommandHistoryEntity.getResentAt())
                .commandDeliveredMsg(deviceCommandHistoryEntity.getDeliveredMessage()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceCommandHistoryEntity> sendCommand(List<DeviceCommandHistoryEntity> deviceCommandHistoryEntity) {
        // Record any SOS/FN command as pending until it is whitelisted. Done before the send so it is tracked even
        // if the command server is unreachable. simProvider and registration status come from the device record
        // (looked up by IMEI): a device is treated as "registered" when it is connected, so a connected device is
        // skipped and only unregistered (disconnected) devices are tracked as pending.
        for (DeviceCommandHistoryEntity entity : deviceCommandHistoryEntity) {
            final String cmd = entity.getCommand();
            if (cmd != null) {
                final String upper = cmd.trim().toUpperCase();
                if (upper.startsWith("SOS") || upper.startsWith("FN")) {
                    final DeviceEntity device = deviceRepository.findByDeviceImei(entity.getDeviceImei());
                    if (device == null || !device.isConnected()) {
                        pendingWhitelistRepository.save(PendingWhitelistEntity.builder()
                                .deviceImei(entity.getDeviceImei()).deviceName(entity.getDeviceName())
                                .commandType(upper.startsWith("SOS") ? "SOS" : "FN").command(cmd)
                                .simProvider(device == null ? null : device.getSimServiceProvider())
                                .loginName(entity.getLoginName()).status("PENDING")
                                .createdAt(System.currentTimeMillis()).build());
                    }
                }
            }
        }

        try (Socket socket = new Socket(commandSocketHost, commandSocketPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            log.info("Connected to command server at {}:{}", commandSocketHost, commandSocketPort);

            for (DeviceCommandHistoryEntity entity : deviceCommandHistoryEntity) {
                writer.println(createCommandJson(entity));
                writer.flush();
                Thread.sleep(5);
            }

            log.info("Commands sent successfully. count={}", deviceCommandHistoryEntity.size());

        } catch (UnknownHostException e) {
            log.error("Unknown command server host: {}", commandSocketHost, e);
        } catch (IOException e) {
            log.error("IO error connecting to command server {}:{}", commandSocketHost, commandSocketPort, e);
        } catch (Exception e) {
            log.error("Unexpected error sending commands", e);
        }

        return deviceCommandHistoryEntity;
    }

    @Override
    public List<DeviceCommandHistoryEntity> sendCommandEmergency(
            List<DeviceCommandHistoryEntity> deviceCommandHistoryEntity) {
        try (Socket socket = new Socket(commandSocketHost, commandSocketPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            log.info("Connected to command server at {}:{} for emergency", commandSocketHost, commandSocketPort);

            for (DeviceCommandHistoryEntity entity : deviceCommandHistoryEntity) {
                JSONObject commandJson = createCommandJson(entity);
                ((JSONObject) commandJson.get("data")).put("command", "AUDIOALM,3");
                writer.println(commandJson);
                writer.flush();
                Thread.sleep(5);
            }

            log.info("Emergency commands sent successfully. count={}", deviceCommandHistoryEntity.size());

        } catch (UnknownHostException e) {
            log.error("Unknown command server host: {}", commandSocketHost, e);
        } catch (IOException e) {
            log.error("IO error connecting to command server {}:{}", commandSocketHost, commandSocketPort, e);
        } catch (Exception e) {
            log.error("Unexpected error sending emergency commands", e);
        }

        return deviceCommandHistoryEntity;
    }

    /**
     * Creates a JSON object representing a command to be sent.
     */
    private JSONObject createCommandJson(DeviceCommandHistoryEntity entity) {
        JSONObject commandJson = new JSONObject();
        commandJson.put("event", "send_command");
        commandJson.put("device_imei", entity.getDeviceImei());

        JSONObject dataJson = new JSONObject();
        dataJson.put("command", entity.getCommand());
        dataJson.put("device", entity.getDeviceImei());
        dataJson.put("timestamp", entity.getTimestamp());
        dataJson.put("deviceName", entity.getDeviceName());
        dataJson.put("loginName", entity.getLoginName());
        dataJson.put("parent_id", entity.getDivisionId());

        commandJson.put("data", dataJson);

        return commandJson;
    }

}
