package com.primesys.adminserviceserver.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to control the Erlang server. The client only sends the {@code action}; the script path is resolved and
 * hardcoded server-side and is never accepted from the client.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerControlRequest {

    /** Action to perform — whitelisted server-side to START or STOP only (case-insensitive). */
    String action;
}
