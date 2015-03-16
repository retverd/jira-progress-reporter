package ru.retverd.jira.reporter.progress;

import java.io.Console;
import java.io.IOException;
import java.util.Scanner;

public class Starter {
    public static void main(String[] args) throws Exception {
        // All things related to prompt
        String pass;
        String login;
        String loginPrompt = "Please enter your login: ";
        String passPrompt = "Please enter your password: ";

        if (args.length < 2) {
            throw new IOException("Missing required parameters! See default.bat for more details.");
        }

        // Load and check required files
        ProgressReporter reportHandler = new ProgressReporter(args[0], args[1]);

        // Request credentials for JIRA
        // TODO handle missing credentials!
        if (args.length == 4) {
            login = args[2];
            pass = args[3];
        } else {
            Console console = System.console();
            System.out.print(loginPrompt);
            if (console == null) {
                Scanner in = new Scanner(System.in);
                login = in.next();
                System.out.print(passPrompt);
                pass = in.next();
                in.close();
            } else {
                login = console.readLine();
                System.out.print(passPrompt);
                pass = new String(console.readPassword());
            }
        }

        // Connect to JIRA
        reportHandler.connectToJIRA(login, pass);

        try {
            // Update report with new values from Jira
            reportHandler.updateReport();
            // Save updated report
            reportHandler.saveReport(args[1]);
        } finally {
            reportHandler.disconnectFromJIRA();
        }
    }
}