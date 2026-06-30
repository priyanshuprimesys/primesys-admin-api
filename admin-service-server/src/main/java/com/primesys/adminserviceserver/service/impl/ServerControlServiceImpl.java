package com.primesys.adminserviceserver.service.impl;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.primesys.adminserviceserver.response.ServerControlResult;
import com.primesys.adminserviceserver.service.ServerControlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class ServerControlServiceImpl implements ServerControlService {

    /**
     * Script paths are hardcoded here on purpose — the client only sends an action and can never influence which script
     * runs (nor which host). {@link #controlErlangServer(String)} whitelists the action to one of these two entries.
     */
    private static final String START_SCRIPT = "/home/prime/scripts/erlang_server_start.sh";
    private static final String STOP_SCRIPT = "/home/prime/scripts/erlang_server_stop.sh";

    @Value("${erlang.server.ssh.host}")
    private String sshHost;

    @Value("${erlang.server.ssh.port:22}")
    private int sshPort;

    @Value("${erlang.server.ssh.username}")
    private String sshUsername;

    @Value("${erlang.server.ssh.private-key-path}")
    private String privateKeyPath;

    @Value("${erlang.server.ssh.known-hosts-path:}")
    private String knownHostsPath;

    @Value("${erlang.server.ssh.strict-host-key-checking:yes}")
    private String strictHostKeyChecking;

    @Value("${erlang.server.ssh.connect-timeout-seconds:15}")
    private int connectTimeoutSeconds;

    @Value("${erlang.server.ssh.command-timeout-seconds:120}")
    private int commandTimeoutSeconds;

    @Override
    public ServerControlResult controlErlangServer(String action) {
        // Whitelist the action server-side and map it to a fixed script path. Never derive the path from client input.
        final String normalized = action == null ? "" : action.trim().toUpperCase();
        final String script;
        if ("START".equals(normalized)) {
            script = START_SCRIPT;
        } else if ("STOP".equals(normalized)) {
            script = STOP_SCRIPT;
        } else {
            throw new IllegalArgumentException("Unsupported action '" + action + "'. Allowed: START, STOP.");
        }

        log.info("erlang-control action={} host={} script={}", normalized, sshHost, script);
        return runRemoteScript(normalized, script);
    }

    /**
     * Open an SSH session to the Erlang host and run the (hardcoded) script. stderr is folded into stdout so the caller
     * gets a single combined output. The session and channel are always torn down.
     */
    private ServerControlResult runRemoteScript(String action, String script) {
        Session session = null;
        ChannelExec channel = null;
        try {
            JSch jsch = new JSch();
            if (StringUtils.hasText(knownHostsPath)) {
                jsch.setKnownHosts(knownHostsPath);
            }
            jsch.addIdentity(privateKeyPath);

            session = jsch.getSession(sshUsername, sshHost, sshPort);
            session.setConfig("StrictHostKeyChecking", strictHostKeyChecking);
            session.connect((int) java.util.concurrent.TimeUnit.SECONDS.toMillis(connectTimeoutSeconds));

            channel = (ChannelExec) session.openChannel("exec");
            // script is a fixed, server-side constant — no client input is interpolated into the command.
            channel.setCommand("bash " + script);
            channel.setInputStream(null);
            channel.setErrStream(null, true); // route stderr to the same stream as stdout

            StringBuilder output = new StringBuilder();
            try (InputStream in = channel.getInputStream()) {
                channel.connect();

                byte[] buffer = new byte[1024];
                long deadline = System.currentTimeMillis()
                        + java.util.concurrent.TimeUnit.SECONDS.toMillis(commandTimeoutSeconds);
                while (true) {
                    while (in.available() > 0) {
                        int read = in.read(buffer, 0, buffer.length);
                        if (read < 0) {
                            break;
                        }
                        output.append(new String(buffer, 0, read, StandardCharsets.UTF_8));
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) {
                            continue; // drain anything that landed after the close flag flipped
                        }
                        break;
                    }
                    if (System.currentTimeMillis() > deadline) {
                        throw new IllegalStateException(
                                "Remote script timed out after " + commandTimeoutSeconds + "s: " + script);
                    }
                    Thread.sleep(100);
                }
            }

            int exitCode = channel.getExitStatus();
            String status = exitCode == 0 ? "SUCCESS" : "FAILED";
            log.info("erlang-control action={} finished exitCode={} status={}", action, exitCode, status);

            return ServerControlResult.builder().action(action).status(status).exitCode(exitCode)
                    .output(output.toString()).build();
        } catch (JSchException e) {
            log.error("SSH failure running erlang-control script {} on {}: {}", script, sshHost, e.getMessage(), e);
            throw new IllegalStateException("SSH connection/auth failed: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("I/O failure running erlang-control script {} on {}: {}", script, sshHost, e.getMessage(), e);
            throw new IllegalStateException("Failed to run control script: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running control script: " + e.getMessage(), e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }
}
