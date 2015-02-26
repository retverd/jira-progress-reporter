package ru.retverd.jira.reporter.progress.tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.retverd.jira.reporter.progress.PropertyHolder;
import ru.retverd.jira.reporter.progress.Starter;

public class PropertiesTests {
	@Test
	public void testStartWithoutArgs() throws Exception {
		String[] args = new String[] {};
		try {
			Starter.main(args);
			Assert.fail("Exception expected, but not thrown!");
		} catch (IOException expected) {
			Assert.assertEquals(expected.getMessage(),
					"Missing path to file with properties!",
					"Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testMissingPropertiesFile() throws Exception {
		String filename = "missing.properties";
		try {
			new PropertyHolder(filename);
			Assert.fail("Exception expected, but not thrown!");
		} catch (FileNotFoundException expected) {
			Assert.assertEquals(expected.getMessage(), filename
					+ " (The system cannot find the file specified)",
					"Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testMissingStringProperty() throws Exception {
		String filename = "src\\test\\resources\\missing_string.properties";
		try {
			new PropertyHolder(filename);
			Assert.fail("Exception expected, but not thrown!");
		} catch (IOException expected) {
			Assert.assertEquals(expected.getMessage(),
					"Missing excel.tab.forbidden.marker property in file "
							+ filename, "Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testMissingIntProperty() throws Exception {
		String filename = "src\\test\\resources\\missing_int.properties";
		try {
			new PropertyHolder(filename);
			Assert.fail("Exception expected, but not thrown!");
		} catch (IOException expected) {
			Assert.assertEquals(expected.getMessage(),
					"Missing excel.update.row property in file " + filename,
					"Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testStringNotIntProperty() throws Exception {
		String filename = "src\\test\\resources\\string_not_int.properties";
		try {
			new PropertyHolder(filename);
			Assert.fail("Exception expected, but not thrown!");
		} catch (IOException expected) {
			Assert.assertEquals(expected.getMessage(),
					"Integer value is expected in property excel.update.row in file "
							+ filename, "Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testInacceptableProperty() throws Exception {
		String filename = "src\\test\\resources\\inacceptable.properties";
		try {
			new PropertyHolder(filename);
			Assert.fail("Exception expected, but not thrown!");
		} catch (IOException expected) {
			Assert.assertEquals(
					expected.getMessage(),
					"excel.issue.description.fill property in file "
							+ filename
							+ " contains inacceptable value: yes. Only values n, y are acceptable.",
					"Wrong error message:");
		} catch (Exception e) {
			IOException exp = new IOException();
			Assert.fail("Wrong exception: expected [" + e.getClass()
					+ "] but found [" + exp.getClass() + "] with message "
					+ e.getMessage());
		}
	}

	@Test
	public void testMissingOptionalProperty() throws Exception {
		String filename = "src\\test\\resources\\missing_optional.properties";
		PropertyHolder props = null;

		try {
			props = new PropertyHolder(filename);
		} catch (Exception e) {
			Assert.fail("No assertion was expected, but exception "
					+ e.getClass() + " with message \"" + e.getMessage()
					+ "\" was thrown!");
		}

		Assert.assertEquals(props.getIssueDescriptionFill(), "n",
				"Incorrect value used by default");
	}

	@Test
	public void testAllCorrectProperties() throws Exception {
		String filename = "src\\test\\resources\\all_correct.properties";
		try {
			new PropertyHolder(filename);
		} catch (Exception e) {
			Assert.fail("No assertion was expected, but exception "
					+ e.getClass() + " with message \"" + e.getMessage()
					+ "\" was thrown!");
		}
	}
}