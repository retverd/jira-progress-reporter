package ru.retverd.jira.reporter.progress.tests;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.xml.sax.SAXParseException;
import ru.retverd.jira.reporter.progress.ProgressReporter;
import ru.retverd.jira.reporter.progress.types.ConfigType;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class PropertiesTests {
    @Test
    public void testMissingConfigFile() {
        String[] args = new String[]{"missing_config.xml", "default.xlsx"};
        try {
            new ProgressReporter(args);
            Assert.fail("Exception " + UnmarshalException.class + " expected, but not thrown!");
        } catch (FileNotFoundException ex) {
            Assert.assertEquals(ex.getMessage(), System.getProperty("user.dir") + "\\" + args[0] + " (The system cannot find the file specified)", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + IOException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(groups = "core")
    public void testAllProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\all_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        ConfigType sample = (ConfigType) unmarshaller.unmarshal(new File(configFile));
        Assert.assertNotNull(sample, "Config class was not instantiated!");
        Assert.assertNotNull(sample.getJira(), "Jira class was not instantiated!");
        Assert.assertEquals(sample.getJira().getUrl(), "https://jira.atlassian.com", "Jira URL is invalid!");
        Assert.assertEquals(sample.getJira().isAnonymous(), true, "Jira anonymous access flag is invalid!");
        Assert.assertNotNull(sample.getJira().getProxy(), "Proxy class was not instantiated!");
        Assert.assertEquals(sample.getJira().getProxy().getHost(), "127.0.0.1", "Proxy host is invalid!");
        Assert.assertEquals(sample.getJira().getProxy().getPort(), "8080", "Proxy port is invalid!");
        Assert.assertNotNull(sample.getJira().getProjects(), "Jira projects class was not instantiated!");
        Assert.assertEquals(sample.getJira().getProjects().size(), 2, "Size of projects list is invalid!");
        Assert.assertEquals(sample.getJira().getProjects().get(0), "PRJ1", "First links in list is invalid!");
        Assert.assertEquals(sample.getJira().getProjects().get(1), "PRJ2", "Second links in list is invalid!");
        Assert.assertNotNull(sample.getReport(), "Report class was not instantiated!");
        Assert.assertEquals(sample.getReport().getMarker(), "# ", "Marker of regular sheet is invalid!");
        Assert.assertNotNull(sample.getReport().getUpdateDate(), "UpdateDate class was not instantiated!");
        Assert.assertEquals(sample.getReport().getUpdateDate().getTimePattern(), "dd MMM yyyy HH:mm ZZ", "Template for update date is invalid!");
        Assert.assertEquals(sample.getReport().getUpdateDate().getRow(), 3, "Row for update date is invalid!");
        Assert.assertEquals(sample.getReport().getUpdateDate().getCol(), 5, "Column for update date is invalid!");
        Assert.assertNotNull(sample.getReport().getToHide(), "ToHide class was not instantiated!");
        Assert.assertEquals(sample.getReport().getToHide().getIssuePrefixRow(), Integer.valueOf(2), "Row for issue prefix is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getIssuePrefixCol(), Integer.valueOf(0), "Column for issue prefix is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getAffectedVersionRow(), Integer.valueOf(2), "Row for affected version is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getAffectedVersionCol(), Integer.valueOf(8), "Column for affected version is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getComponentsRow(), Integer.valueOf(2), "Row for components is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getComponentsCol(), Integer.valueOf(9), "Column for components is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getLabelsRow(), Integer.valueOf(2), "Row for labels is invalid!");
        Assert.assertEquals(sample.getReport().getToHide().getLabelsCol(), Integer.valueOf(10), "Column for labels is invalid!");
        Assert.assertNotNull(sample.getReport().getJqlQuery(), "JQL request class was not instantiated!");
        Assert.assertEquals(sample.getReport().getJqlQuery().getCol(), 1, "Column for JQL request is invalid!");
        Assert.assertEquals(sample.getReport().getJqlQuery().getRow(), 2, "Row for JQL request is invalid!");
        Assert.assertNotNull(sample.getReport().getRootIssue(), "Root Issue class was not instantiated!");
        Assert.assertEquals(sample.getReport().getRootIssue().isUnfoldSubtasks(), true, "UnfoldSubtasks flag is invalid!");
        Assert.assertEquals(sample.getReport().getRootIssue().getIssueKeyCol(), 3, "Column for root issue is invalid!");
        Assert.assertEquals(sample.getReport().getRootIssue().getIssueKeyRow(), 2, "Row for root issue is invalid!");
        Assert.assertNotNull(sample.getReport().getRootIssue().getLinks(), "Issue links class was not instantiated!");
        Assert.assertEquals(sample.getReport().getRootIssue().getLinks().get(0), "relates to", "Column for link types is invalid!");
        Assert.assertEquals(sample.getReport().getRootIssue().getLinks().get(1), "is duplicated by", "Row for link types is invalid!");
        Assert.assertEquals(sample.getReport().getStartProcessingRow(), 5, "Start processing issueKeyRow is invalid!");
        Assert.assertNotNull(sample.getReport().getIssueColumns(), "Issue columns class was not instantiated!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getSummary(), Integer.valueOf(0), "Column for issue summary is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getKey(), Integer.valueOf(1), "Column for issue key is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getRelation(), Integer.valueOf(2), "Column for issue relation is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getParentKey(), Integer.valueOf(3), "Column for issue parent key is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getEstimation(), Integer.valueOf(4), "Column for issue estimation is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getSpentTime(), Integer.valueOf(5), "Column for issue spent time is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getRemainingTime(), Integer.valueOf(6), "Column for issue remaining time is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getStatus(), Integer.valueOf(7), "Column for issue status is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getAfVersion(), Integer.valueOf(8), "Column for issue affected version(s) is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getComponents(), Integer.valueOf(9), "Column for issue component(s) is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getLabels(), Integer.valueOf(10), "Column for issue label(s) is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getAssignee(), Integer.valueOf(11), "Column for issue assignee is invalid!");
        Assert.assertEquals(sample.getReport().getTimeTrackingFormat(), "0.00", "Time tracking format is invalid!");
        Assert.assertNotNull(sample.getReport().getProcessingFlags(), "Processing flags class was not instantiated!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isIssueSummaryUpdate(), true, "IssueSummaryUpdate flag is invalid!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isRecalculateFormulas(), false, "RecalculateFormulas flag is invalid!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isAutosizeColumns(), false, "AutosizeColumns flag is invalid!");
        Assert.assertNotNull(sample.getReport().getReportName(), "Report name class was not instantiated!");
        Assert.assertEquals(sample.getReport().getReportName().getPrefix(), "prefix_", "Report prefix is invalid!");
        Assert.assertEquals(sample.getReport().getReportName().getTimePattern(), "yyyy.MM.dd_HH_mm_ss", "Report time pattern is invalid!");
        Assert.assertEquals(sample.getReport().getReportName().getSuffix(), "_suffix", "Report suffix is invalid!");
    }

    @Test(dependsOnMethods = "testAllProperties", groups = "core")
    public void testMandatoryProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\only_mandatory_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        ConfigType sample = (ConfigType) unmarshaller.unmarshal(new File(configFile));
        Assert.assertNotNull(sample, "Config class was not instantiated!");
        Assert.assertNotNull(sample.getJira(), "Jira class was not instantiated!");
        Assert.assertEquals(sample.getJira().getUrl(), "https://jira.atlassian.com", "Jira URL is invalid!");
        Assert.assertEquals(sample.getJira().isAnonymous(), false, "Jira anonymous access flag is invalid!");
        Assert.assertNull(sample.getJira().getProxy(), "Proxy class was instantiated!");
        Assert.assertNull(sample.getJira().getProjects(), "Jira projects class was instantiated!");
        Assert.assertNotNull(sample.getReport(), "Report class was not instantiated!");
        Assert.assertEquals(sample.getReport().getMarker(), "# ", "Marker of regular sheet is invalid!");
        Assert.assertNull(sample.getReport().getToHide(), "ToHide class was instantiated!");
        Assert.assertNull(sample.getReport().getJqlQuery(), "JQL request class was instantiated!");
        Assert.assertNull(sample.getReport().getRootIssue(), "Root Issue class was instantiated!");
        Assert.assertNotNull(sample.getReport().getIssueColumns(), "Issue columns class was not instantiated!");
        Assert.assertNull(sample.getReport().getIssueColumns().getSummary(), "Column for issue summary is invalid!");
        Assert.assertEquals(sample.getReport().getIssueColumns().getKey(), Integer.valueOf(1), "Column for issue key is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getRelation(), "Column for issue relation is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getParentKey(), "Column for issue parent key is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getEstimation(), "Column for issue estimation is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getSpentTime(), "Column for issue spent time is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getRemainingTime(), "Column for issue remaining time is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getStatus(), "Column for issue status is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getAfVersion(), "Column for issue affected version(s) is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getComponents(), "Column for issue component(s) is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getLabels(), "Column for issue label(s) is invalid!");
        Assert.assertNull(sample.getReport().getIssueColumns().getAssignee(), "Column for issue assignee is invalid!");
        Assert.assertNull(sample.getReport().getTimeTrackingFormat(), "Time tracking format is invalid!");
        Assert.assertNull(sample.getReport().getProcessingFlags(), "Processing flags class was instantiated!");
        Assert.assertNull(sample.getReport().getReportName(), "Report name class was not instantiated!");
    }

    @Test(dependsOnGroups = "core")
    public void testMixedBooleanProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\mixed_boolean_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        ConfigType sample = (ConfigType) unmarshaller.unmarshal(new File(configFile));
        Assert.assertEquals(sample.getJira().isAnonymous(), true, "Jira anonymous access flag is invalid!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isIssueSummaryUpdate(), true, "IssueSummaryUpdate flag is invalid!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isRecalculateFormulas(), false, "RecalculateFormulas flag is invalid!");
        Assert.assertEquals(sample.getReport().getProcessingFlags().isAutosizeColumns(), false, "AutosizeColumns flag is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testEmptyCellsProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\empty_cells_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        ConfigType sample = (ConfigType) unmarshaller.unmarshal(new File(configFile));
        Assert.assertNotNull(sample.getReport().getToHide(), "ToHide class was not instantiated!");
        Assert.assertNull(sample.getReport().getToHide().getIssuePrefixRow(), "Row for issue prefix is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getIssuePrefixCol(), "Column for issue prefix is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getAffectedVersionRow(), "Row for affected version is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getAffectedVersionCol(), "Column for affected version is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getComponentsRow(), "Row for components is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getComponentsCol(), "Column for components is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getLabelsRow(), "Row for labels is invalid!");
        Assert.assertNull(sample.getReport().getToHide().getLabelsCol(), "Column for labels is invalid!");
    }

    @Test(dependsOnGroups = "core")
    public void testConfigFileValidation() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_mandatory_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        try {
            unmarshaller.unmarshal(new File(configFile));
        } catch (UnmarshalException ex) {
            Throwable linked = ex.getLinkedException();
            Assert.assertEquals(linked.getClass(), SAXParseException.class, "Wrong exception class:");
            Assert.assertEquals(linked.getMessage(), "cvc-complex-type.2.4.b: The content of element 'config' is not complete. One of '{jira}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + UnmarshalException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testMissingProjectProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_project_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        try {
            unmarshaller.unmarshal(new File(configFile));
            Assert.fail("Exception " + UnmarshalException.class + " expected, but not thrown!");
        } catch (UnmarshalException ex) {
            Throwable linked = ex.getLinkedException();
            Assert.assertEquals(linked.getClass(), SAXParseException.class, "Wrong exception class:");
            Assert.assertEquals(linked.getMessage(), "cvc-complex-type.2.4.b: The content of element 'projects' is not complete. One of '{entry}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + UnmarshalException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }

    @Test(dependsOnGroups = "core")
    public void testMissingLinksProperties() throws Throwable {
        String configFile = "src\\test\\resources\\configs\\missing_links_properties.xml";
        String schemaFile = "progress_reporter.xsd";

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = schemaFactory.newSchema(new File(schemaFile));

        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setSchema(schema);
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        try {
            unmarshaller.unmarshal(new File(configFile));
            Assert.fail("Exception " + UnmarshalException.class + " expected, but not thrown!");
        } catch (UnmarshalException ex) {
            Throwable linked = ex.getLinkedException();
            Assert.assertEquals(linked.getClass(), SAXParseException.class, "Wrong exception class:");
            Assert.assertEquals(linked.getMessage(), "cvc-complex-type.2.4.b: The content of element 'links' is not complete. One of '{entry}' is expected.", "Wrong error message:");
        } catch (Throwable e) {
            Assert.fail("Wrong exception: expected [" + UnmarshalException.class + "], but actual is [" + e.getClass() + "] with message " + e.getMessage());
        }
    }
}