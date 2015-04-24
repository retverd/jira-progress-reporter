package ru.retverd.jira.reporter.progress;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Starter {
    private static final Logger log = Logger.getLogger(Starter.class.getName());

    public static void main(String[] args) throws IOException {
        DOMConfigurator.configure("log4j.xml");
        log.info("Starting!");

        if (args.length < 2) {
            log.fatal("Missing required parameters! See default.bat for more details.");
        }

        ProgressReporter reportHandler = new ProgressReporter();

        try {
            reportHandler.loadConfigFile(args[0]);
            String login = null;
            String pass = null;
            if (args.length == 4) {
                login = args[2];
                pass = args[3];
            }
            reportHandler.connectToJira(login, pass);

            reportHandler.loadReportTemplate(args[1]);

            reportHandler.initObjects();

            reportHandler.updateReport();

            reportHandler.saveReport(args[1]);
        } catch (RuntimeException e) {
            if (e.getCause() != null) {
                if (e.getCause().getClass().equals(UnknownHostException.class)) {
                    log.fatal("Connection to JIRA failed! Please check your Internet connection!", e);
                } else if (e.getCause().getClass().equals(ConnectException.class)) {
                    log.fatal("Connection to JIRA failed! Please check your proxy settings!", e);
                } else if (e.getCause().getClass().equals(SocketException.class)) {
                    log.fatal("Connection was lost! Please check your Internet connection!", e);
                } else {
                    log.fatal(e.getClass() + ": " + e.getMessage(), e);
                }
            } else {
                log.fatal(e.getClass() + ": " + e.getMessage(), e);
            }

        } catch (ConfigurationException e) {
            // TODO something?
        } finally {
            reportHandler.disconnect();
        }

        if (reportHandler.isReportSaved()) {
            log.info("Report update was successfully completed!");
        } else {
            log.error("Report was not updated due to errors occurred! Please see error messages and log file for more details.");
        }

        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}