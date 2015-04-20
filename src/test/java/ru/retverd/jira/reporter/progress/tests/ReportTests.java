package ru.retverd.jira.reporter.progress.tests;

import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.retverd.jira.reporter.progress.ProgressReporter;

import javax.naming.ConfigurationException;

public class ReportTests {
    @Test
    public void testMissingReportFile() throws Exception {
        String missingReportTemplate = "missing_report_template.xlsx";
        try {
            ProgressReporter reporter = new ProgressReporter();
            reporter.loadReportTemplate(missingReportTemplate);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), System.getProperty("user.dir") + "\\" + missingReportTemplate + " (The system cannot find the file specified)", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test
    public void testInvalidReportFile() throws Exception {
        String missingReportTemplate = "src\\test\\resources\\templates\\wrong_format.jpg";
        try {
            ProgressReporter reporter = new ProgressReporter();
            reporter.loadReportTemplate(missingReportTemplate);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (InvalidOperationException ex) {
            Assert.assertEquals(ex.getMessage(), "Can't open the specified file: '" + missingReportTemplate + "'", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + InvalidOperationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }
}
