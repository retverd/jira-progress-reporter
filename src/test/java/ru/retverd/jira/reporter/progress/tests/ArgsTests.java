package ru.retverd.jira.reporter.progress.tests;

import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import ru.retverd.jira.reporter.progress.Starter;

public class ArgsTests {
    @Test
    public void testStartWithoutArgs() throws Exception {
	String[] args = new String[] {};
	try {
	    Starter.main(args);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "Missing required parameters! See default.bat for more details.", "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + e.getClass() + "] but found [" + exp.getClass() + "] with message " + e.getMessage());
	}
    }

    @Test
    public void testStartWithOneArg() throws Exception {
	String[] args = new String[] {"test string"};
	try {
	    Starter.main(args);
	    Assert.fail("Exception expected, but not thrown!");
	} catch (IOException expected) {
	    Assert.assertEquals(expected.getMessage(), "Missing required parameters! See default.bat for more details.", "Wrong error message:");
	} catch (Exception e) {
	    IOException exp = new IOException();
	    Assert.fail("Wrong exception: expected [" + e.getClass() + "] but found [" + exp.getClass() + "] with message " + e.getMessage());
	}
    }
}
