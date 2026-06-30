package com.primesys.adminserviceserver.controller;

import com.primesys.adminservicecommon.dto.ChatBotResponseDTO;
import com.primesys.adminservicemongodb.entity.WMessageEntity;
import com.primesys.adminserviceserver.request.MessageRequest;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.response.HttpChatBotApiResponse;
import com.primesys.adminserviceserver.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/chat-bot")
@CrossOrigin("*")
public class ChatbotController {

    private final ChatBotService chatBotService;

    @GetMapping("/get-questions")
    public ResponseEntity<HttpChatBotApiResponse<List<ChatBotResponseDTO>>> getQuestions() {
        log.info("get-questions call");
        List<ChatBotResponseDTO> questions = chatBotService.getQuestions();
        log.info("get-questions returned {} question(s)", questions.size());
        return ResponseEntity.ok(new HttpChatBotApiResponse<>(questions));
    }

    @PostMapping("/save-msg")
    public ResponseEntity<HttpApiResponse<Object>> saveMsg(@RequestBody List<MessageRequest> msgList) {
        log.info("save-msg call with {} message(s)", msgList == null ? 0 : msgList.size());
        Optional<List<WMessageEntity>> response = chatBotService.saveMsg(msgList);
        log.info("save-msg saved {} message(s)", response.map(List::size).orElse(0));
        return new ResponseEntity<>(new HttpApiResponse<>(response, Boolean.TRUE), HttpStatus.OK);
    }

    @GetMapping("/get-whatsapp-msg")
    public ResponseEntity<HttpApiResponse<Object>> getWhatsAppMsg() {
        log.info("get-whatsapp-msg call");
        Optional<List<WMessageEntity>> response = chatBotService.getWhatsAppMsg();
        log.info("get-whatsapp-msg returned {} message(s)", response.map(List::size).orElse(0));
        return new ResponseEntity<>(new HttpApiResponse<>(response, Boolean.TRUE), HttpStatus.OK);
    }

}
