/*
 * Copyright (c) 2022 NIBIO <http://www.nibio.no/>. 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package no.nibio.vips.logic.messaging.distribution;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import no.nibio.vips.logic.messaging.distribution.entity.DistributionTypeEnum;
import no.nibio.vips.logic.messaging.distribution.entity.MsgDistResultEnum;
import no.nibio.vips.logic.messaging.distribution.entity.MsgReceiver;
import no.nibio.vips.logic.messaging.distribution.entity.MsgToSend;
import no.nibio.vips.logic.messaging.distribution.entity.VipsMessage;
import no.nibio.vips.logic.messaging.distribution.entity.VipsSendMsgResult;
import no.nibio.vips.logic.messaging.distribution.send.CSVTrafficFileLogger;
import no.nibio.vips.logic.messaging.distribution.send.IDeliveryMsgHandler;
import no.nibio.vips.logic.messaging.distribution.send.MailMsgDeliveryHandler;
import no.nibio.vips.logic.messaging.distribution.send.SingleMsgSendResult;
import no.nibio.vips.logic.messaging.distribution.send.SingleMsgSentStateEnum;
import no.nibio.vips.logic.messaging.distribution.send.SmsMsgDeliveryHandler;



/**
 * Handles client communication with the different types of channels that are
 * available
 *
 * @author Lars Aksel Opsahl <lars.opsahl@nibio.no>
 * @author Tor-Einar Skog <tor-einar.skog@nibio.no>
 *
 */
public class VipsMessageInputHandler implements IVipsMessageHandler {
    
    private static Logger LOGGER = LoggerFactory.getLogger(VipsMessageInputHandler.class);


    // where to out log files for th etraf
    private String trafficLogDirectory;

    // mail server properties
    private String smtpMailserver;
    private String mailSenderAddress;
    private Boolean simulateMailSending;

    // smcs server properties
    private String smscServer;
    private String smscUsername;
    private String smscPassword;
    private String smscSenderSrc;

    // the default locale be used if there are no matching value between the sender and the messages
    // if the there are no messages in this local the message will just be taken.
    private String msgLocaleIfNoMatch = "en";

    // the blocker queue to avoid to many to many results to be fetched at at time
    // TODO fix this to work inside boundig box
    private BlockingQueue subJobBlockingQueue;

    // A thread safe temporary job counter
    // Will be reset when tomcat restarts
    private final AtomicLong jobnr = new AtomicLong(0);

    /**
     * ExecutorService to hadle message sending
     */
    private ExecutorService executorService;

    // The list of message output handlers
    Hashtable<DistributionTypeEnum, IDeliveryMsgHandler> msgHandlerList = new Hashtable<>();

    // TODO maybe move this to a config file
    public VipsMessageInputHandler() {
        this.init();
    }

    public void init() {
        IDeliveryMsgHandler mailHandler = new MailMsgDeliveryHandler(getSmtpMailserver(), getMailSenderAddress(), isSimumlateMailSending());
        msgHandlerList.put(mailHandler.getDistributionType(), mailHandler);

        IDeliveryMsgHandler smsHandler = new SmsMsgDeliveryHandler(getSmscServer(), getSmscUsername(), getSmscPassword(), getSmscSenderSrc(),
                isSimumlateMailSending());
        msgHandlerList.put(smsHandler.getDistributionType(), smsHandler);

        // check if log directory exist
        File f = new File(this.getTrafficLogDirectory());
        if (!f.exists()) {
            f.mkdir();
            /*
            if (logger.isDebugEnabled()) {
                logger.debug("create traffic log file directory " + trafficLogDirectory);
            }
            */
        }

        // set up a job queue with max
        subJobBlockingQueue = new ArrayBlockingQueue(1);
        this.executorService = Executors.newFixedThreadPool(10);

    }

    @Override
    public VipsSendMsgResult sendMessage(VipsMessage vm) throws Exception {

        
        LOGGER.debug("vm message expiresAt " + vm.expiresAt);
        
        VipsSendMsgResult vipsSendMsgResult = new VipsSendMsgResult();

        Long nextJob = jobnr.incrementAndGet();

        try {

            LOGGER.debug("Ready to start next job" + nextJob);

            // add to job queue or wait to many jobs are running
            subJobBlockingQueue.put(nextJob);

            // start vips message job, this does take a long time, because we here initiate the job sending
            vipsSendMsgResult = startsVipsMessageJob(vm);

        } catch (InterruptedException e) {
            vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSentWithException;
            LOGGER.error("Failed to send message: ", e);

        } finally {
            // remove the job so the next job can run
            subJobBlockingQueue.remove(nextJob);

        }

        return vipsSendMsgResult;
    }

    /**
     * Starts the vips message job
     *
     * @param vm
     * @param vipsSendMsgResult
     * @param msgSentResult
     * @return
     */
    private VipsSendMsgResult startsVipsMessageJob(VipsMessage vm) {


        Date currentTime = new Date();
        String pattern = "yyyy_MM_dd_H_m";
        SimpleDateFormat format = new SimpleDateFormat(pattern);

        VipsSendMsgResult vipsSendMsgResult = new VipsSendMsgResult();
        vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSent;
        UUID uuid = UUID.randomUUID();
        String jobId = uuid.toString();
        vipsSendMsgResult.jobId = jobId;
        vipsSendMsgResult.numRecipients = vm.distributionList.size();

        // trafficLogDirectory
        String fileNameReceived = getTrafficLogDirectory() + vipsSendMsgResult.jobId + "_received_" + format.format(currentTime);
        // create a log with jobid name and received time
        // the file should contain the following fileds
        CSVTrafficFileLogger.writeCsvFilerReceived(fileNameReceived, vm.distributionList);

        // the filename to keep the results
        String fileNameSentResult = getTrafficLogDirectory() + vipsSendMsgResult.jobId + "_sent_" + format.format(currentTime);

        // check if message is to old before trying to send it
        // TODO may be move this check to when sending message, but then some messages may be sent and other not ????
        if (vm.expiresAt != null && vm.expiresAt.before(currentTime)) {
            vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSentToOld;
            // list of result to used for logging
            ArrayList<SingleMsgSendResult> msgSentResult = new ArrayList<>();

            vm.distributionList.stream().map((msgReceiver) -> new SingleMsgSendResult(msgReceiver, SingleMsgSentStateEnum.FailedNotSentToOld)).forEachOrdered((msgSendResult) -> {
                msgSentResult.add(msgSendResult);
            });
            // create a log with jobid name and received time
            // the file should contain the following fileds
            CSVTrafficFileLogger.writeCsvFilerReceived(fileNameSentResult, msgSentResult);

        } else {

            // submit the job and leave, dont't wiath for any results
            RunVipsMessageJob runVipsMessageJob = new RunVipsMessageJob(vm, vipsSendMsgResult, fileNameSentResult);
            executorService.submit(runVipsMessageJob);

        }


        return vipsSendMsgResult;
    }

    /**
     *
     * Runs a job against and get info about single farm
     *
     */
    class RunVipsMessageJob implements Callable<VipsSendMsgResult> {

        private final VipsMessage vm;
        private final VipsSendMsgResult vipsSendMsgResult;
        private final String fileNameSentResult;

        public RunVipsMessageJob(VipsMessage vm, VipsSendMsgResult vipsSendMsgResult, String fileNameSentResult) {
            this.vm = vm;
            this.vipsSendMsgResult = vipsSendMsgResult;
            this.fileNameSentResult = fileNameSentResult;
        }

        @Override
        public VipsSendMsgResult call() {
           

            // list of result to used for logging
            ArrayList<SingleMsgSendResult> msgSentResult = new ArrayList<>();

            try {

                Integer messageCount = 0;
                
                LOGGER.debug("start sending messages ");
                
                // loop through all rescievers
                for (MsgReceiver msgReceiver : vm.distributionList) {

                    LOGGER.debug("start with message number " + (messageCount + 1));

                    IDeliveryMsgHandler iSendMsg = msgHandlerList.get(msgReceiver.type);
                    if (iSendMsg != null && vm.getMessageLocalVersions() != null && !vm.getMessageLocalVersions().isEmpty()) {
                        List<MsgToSend> messageLocalVersions = vm.getMessageLocalVersions();

                        // set a default message to send
                        MsgToSend msgToSend = null;

                        if (msgReceiver.preferredLocale != null) {
                            for (MsgToSend msgTo : messageLocalVersions) {
                                if (msgReceiver.preferredLocale.equals(msgTo.getLocale())) {
                                    msgToSend = msgTo;
                                    LOGGER.debug("use correct locale " + msgTo.getLocale());
                                    break;
                                }
                            }
                        }

                        if (msgToSend == null) {
                            for (MsgToSend msgTo : messageLocalVersions) {
                                if (msgLocaleIfNoMatch.equals(msgTo.getLocale())) {
                                    msgToSend = msgTo;

                                    LOGGER.debug("use system locale " + msgTo.getLocale());

                                    break;
                                }
                            }
                        }

                        if (msgToSend == null) {
                            LOGGER.debug("No loacale found for " + msgReceiver.preferredLocale);
                            msgToSend = messageLocalVersions.get(0);
                        }

                        SingleMsgSentStateEnum sendMessageResult = iSendMsg.sendMessage(vm, msgReceiver, msgToSend);

                        if (!SingleMsgSentStateEnum.Ok.equals(sendMessageResult)) {
                            System.out.println(
                                    "Send message " + msgToSend.msgBody + " of type " + msgReceiver.type + " failed for " + msgReceiver.msgDeliveryAddress);
                        } else {
                            messageCount++;
                        }

                        LOGGER.debug("Valid handler of type " + iSendMsg.getDistributionType() + " found for " + msgReceiver.msgDeliveryAddress
                                + " send mesage result " + sendMessageResult);
                        SingleMsgSendResult msgSendResult = new SingleMsgSendResult(msgReceiver, sendMessageResult);
                        msgSentResult.add(msgSendResult);

                    } else {
                        SingleMsgSendResult msgSendResult = new SingleMsgSendResult(msgReceiver, SingleMsgSentStateEnum.FailedNotSentNoDistType);
                        msgSentResult.add(msgSendResult);

                        System.out.println("No valid handler of type " + msgReceiver.type + " found for " + msgReceiver.msgDeliveryAddress);
                    }
                    
                        LOGGER.debug("done with message number " + messageCount);

                }

                if (messageCount == 0) {
                    vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSent;
                    vipsSendMsgResult.numRecipients = 0;
                } else if (messageCount != vm.distributionList.size()) {
                    vipsSendMsgResult.status = MsgDistResultEnum.SomeMessagesFailed;
                    vipsSendMsgResult.numRecipients = messageCount;
                } else {
                    vipsSendMsgResult.status = MsgDistResultEnum.Ok;
                }
                
                LOGGER.debug("Done sending messages " + messageCount);

            } catch (Exception e) {
                vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSentWithException;
                LOGGER.error("Failed to send message",e);
            } catch (Throwable e1) {
                vipsSendMsgResult.status = MsgDistResultEnum.FailedNotSentWithException;
                LOGGER.error("Failed to send message",e1);
            } finally {
                CSVTrafficFileLogger.writeCsvFilerReceived(fileNameSentResult, msgSentResult);

            }

            

            return vipsSendMsgResult;

        }

    }

    /**
     * @return the smtpMailserver
     */
    public String getSmtpMailserver() {
        if(this.smtpMailserver == null)
        {
            this.smtpMailserver = System.getProperty("no.nibio.vips.logic.SMTP_SERVER");
        }
        return smtpMailserver;
    }

    /**
     * @param smtpMailserver the smtpMailserver to set
     */
    public void setSmtpMailserver(String smtpMailserver) {
        this.smtpMailserver = smtpMailserver;
    }

    /**
     * @return the mailSenderAddress
     */
    public String getMailSenderAddress() {
        if(this.mailSenderAddress == null)
        {
            this.mailSenderAddress = System.getProperty("no.nibio.vips.logic.messaging.dist.MAIL_SENDER_ADDRESS");
        }
        return mailSenderAddress;
    }

    /**
     * @param mailSenderAddress the mailSenderAddress to set
     */
    public void setMailSenderAddress(String mailSenderAddress) {
        this.mailSenderAddress = mailSenderAddress;
    }

    /**
     * @return the simumlateMailSending. DEFAULT is false
     */
    public boolean isSimumlateMailSending() {
        if(this.simulateMailSending == null)
        {
            String simulateMailsendingProp = System.getProperty("no.nibio.vips.logic.messaging.dist.SIMULATE_MAIL_SENDING");
            this.simulateMailSending = simulateMailsendingProp == null ? true: simulateMailsendingProp.equals("true");
        }
        return simulateMailSending;
    }

    /**
     * @param simumlateMailSending the simumlateMailSending to set
     */
    public void setSimumlateMailSending(boolean simumlateMailSending) {
        this.simulateMailSending = simumlateMailSending;
    }

    /**
     * @return the trafficLogDirectory
     */
    public String getTrafficLogDirectory() {
        if(this.trafficLogDirectory == null)
        {
            this.trafficLogDirectory = System.getProperty("no.nibio.vips.logic.messaging.dist.TRAFFIC_LOG_DIRECTORY");
        }
        return trafficLogDirectory;
    }

    /**
     * @param trafficLogDirectory the trafficLogDirectory to set
     */
    public void setTrafficLogDirectory(String trafficLogDirectory) {
        this.trafficLogDirectory = trafficLogDirectory;
    }

    /**
     * @return the smscServer
     */
    public String getSmscServer() {
        if(this.smscServer == null)
        {
            this.smscServer = System.getProperty("no.nibio.vips.logic.messaging.dist.SMS_SERVER");
        }
        return smscServer;
    }

    /**
     * @param smscServer the smscServer to set
     */
    public void setSmscServer(String smscServer) {
        this.smscServer = smscServer;
    }

    /**
     * @return the smscUsername
     */
    public String getSmscUsername() {
        if(this.smscUsername == null)
        {
            this.smscUsername = System.getProperty("no.nibio.vips.logic.messaging.dist.SMS_USERNAME");
        }
        return smscUsername;
    }

    /**
     * @param smscUsername the smscUsername to set
     */
    public void setSmscUsername(String smscUsername) {
        this.smscUsername = smscUsername;
    }

    /**
     * @return the smscPassword
     */
    public String getSmscPassword() {
        if(this.smscPassword == null)
        {
            this.smscPassword = System.getProperty("no.nibio.vips.logic.messaging.dist.SMS_PASSWORD");
        }
        return smscPassword;
    }

    /**
     * @param smscPassword the smscPassword to set
     */
    public void setSmscPassword(String smscPassword) {
        this.smscPassword = smscPassword;
    }

    /**
     * @return the smscSenderSrc
     */
    public String getSmscSenderSrc() {
        if(this.smscSenderSrc == null)
        {
            this.smscSenderSrc = System.getProperty("no.nibio.vips.logic.messaging.dist.SMS_SENDER_SRC");
        }
        return smscSenderSrc;
    }

    /**
     * @param smscSenderSrc the smscSenderSrc to set
     */
    public void setSmscSenderSrc(String smscSenderSrc) {
        this.smscSenderSrc = smscSenderSrc;
    }

    /**
     * @return the executorService
     */
    public ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * @param executorService the executorService to set
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * @return the msgLocaleIfNoMatch
     */
    public String getMsgLocaleIfNoMatch() {
        return msgLocaleIfNoMatch;
    }

    /**
     * @param msgLocaleIfNoMatch the msgLocaleIfNoMatch to set
     */
    public void setMsgLocaleIfNoMatch(String msgLocaleIfNoMatch) {
        this.msgLocaleIfNoMatch = msgLocaleIfNoMatch;
    }
}
