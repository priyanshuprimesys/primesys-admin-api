package com.primesys.adminserviceserver.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of an Erlang server control action. {@code status} reflects whether the script exited cleanly (SUCCESS /
 * FAILED); {@code output} is the combined stdout+stderr from the script for display.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerControlResult {

    /** Echo of the action that ran — START or STOP. */
    String action;

    /** SUCCESS when the script exited 0, otherwise FAILED. */
    String status;

    /** Script process exit code. */
    int exitCode;

    /** Combined stdout + stderr from the script. */
    String output;
}
