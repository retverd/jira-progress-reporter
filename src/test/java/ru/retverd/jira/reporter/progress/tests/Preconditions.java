package ru.retverd.jira.reporter.progress.tests;

import org.apache.log4j.xml.DOMConfigurator;
import org.testng.annotations.BeforeSuite;

/**
 * Created by rtverdok on 20.04.2015.
 */
public class Preconditions {
    @BeforeSuite
    public void preparation() {
        DOMConfigurator.configure("log4j.xml");
    }
}
