package ru.retverd.jira.reporter.progress;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.naming.ConfigurationException;

public class Starter {
    private static final Logger log = Logger.getLogger(Starter.class.getName());

    public static void main(String[] args) throws Throwable {
        DOMConfigurator.configure("log4j.xml");
        log.info("Starting!");

        if (args.length < 2) {
            log.fatal("Missing required parameters! See default.bat for more details.");
            throw new ConfigurationException("Missing required parameters! See default.bat for more details.");
        }

        // Load and check required files
        ProgressReporter reportHandler = new ProgressReporter(args);

        try {
            // Update report with new values from Jira
            reportHandler.updateReport();
            // Save updated report
            reportHandler.saveReport();
        } finally {
            reportHandler.disconnect();
        }
        log.info("Report update was successfully completed!");
        System.out.println("Press Enter to exit...");
        System.in.read();
    }
}