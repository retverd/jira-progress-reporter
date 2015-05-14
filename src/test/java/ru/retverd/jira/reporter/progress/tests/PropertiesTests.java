package ru.retverd.jira.reporter.progress.tests;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;
import ru.retverd.jira.reporter.progress.ProgressReporter;

import javax.naming.ConfigurationException;
import javax.xml.bind.UnmarshalException;
import java.io.FileNotFoundException;
import java.util.Locale;

public class PropertiesTests {
    @Test
    public void testMissingConfigFile() {
        String missingConfig = "missing_config.xml";
        try {
            ProgressReporter reporter = new ProgressReporter();
            reporter.loadConfigFile(missingConfig);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getCause().getClass(), UnmarshalException.class, "Wrong root cause.");
            UnmarshalException e = (UnmarshalException) ex.getCause();
            Assert.assertEquals(e.getLinkedException().getClass(), FileNotFoundException.class, "Wrong linked exception:");
            Assert.assertEquals(e.getLinkedException().getMessage(), System.getProperty("user.dir") + "\\" + missingConfig + " (The system cannot find the file specified)", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(groups = "core")
    public void testAllProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\all_properties.xml";
        DateTime dateTime = new DateTime();

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertNotNull(reporter.getConfig(), "Config class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getLocale(), Locale.ENGLISH, "Locale is invalid!");
        Assert.assertNotNull(reporter.getConfig().getJira(), "Jira class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getJira().getUrl(), "https://jira.atlassian.com", "Jira URL is invalid!");
        Assert.assertEquals(reporter.getConfig().getJira().isAnonymous(), true, "Jira anonymous access flag is invalid!");
        Assert.assertNotNull(reporter.getConfig().getJira().getProxy(), "Proxy class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getJira().getProxy().getHost(), "127.0.0.1", "Proxy host is invalid!");
        Assert.assertEquals(reporter.getConfig().getJira().getProxy().getPort(), "8080", "Proxy port is invalid!");
        Assert.assertNotNull(reporter.getConfig().getJira().getProjects(), "Jira projects class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getJira().getProjects().size(), 2, "Size of projects list is invalid!");
        Assert.assertEquals(reporter.getConfig().getJira().getProjects().get(0), "PRJ1", "First links in list is invalid!");
        Assert.assertEquals(reporter.getConfig().getJira().getProjects().get(1), "PRJ2", "Second links in list is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport(), "Report class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getMarker(), "# ", "Marker of regular sheet is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getUpdateDate(), "UpdateDate class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getUpdateDate().getUpdateTime(dateTime, reporter.getConfig().getLocale()), DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ").withLocale(Locale.ENGLISH).print(dateTime), "Template for update date is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getUpdateDate().getRow(), 3, "Row for update date is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getUpdateDate().getCol(), 5, "Column for update date is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getToHide(), "ToHide class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getIssuePrefixRow(), Integer.valueOf(2), "Row for issue prefix is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getIssuePrefixCol(), Integer.valueOf(0), "Column for issue prefix is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getAffectedVersionRow(), Integer.valueOf(2), "Row for affected version is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getAffectedVersionCol(), Integer.valueOf(8), "Column for affected version is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getComponentsRow(), Integer.valueOf(2), "Row for components is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getComponentsCol(), Integer.valueOf(9), "Column for components is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getLabelsRow(), Integer.valueOf(2), "Row for labels is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getToHide().getLabelsCol(), Integer.valueOf(10), "Column for labels is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getJqlQuery(), "JQL request class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getJqlQuery().getCol(), 1, "Column for JQL request is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getJqlQuery().getRow(), 2, "Row for JQL request is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getRootIssue(), "Root Issue class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getRootIssue().isUnfoldSubtasks(), true, "UnfoldSubtasks flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getRootIssue().getIssueKeyCol(), 3, "Column for root issue is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getRootIssue().getIssueKeyRow(), 2, "Row for root issue is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getRootIssue().getLinks(), "Issue links class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getRootIssue().getLinks().get(0), "relates to", "Column for link types is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getRootIssue().getLinks().get(1), "is duplicated by", "Row for link types is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getStartProcessingRow(), 5, "Start processing issueKeyRow is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getIssueColumns(), "Issue columns class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getSummary(), Integer.valueOf(0), "Column for issue summary is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getKey(), Integer.valueOf(1), "Column for issue key is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getRelation(), Integer.valueOf(2), "Column for issue relation is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getParentKey(), Integer.valueOf(3), "Column for issue parent key is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getEstimation(), Integer.valueOf(4), "Column for issue estimation is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getSpentTime(), Integer.valueOf(5), "Column for issue spent time is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getRemainingTime(), Integer.valueOf(6), "Column for issue remaining time is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getStatus(), Integer.valueOf(7), "Column for issue status is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getAfVersion(), Integer.valueOf(8), "Column for issue affected version(s) is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getDueDate(), Integer.valueOf(9), "Column for issue component(s) is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getComponents(), Integer.valueOf(10), "Column for issue component(s) is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getLabels(), Integer.valueOf(11), "Column for issue label(s) is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getAssignee(), Integer.valueOf(12), "Column for issue assignee is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getDueDateFormat(), "dd.mmm.yy", "Time tracking format is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getTimeTrackingFormat(), "0.00", "Time tracking format is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getProcessingFlags(), "Processing flags class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isIssueSummaryUpdate(), true, "IssueSummaryUpdate flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isRecalculateFormulas(), false, "RecalculateFormulas flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isAutosizeColumns(), false, "AutosizeColumns flag is invalid!");
        Assert.assertNotNull(reporter.getConfig().getReport().getReportName(), "Report name class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getReportName().getFullName(dateTime, reporter.getConfig().getLocale()), "prefix_" + DateTimeFormat.forPattern("yyyy.MM.dd_HH_mm_ss").withLocale(Locale.ENGLISH).print(dateTime) + "_suffix", "Report name is invalid!");
    }

    @Test(dependsOnMethods = "testAllProperties", groups = "core")
    public void testMandatoryProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\only_mandatory_properties.xml";

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertNotNull(reporter.getConfig(), "Config class was not instantiated!");
        Assert.assertNull(reporter.getConfig().getLocale(), "Locale was instantiated!");
        Assert.assertNotNull(reporter.getConfig().getJira(), "Jira class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getJira().getUrl(), "https://jira.atlassian.com", "Jira URL is invalid!");
        Assert.assertEquals(reporter.getConfig().getJira().isAnonymous(), false, "Jira anonymous access flag is invalid!");
        Assert.assertNull(reporter.getConfig().getJira().getProxy(), "Proxy class was instantiated!");
        Assert.assertNull(reporter.getConfig().getJira().getProjects(), "Jira projects class was instantiated!");
        Assert.assertNotNull(reporter.getConfig().getReport(), "Report class was not instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getMarker(), "# ", "Marker of regular sheet is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide(), "ToHide class was instantiated!");
        Assert.assertNull(reporter.getConfig().getReport().getJqlQuery(), "JQL request class was instantiated!");
        Assert.assertNull(reporter.getConfig().getReport().getRootIssue(), "Root Issue class was instantiated!");
        Assert.assertNotNull(reporter.getConfig().getReport().getIssueColumns(), "Issue columns class was not instantiated!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getSummary(), "Column for issue summary is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getIssueColumns().getKey(), Integer.valueOf(1), "Column for issue key is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getRelation(), "Column for issue relation is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getParentKey(), "Column for issue parent key is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getEstimation(), "Column for issue estimation is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getSpentTime(), "Column for issue spent time is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getRemainingTime(), "Column for issue remaining time is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getStatus(), "Column for issue status is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getAfVersion(), "Column for issue affected version(s) is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getComponents(), "Column for issue component(s) is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getLabels(), "Column for issue label(s) is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getIssueColumns().getAssignee(), "Column for issue assignee is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getTimeTrackingFormat(), "Time tracking format is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getProcessingFlags(), "Processing flags class was instantiated!");
        Assert.assertNull(reporter.getConfig().getReport().getReportName(), "Report name class was instantiated!");
    }

    @Test(dependsOnGroups = "core")
    public void testMixedBooleanProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\mixed_boolean_properties.xml";

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertEquals(reporter.getConfig().getJira().isAnonymous(), true, "Jira anonymous access flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isIssueSummaryUpdate(), true, "IssueSummaryUpdate flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isRecalculateFormulas(), false, "RecalculateFormulas flag is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getProcessingFlags().isAutosizeColumns(), false, "AutosizeColumns flag is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testEmptyCellsProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\empty_cells_properties.xml";

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertNotNull(reporter.getConfig().getReport().getToHide(), "ToHide class was not instantiated!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getIssuePrefixRow(), "Row for issue prefix is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getIssuePrefixCol(), "Column for issue prefix is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getAffectedVersionRow(), "Row for affected version is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getAffectedVersionCol(), "Column for affected version is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getComponentsRow(), "Row for components is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getComponentsCol(), "Column for components is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getLabelsRow(), "Row for labels is invalid!");
        Assert.assertNull(reporter.getConfig().getReport().getToHide().getLabelsCol(), "Column for labels is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testConfigFileValidation() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_mandatory_properties.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getCause().getClass(), UnmarshalException.class, "Wrong cause exception class:");
            UnmarshalException cause = (UnmarshalException) ex.getCause();
            Assert.assertEquals(cause.getLinkedException().getClass(), SAXParseException.class, "Wrong linked exception class:");
            Assert.assertEquals(cause.getLinkedException().getMessage(), "cvc-complex-type.2.4.b: The content of element 'config' is not complete. One of '{jira, locale}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testMissingProjectProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_project_properties.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getCause().getClass(), UnmarshalException.class, "Wrong cause exception class:");
            UnmarshalException cause = (UnmarshalException) ex.getCause();
            Assert.assertEquals(cause.getLinkedException().getClass(), SAXParseException.class, "Wrong linked exception class:");
            Assert.assertEquals(cause.getLinkedException().getMessage(), "cvc-complex-type.2.4.b: The content of element 'projects' is not complete. One of '{entry}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testMissingLinksProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_links_properties.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getCause().getClass(), UnmarshalException.class, "Wrong cause exception class:");
            UnmarshalException cause = (UnmarshalException) ex.getCause();
            Assert.assertEquals(cause.getLinkedException().getClass(), SAXParseException.class, "Wrong linked exception class:");
            Assert.assertEquals(cause.getLinkedException().getMessage(), "cvc-complex-type.2.4.b: The content of element 'links' is not complete. One of '{entry}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testEmptyReportName() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\empty_report_name.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "At least one field for tag <reportName> should be filled!", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testEmptyTimePattern() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\empty_time_pattern.xml";

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertEquals(reporter.getConfig().getReport().getReportName().getFullName(new DateTime(), reporter.getConfig().getLocale()), "prefix_", "Report name is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testMissingTimePattern() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_time_pattern.xml";

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertEquals(reporter.getConfig().getReport().getReportName().getFullName(new DateTime(), reporter.getConfig().getLocale()), "prefix__suffix", "Report name is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testEmptyTimePatternForUpdateDate() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\empty_timepattern_updatedate.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "Invalid pattern specification for field config -> report -> updateDate -> timePattern", "Wrong error message:");
            Assert.assertEquals(ex.getCause().getClass(), IllegalArgumentException.class, "Wrong cause: ");
            Assert.assertEquals(ex.getCause().getMessage(), "Invalid pattern specification", "Wrong cause message: ");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testDefaultLocale() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\default_locale.xml";
        DateTime dateTime = new DateTime();

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertNull(reporter.getConfig().getLocale(), "Locale class was instantiated!");
        Assert.assertEquals(reporter.getConfig().getReport().getUpdateDate().getUpdateTime(dateTime, reporter.getConfig().getLocale()), DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ").print(dateTime), "Template for update date is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getReportName().getFullName(dateTime, reporter.getConfig().getLocale()), DateTimeFormat.forPattern("yyyy.MM.dd_HH_mm_ss").print(dateTime), "Report name is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testGermanLocale() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\german_locale.xml";

        DateTime dateTime = new DateTime();

        ProgressReporter reporter = new ProgressReporter();
        reporter.loadConfigFile(configFile);

        Assert.assertEquals(reporter.getConfig().getLocale(), Locale.GERMAN, "Locale is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getUpdateDate().getUpdateTime(dateTime, reporter.getConfig().getLocale()), DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ").withLocale(Locale.GERMAN).print(dateTime), "Template for update date is invalid!");
        Assert.assertEquals(reporter.getConfig().getReport().getReportName().getFullName(dateTime, reporter.getConfig().getLocale()), DateTimeFormat.forPattern("yyyy.MM.dd_HH_mm_ss").withLocale(Locale.GERMAN).print(dateTime), "Report name is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testWrongLocale() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\wrong_locale.xml";

        ProgressReporter reporter = new ProgressReporter();

        try {
            reporter.loadConfigFile(configFile);
            Assert.fail("Exception " + ConfigurationException.class + " expected, but not thrown!");
        } catch (ConfigurationException ex) {
            Assert.assertEquals(ex.getMessage(), "class javax.xml.bind.JAXBException thrown with linked exception class org.xml.sax.SAXParseException and message: cvc-enumeration-valid: Value 'RUSSIAN' is not facet-valid with respect to enumeration '[ENGLISH, GERMAN]'. It must be a value from the enumeration.", "Wrong error message:");
            Assert.assertEquals(ex.getCause().getClass(), UnmarshalException.class, "Wrong cause: ");
            Assert.assertNull(ex.getCause().getMessage(), "Cause message is not null!");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + ConfigurationException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }
//    TODO Add test for locale added in XSD, but missing in Java
//    @Test(dependsOnGroups = "core")
//    public void testUnexpectedLocale() throws Throwable {
//    }
}