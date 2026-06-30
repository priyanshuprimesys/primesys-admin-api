package com.primesys.adminserviceserver.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primesys.adminserviceserver.request.DeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StringToDeviceRequestConverter implements Converter<String, DeviceRequest> {

    private final ObjectMapper objectMapper;

    @Override
    public DeviceRequest convert(String source) {
        try {
            return objectMapper.readValue(source, DeviceRequest.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}