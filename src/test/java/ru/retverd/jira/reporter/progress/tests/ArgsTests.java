package ru.retverd.jira.reporter.progress.tests;

import org.apache.log4j.xml.DOMConfigurator;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import ru.retverd.jira.reporter.progress.Starter;

import javax.naming.ConfigurationException;

public class ArgsTests {
    @BeforeSuite
    public void preparation() {
        DOMConfigurator.configure("log4j.xml");
    }

    @Test
    public void testStartWithoutArgs() {
        String[] args = new String[]{};
        try {
            Starter.main(args);
            Assert.fail("Exception expected, but not thrown!");
        } catch (ConfigurationException expected) {
            Assert.assertEquals(expected.getMessage(), "Missing required parameters! See default.bat for more details.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test
    public void testStartWithOneArg() {
        String[] args = new String[]{"test string"};
        try {
            Starter.main(args);
            Assert.fail("Exception expected, but not thrown!");
        } catch (ConfigurationException expected) {
            Assert.assertEquals(expected.getMessage(), "Missing required parameters! See default.bat for more details.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }
}
