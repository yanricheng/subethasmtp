package org.subethamail.smtp.netty.cmd.impl;

import com.github.davidmoten.guavamini.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.smtp.DropConnectionException;
import org.subethamail.smtp.internal.util.SMTPResponseHelper;
import org.subethamail.smtp.netty.session.SmtpSession;

import java.io.IOException;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 * @author Jon Stevens
 * @author Jeff Schnitzer
 */
public final class DataCmd extends BaseCmd {
    private final static int BUFFER_SIZE = 1024 * 32; // 32k seems reasonable
    private static Logger logger = LoggerFactory.getLogger(DataCmd.class);

    public DataCmd() {
        super("DATA", "Following text is collected as the message.\n"
                + "End data with <CR><LF>.<CR><LF>");
    }

    @Override
    public void execute(String commandString, SmtpSession sess)
            throws IOException, DropConnectionException {
        if (!sess.isMailTransactionInProgress() || !sess.getMail().isPresent()) {
            sess.sendResponse("503 5.5.1 Error: need MAIL command");
            return;
        } else if (sess.getRecipientCount() == 0) {
            sess.sendResponse("503 Error: need RCPT command");
            return;
        }

        //第一次执行
        if (!getName().equalsIgnoreCase(sess.getLastCmdName())) {
            sess.sendResponse("354 End data with <CR><LF>.<CR><LF>");
            //判断是否要加头
            if (!sess.isDurativeCmd()) {
                if (!sess.getSmtpConfig().isDisableReceivedHeaders()) {
                    sess.getMail().get().getData().append(generateHead(sess.getHelo(),
                            sess.getRemoteAddress(), sess.getSmtpConfig().getHostName(),
                            Optional.of(sess.getSmtpConfig().getSoftwareName()), sess.getId(),
                            sess.getSingleRecipient()));
                }
            }
            sess.setDurativeCmd(true);
            return;
        }

        String dataFrame = sess.getDataFrame();
        //一直都数据，直接遇到<CR><LF>.<CR><LF>
        if (!".".equals(dataFrame)) {
            sess.getMail().get().getData().append(dataFrame).append("\n\r");
            sess.setDurativeCmd(true);
            return;
        } else {
            try {
                sess.getMail().get().getData().append("\n\r");
                String mailDate = sess.getMail().get().getData().toString();
                logger.info(">>> receive email data:\n\r{}", mailDate);

                String handleResult = handleDate(mailDate);
                if (null != handleResult) {
                    sess.sendResponse(SMTPResponseHelper.buildResponse("250", handleResult));
                } else {
                    sess.sendResponse("250 Ok");
                }
            } finally {
                sess.setDurativeCmd(false);
                sess.setMailTransactionInProgress(false);
                sess.resetMailTransaction();
            }

        }


    }

    private String handleDate(String mailDate) {
        return null;
    }

    public String generateHead(Optional<String> heloHost, InetAddress host, String whoami,
                               Optional<String> softwareName, String id, Optional<String> singleRecipient) {
        /*
         * Looks like: Received: from iamhelo (wasabi.infohazard.org
         * [209.237.247.14]) by mx.google.com with SMTP id
         * 32si2669129wfa.13.2009.05.27.18.27.31; Wed, 27 May 2009 18:27:48
         * -0700 (PDT)
         */
        Preconditions.checkNotNull(heloHost);
        Preconditions.checkNotNull(softwareName);
        Preconditions.checkNotNull(singleRecipient);
        DateFormat fmt = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z (z)", Locale.US);
        String timestamp = fmt.format(new Date());

        StringBuilder header = new StringBuilder();
        header.append("Received: from ").append(heloHost.orElse(null)).append(" (").append(constructTcpInfo(host)).append(")\r\n");
        header.append("        by ").append(whoami).append("\r\n");
        header.append("        with SMTP");
        if (softwareName.isPresent()) {
            header.append(" (").append(softwareName.get()).append(")");
        }
        header.append(" id ").append(id);
        if (singleRecipient.isPresent()) {
            header.append("\r\n        for ").append(singleRecipient.get());
        }
        header.append(";\r\n");
        header.append("        ").append(timestamp).append("\r\n");

        return header.toString();
    }

    /**
     * Returns a formatted TCP-info element, depending on the success of the IP
     * address name resolution either with domain name or only the address
     * literal.
     *
     * @param host the address of the remote SMTP client.
     * @return the formatted TCP-info element as defined by RFC 5321
     */
    private String constructTcpInfo(InetAddress host) {
        // if it is not successful it just returns the address
        String domain = host.getCanonicalHostName();
        String address = host.getHostAddress();
        // check whether the host name resolution was successful
        if (domain.equals(address)) {
            return "[" + address + "]";
        } else {
            return domain + " [" + address + "]";
        }
    }
}
