package ru.retverd.jira.reporter.progress;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ru.retverd.jira.helpers.JiraSimplifiedClient;

public class Starter {
	public static void main(String[] args) throws Exception {
		// All things related to prompt
		String pass;
		String login;
		String loginPrompt = "Please enter your login: ";
		String passPrompt = "Please enter your password: ";

		if (args.length < 2) {
			throw new IOException(
					"Missing required parameters! See default.bat for more details.");
		}

		String propertyFile = args[0];
		String reportFile = args[1];

		// Load and check file with properties
		System.out.format("Loading and parsing properties from " + propertyFile
				+ "...");
		PropertyHolder properties = new PropertyHolder(propertyFile);
		System.out.format("done!%n");

		// Load Excel file with report base
		// TODO handle missing file!
		System.out.format("Reading file " + reportFile + "...");
		FileInputStream fis = new FileInputStream(reportFile);
		XSSFWorkbook workbook = new XSSFWorkbook(fis);
		fis.close();
		System.out.format("done!%n");

		// Request credentials for JIRA
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

		// Open connection to Jira
		// TODO handle incorrect credentials!
		JiraSimplifiedClient jc = new JiraSimplifiedClient(
				properties.getJiraURL(), login, pass);

		try {
			// Update report with new values from Jira
			workbook = ProgressReporter.updateReport(properties, workbook, jc);

			// Rewrite Excel file
			System.out.format("Rewriting file " + reportFile + "...");
			FileOutputStream fos = new FileOutputStream(new File(reportFile));
			workbook.write(fos);
			fos.close();
			System.out.format("done!%n");
		} finally {
			jc.closeConnection();
		}
	}
}