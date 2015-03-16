package ru.retverd.jira.reporter.progress.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import ru.retverd.jira.reporter.progress.Starter;

import java.io.IOException;

public class ReportTests {
    @Test
    public void testMissingReportFile() throws Exception {
        String[] args = new String[]{"src\\test\\resources\\all_correct.properties", "src\\test\\resources\\missing_report.xlsx"};

        try {
            Starter.main(args);
            Assert.fail("Exception expected, but not thrown!");
        } catch (IOException expected) {
            Assert.assertEquals(expected.getMessage(), args[1] + " (The system cannot find the file specified)", "Wrong error message:");
        } catch (Exception e) {
            IOException exp = new IOException();
            Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test
    public void testInvalidReportFile() throws Exception {
        String[] args = new String[]{"src\\test\\resources\\all_correct.properties", "src\\test\\resources\\all_correct.properties"};

        try {
            Starter.main(args);
            Assert.fail("Exception expected, but not thrown!");
        } catch (IOException expected) {
            Assert.assertEquals(expected.getMessage(), "Report file " + args[1] + " has incorrect format.", "Wrong error message:");
        } catch (Exception e) {
            IOException exp = new IOException();
            Assert.fail("Wrong exception: expected [" + exp.getClass() + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }
}
