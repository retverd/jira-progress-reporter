package ru.retverd.jira.reporter.progress.tests;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.retverd.jira.reporter.progress.PropertyHolder;

public class PropertiesTests {
    @Test
    public void testMissingPropertiesFile() throws Exception {
	String filename = "missing.properties";
	try {
	    new PropertyHolder(filename);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (FileNotFoundException expected) {
	    Assert.assertEquals(expected.getMessage(), filename + " (The system cannot find the file specified)", "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testMissingStringProperty() throws Exception {
	String filename = "src\\test\\resources\\missing_string.properties";
	try {
	    new PropertyHolder(filename);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "Missing excel.tab.forbidden.marker property in file " + filename, "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testMissingIntProperty() throws Exception {
	String filename = "src\\test\\resources\\missing_int.properties";
	try {
	    new PropertyHolder(filename);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "Missing excel.update.row property in file " + filename, "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testStringNotIntProperty() throws Exception {
	String filename = "src\\test\\resources\\string_not_int.properties";
	try {
	    new PropertyHolder(filename);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "Integer value is expected in property excel.update.row in file " + filename, "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testOutOfListProperty() throws Exception {
	String filename = "src\\test\\resources\\out_of_list.properties";
	try {
	    new PropertyHolder(filename);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "excel.issue.summary.fill property in file " + filename
		    + " contains inacceptable value: yes. Only values n, y are acceptable.", "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testMissingOptionalFlagProperty() throws Exception {
	String filename = "src\\test\\resources\\missing_optional.properties";
	PropertyHolder props = null;

	try {
	    props = new PropertyHolder(filename);
	} catch (Exception e) {
	    Assert.fail("No assertion was expected, but exception " + e.getClass() + " with message \"" + e.getMessage() + "\" was thrown!");
	}

	Assert.assertEquals(props.getIssueSummaryFill(), false, "Incorrect value used by default");
    }

    @Test
    public void testMissingOptionalStringProperty() throws Exception {
	String filename = "src\\test\\resources\\missing_pattern.properties";
	PropertyHolder sample = null;
	try {
	    sample = new PropertyHolder(filename);
	} catch (Exception e) {
	    Assert.fail("No assertion was expected, but exception " + e.getClass() + " with message \"" + e.getMessage() + "\" was thrown!");
	}
	Assert.assertEquals(sample.getReportFilenamePattern(), null, "Incorrect result for property report.filename.pattern");
    }

    @Test
    public void testAllCorrectProperties() throws Exception {
	String filename = "src\\test\\resources\\all_correct.properties";
	PropertyHolder sample = null;
	try {
	    sample = new PropertyHolder(filename);
	} catch (Exception e) {
	    Assert.fail("No assertion was expected, but exception " + e.getClass() + " with message \"" + e.getMessage() + "\" was thrown!");
	}
	Assert.assertEquals(sample.getIssueSummaryFill(), true, "Incorrect result for property excel.issue.summary.fill");
	Assert.assertEquals(sample.getRecalculateFormulas(), false, "Incorrect result for property excel.recalculate.formulas");
	Assert.assertEquals(sample.getReportFilenamePattern(), "yyyy.MM.dd_HH_mm_ss", "Incorrect result for property report.filename.pattern");
    }
}