package com.primesys.adminserviceserver.service;

import com.primesys.adminserviceserver.response.ServerControlResult;

public interface ServerControlService {

    /**
     * Run the Erlang server control script for the given action. The action is whitelisted server-side: only START and
     * STOP are accepted, each mapped to a script path that is hardcoded in the implementation. The client never
     * supplies a path.
     *
     * @param action
     *            "START" or "STOP" (case-insensitive); anything else is rejected
     *
     * @return the action, exit status, exit code, and combined script output
     *
     * @throws IllegalArgumentException
     *             if the action is not START or STOP
     */
    ServerControlResult controlErlangServer(String action);
}
