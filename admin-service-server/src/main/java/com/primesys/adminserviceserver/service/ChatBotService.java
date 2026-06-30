package com.primesys.adminserviceserver.service;

import com.primesys.adminservicecommon.dto.ChatBotResponseDTO;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import com.primesys.adminserviceserver.request.MessageRequest;

import java.util.List;
import java.util.Optional;

public interface ChatBotService {

    List<ChatBotResponseDTO> getQuestions();

    Optional<List<WMessageEntity>> saveMsg(List<MessageRequest> msgList);

    Optional<List<WMessageEntity>> getWhatsAppMsg();

    String pickUpIssue(String noteId, String userId, String action);
}
