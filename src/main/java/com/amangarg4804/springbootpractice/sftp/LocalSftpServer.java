package com.amangarg4804.springbootpractice.sftp;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;

import org.apache.sshd.sftp.server.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
@Service
public class LocalSftpServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalSftpServer.class);
    @PostConstruct
    public void startServer() throws IOException {
        start();
    }

    private void start() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        sshd.setPort(2222);
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(new File("host.ser").toPath()));
        sshd.setSubsystemFactories(Collections.singletonList(new SftpSubsystemFactory()));
        sshd.setPasswordAuthenticator((username, password, session) -> username.equals("test") && password.equals("password"));
        sshd.start();
        logger.info("SFTP server started");
    }
}
