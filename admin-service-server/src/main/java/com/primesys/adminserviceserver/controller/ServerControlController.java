package com.primesys.adminserviceserver.controller;

import com.primesys.adminserviceserver.request.ServerControlRequest;
import com.primesys.adminserviceserver.response.ErrorResponse;
import com.primesys.adminserviceserver.response.HttpApiResponse;
import com.primesys.adminserviceserver.response.ServerControlResult;
import com.primesys.adminserviceserver.service.ServerControlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2/server")
@CrossOrigin("*")
public class ServerControlController {

    private final ServerControlService serverControlService;

    /**
     * Start or stop the Erlang server. The client sends only an action (START / STOP); the matching script path is
     * resolved and hardcoded server-side. Access is restricted to ADMIN in {@code SecurityConfiguration}.
     *
     * POST /v2/server/erlang-control body: { "action": "START" | "STOP" }
     */
    @PostMapping("/erlang-control")
    public ResponseEntity<HttpApiResponse<Object>> controlErlangServer(@RequestBody ServerControlRequest request) {
        String action = request == null ? null : request.getAction();
        log.info("erlang-control called action={}", action);

        try {
            ServerControlResult result = serverControlService.controlErlangServer(action);
            return new ResponseEntity<>(new HttpApiResponse<>(result, Boolean.TRUE), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid erlang-control request: {}", e.getMessage());
            HttpApiResponse<Object> response = new HttpApiResponse<>(new ErrorResponse(1005, e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("erlang-control failed: {}", e.getMessage(), e);
            HttpApiResponse<Object> response = new HttpApiResponse<>(new ErrorResponse(1005, e.getMessage()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
    }
}
