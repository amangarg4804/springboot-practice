package com.amangarg4804.springbootpractice.config;

import com.jcraft.jsch.ChannelSftp;

import io.micrometer.common.util.StringUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.InputStreamResource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class SftpConfig {
    private static final Logger logger = LoggerFactory.getLogger(SftpConfig.class);
    private final int sftpPort;
    private final String sftpHost;
    private final String sftpUserName;
    private final String sftpPassword;
    private final String sftpLocalDir;
    private final String sftpRemoteDir;
    private final String sshUserPrivateKeyLocation;

    public SftpConfig(@Value("${sftp.port}") int sftpPort,
                      @Value("${sftp.host}") String sftpHost,
                      @Value("${sftp.user}") String sftpUserName,
                      @Value("${sftp.Pass}") String sftpPassword,
                      @Value("${sftp.sftpLocalDir:local}") String sftpLocalDir,
                      @Value("${sftp.sftpLocalDir:remote}") String sftpRemoteDir,
                      @Value("${sftp.sshUserPrivateKeyLocation}") String sshUserPrivateKeyLocation
    ) {
        this.sftpPort = sftpPort;
        this.sftpHost = sftpHost;
        this.sftpUserName = sftpUserName;
        this.sftpPassword = sftpPassword;
        this.sftpLocalDir = sftpLocalDir;
        this.sftpRemoteDir = sftpRemoteDir;
        this.sshUserPrivateKeyLocation = sshUserPrivateKeyLocation;
    }

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(sftpHost);
        factory.setPort(sftpPort);
        factory.setUser(sftpUserName);

        if (StringUtils.isNotBlank(sshUserPrivateKeyLocation)) {
            logger.info("Setting Private Key ={}", sshUserPrivateKeyLocation);
            try {
                Path path = new File(checkTilde(sshUserPrivateKeyLocation)).toPath();
                InputStreamResource inputStreamResource = new InputStreamResource(Files.newInputStream(path));
                factory.setPrivateKey(inputStreamResource);
            } catch (IOException e) {
                logger.error("Error while setting private key ", e);
            }
        } else {
            factory.setPassword(sftpPassword);
        }

        factory.setAllowUnknownKeys(true);
        logger.info("SftpConfig|SessionFactory");
        return new CachingSessionFactory<>(factory);
    }

    static String checkTilde(String str) {
        try {
            if (str.startsWith("~")) {
                str = str.replace("~", System.getProperty("user.home"));
            }
        } catch (SecurityException ignored) {
        }
        return str;
    }

    public String getSftpLocalDir() {
        return sftpLocalDir;
    }

    public String getSftpRemoteDir() {
        return sftpRemoteDir;
    }

    public String getSftpHost() {
        return sftpHost;
    }
}