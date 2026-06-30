package com.primesys.adminserviceserver.service.impl;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.bulk.BulkWriteError;
import com.primesys.adminservicecommon.dto.ChatBotResponseDTO;
import com.primesys.adminservicemongodb.entity.DivisionLoginEntity;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import com.primesys.adminservicemongodb.repository.ChatBotQuestionsRepository;
import com.primesys.adminservicemongodb.repository.WMessageRepository;
import com.primesys.adminserviceserver.request.MessageRequest;
import com.primesys.adminserviceserver.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatBotServiceImpl implements ChatBotService {

    private final ChatBotQuestionsRepository chatBotQuestionsRepository;
    private final WMessageRepository wMessageRepository;
    private final DivisionLoginServiceImpl divisionLoginService;

    @Override
    public List<ChatBotResponseDTO> getQuestions() {

        return chatBotQuestionsRepository.findAll().stream()
                .map(tripEntity -> ChatBotResponseDTO.builder().id(tripEntity.getQueId().toString())
                        .question(tripEntity.getQuestion()).options(tripEntity.getOptions()).build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<WMessageEntity>> saveMsg(List<MessageRequest> msgList) {
        if (msgList == null || msgList.isEmpty()) {
            return Optional.empty();
        }

        // Step 1: Filter and map incoming messages to entities
        List<WMessageEntity> entitiesToSave = msgList.stream()
                .filter(e -> e.getSender() != null && !e.getSender().toLowerCase().contains("primesys")).map(msg -> {
                    String senderRaw = msg.getSender();
                    String[] parts = senderRaw.split(":");

                    String groupName = parts[0].trim().replaceAll("\\(\\d+ messages\\)", "").trim();
                    String senderName = parts.length > 1 ? parts[1].replace("~", "").replace(" ", "").trim() : "";

                    Optional<DivisionLoginEntity> optionalDivision = divisionLoginService
                            .getDivisionFromWGroupName(groupName);
                    String divisionId = optionalDivision.map(DivisionLoginEntity::getId)
                            .orElse("No matching division found");

                    if ("No matching division found".equals(divisionId)) {
                        log.warn("No division found for group name: {}", groupName);
                    }

                    return WMessageEntity.builder().message(msg.getMessage()).postTime(msg.getPostTime())
                            .groupName(groupName).senderName(senderName).sender(senderRaw).divisionId(divisionId)
                            .isIssue(false).noteId(msg.getNoteId()).activeStatus(true).build();
                }).collect(Collectors.toList());

        // Step 2: Extract noteIds and check for duplicates
        List<String> noteIds = entitiesToSave.stream().map(WMessageEntity::getNoteId).collect(Collectors.toList());

        List<String> existingNoteIds = wMessageRepository.findByNoteIdIn(noteIds).stream()
                .map(WMessageEntity::getNoteId).collect(Collectors.toList());

        // Step 3: Filter out duplicates
        List<WMessageEntity> toSave = entitiesToSave.stream()
                .filter(entity -> !existingNoteIds.contains(entity.getNoteId())).collect(Collectors.toList());

        if (toSave.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(wMessageRepository.saveAll(toSave));
    }

    @Override
    public Optional<List<WMessageEntity>> getWhatsAppMsg() {
        return Optional.of(wMessageRepository.findByActiveStatusTrue());
    }

    @Override
    public String pickUpIssue(String noteId, String userId, String action) {
        return null;
    }

}
