package com.amangarg4804.springbootpractice.sftp;

import com.amangarg4804.springbootpractice.config.SftpConfig;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.sftp.filters.SftpSimplePatternFileListFilter;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizer;
import org.springframework.integration.sftp.inbound.SftpInboundFileSynchronizingMessageSource;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.StopWatch;

import java.io.File;

@Configuration
@IntegrationComponentScan
@EnableIntegration
public class SftpPoller {

    private static final Logger logger = LogManager.getLogger("mastercard." + SftpPoller.class.getCanonicalName());

    private final SftpConfig sftpConfig;

    private final String remoteFileNameFilter;

    @Autowired
    public SftpPoller(SftpConfig sftpConfig,
                      @Value("${file.filter:*.csv}") String remoteFileNameFilter) {
        this.sftpConfig = sftpConfig;
        this.remoteFileNameFilter = remoteFileNameFilter;
    }

    @Bean
    public SftpInboundFileSynchronizer sftpInboundFileSynchronizer() {
        SftpInboundFileSynchronizer fileSynchronizer = new SftpInboundFileSynchronizer(sftpConfig.sftpSessionFactory());
        fileSynchronizer.setDeleteRemoteFiles(true);
        fileSynchronizer.setRemoteDirectory(sftpConfig.getSftpRemoteDir());
        fileSynchronizer.setFilter(new SftpSimplePatternFileListFilter(remoteFileNameFilter));
        return fileSynchronizer;
    }

    @Bean
    @InboundChannelAdapter(channel = "fromSftpChannel", poller = @Poller(cron = "${sftp.cron:0 */2 * ? * *}"))
    public MessageSource<File> sftpMessageSource() {
        SftpInboundFileSynchronizingMessageSource source = new SftpInboundFileSynchronizingMessageSource(
                sftpInboundFileSynchronizer());
        source.setLocalDirectory(new File(sftpConfig.getSftpLocalDir()));
        source.setAutoCreateLocalDirectory(true);
        source.setLocalFilter(new AcceptOnceFileListFilter<>());
        logger.info("Synchronizing files with pattern={} from {} {} to local {}", remoteFileNameFilter,
                sftpConfig.getSftpHost(), sftpConfig.getSftpRemoteDir(), sftpConfig.getSftpLocalDir());
        return source;
    }

    @Bean
    @ServiceActivator(inputChannel = "fromSftpChannel")
    public MessageHandler resultFileHandler() {
        return message -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            File fileReceived = null;
            try {
                fileReceived = (File) message.getPayload();
                String fileName = fileReceived.getName();
                logger.info("Received File = {}", fileName);
                // TODO: Process CSV
            } catch (Exception e) {
                logger.error("Error while processing the file.", e);


            } finally {
                try {
                    if(fileReceived!= null && fileReceived.exists()) {
                        FileUtils.forceDelete(fileReceived);
                        logger.info("file: {} has been deleted from dir: {}", fileReceived.getName(), this.sftpConfig.getSftpLocalDir());
                    }
                } catch (Exception e) {
                    logger.error("Exception while deleting file from local directory", e);
                }
            }
        };
    }


}


