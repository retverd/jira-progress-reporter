package ru.retverd.jira.reporter.progress;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class PropertyHolder {
    static private final String JIRA_URL_NAME = "jira.url";
    static private final String JIRA_PROXY = "jira.proxy";
    static private final String JIRA_ANONYMOUS_ACCESS = "jira.access.anonymously";
    static private final String REGULAR_TAB_MARKER = "excel.tab.regular.marker";
    static private final String FORBIDDEN_TAB_MARKER = "excel.tab.forbidden.marker";
    static private final String UPDATE_ROW = "excel.update.row";
    static private final String UPDATE_COLUMN = "excel.update.column";
    static private final String START_PROCESSING_ROW = "excel.start_processing.row";
    static private final String ISSUE_SUMMARY_COLUMN = "excel.issue.summary.column";
    static private final String ISSUE_KEY_COLUMN = "excel.issue.key.column";
    static private final String ISSUE_ESTIMATION_COLUMN = "excel.issue.estimation.column";
    static private final String ISSUE_SPENT_COLUMN = "excel.issue.spent.column";
    static private final String ISSUE_REMAINING_COLUMN = "excel.issue.remaining.column";
    static private final String ISSUE_STATUS_COLUMN = "excel.issue.status.column";
    static private final String ISSUE_COMPONENTS_COLUMN = "excel.issue.components.column";
    static private final String ISSUE_ASSIGNEE_COLUMN = "excel.issue.assignee.column";
    static private final String ISSUE_KEY_PREFIX = "excel.issue.key.prefix";
    static private final String ISSUE_SUMMARY_FILL = "excel.issue.summary.fill";
    static private final String RECALCULATE_FORMULAS = "excel.recalculate.formulas";
    static private final String REPORT_FILENAME_PATTERN = "report.filename.pattern";
    static private final String UNFOLD_MARKER = "excel.unfold.marker";
    static private final String UNFOLD_MARKER_COLUMN = "excel.unfold.marker.column";
    static private final String ISSUE_RELATION_COLUMN = "excel.issue.relation.column";
    static private final String ISSUE_PARENT_KEY_COLUMN = "excel.issue.parent.key.column";
    // Divider for properties lists
    static private final String PROPERTIES_DIVIDER = ",";
    // Divider between proxy host and port
    static private final String PROXY_DIVIDER = ":";
    // First value is default
    static private final String[] yesNoValues = {"n", "y"};

    // Where to connect
    private String jiraURL;
    // Should authentication be used
    private boolean jiraAnonymousAccess;
    // Tabs that starts with this string will be processed
    private String regularTabMarker;
    // Tab that starts with this string contains issues where time should not be estimated or logged
    private String forbiddenTabMarker;
    // Pattern for new report filename, based on joda DateTimeFormat syntax
    private String reportFilenamePattern;
    // Coordinates of cell with date of update
    private int updateRow;
    private int updateColumn;
    // Row number for start processing
    private int startProcessingRow;
    // Column numbers for corresponding values
    private int issueSummaryColumn;
    private int issueKeyColumn;
    private int issueEstimationColumn;
    private int issueSpentColumn;
    private int issueRemainingColumn;
    private int issueStatusColumn;
    private int issueComponentsColumn;
    private int issueAssigneeColumn;
    // List of prefixes (with hyphen) for issues in project(s)
    private String[] issueKeyPrefixList;
    // Overwrite issue summary
    private boolean issueSummaryFill;
    // Recalculate all formulas in report
    private boolean recalculateFormulas;
    // Value to indicate that specific issue should be tried to be unfolded
    private String unfoldMarker;
    // Column numbers for corresponding values
    private int unfoldMarkerColumn;
    private int issueRelationColumn;
    private int issueParentKeyColumn;

    public PropertyHolder(String propertiesFile) throws IOException {
        Properties properties = new Properties();

        // Load properties from given file
        InputStream pathToPropertiesFile = new FileInputStream(propertiesFile);
        properties.load(pathToPropertiesFile);
        pathToPropertiesFile.close();

        // I need a hint how to init all variables better :)
        jiraURL = getStringProperty(properties, JIRA_URL_NAME, propertiesFile);

        regularTabMarker = getStringProperty(properties, REGULAR_TAB_MARKER, propertiesFile);

        forbiddenTabMarker = getStringProperty(properties, FORBIDDEN_TAB_MARKER, propertiesFile);

        updateRow = getIntProperty(properties, UPDATE_ROW, propertiesFile);

        updateColumn = getIntProperty(properties, UPDATE_COLUMN, propertiesFile);

        issueSummaryColumn = getIntProperty(properties, ISSUE_SUMMARY_COLUMN, propertiesFile);

        issueKeyColumn = getIntProperty(properties, ISSUE_KEY_COLUMN, propertiesFile);

        issueEstimationColumn = getIntProperty(properties, ISSUE_ESTIMATION_COLUMN, propertiesFile);

        issueSpentColumn = getIntProperty(properties, ISSUE_SPENT_COLUMN, propertiesFile);

        issueRemainingColumn = getIntProperty(properties, ISSUE_REMAINING_COLUMN, propertiesFile);

        issueStatusColumn = getIntProperty(properties, ISSUE_STATUS_COLUMN, propertiesFile);

        issueComponentsColumn = getIntProperty(properties, ISSUE_COMPONENTS_COLUMN, propertiesFile);

        issueAssigneeColumn = getIntProperty(properties, ISSUE_ASSIGNEE_COLUMN, propertiesFile);

        startProcessingRow = getIntProperty(properties, START_PROCESSING_ROW, propertiesFile);

        issueKeyPrefixList = getStringArrayProperty(properties, ISSUE_KEY_PREFIX, propertiesFile);

        issueSummaryFill = isActionRequested(getStringPropertyWithDefaults(properties, ISSUE_SUMMARY_FILL, yesNoValues, propertiesFile));

        recalculateFormulas = isActionRequested(getStringPropertyWithDefaults(properties, RECALCULATE_FORMULAS, yesNoValues, propertiesFile));

        jiraAnonymousAccess = isActionRequested(getStringPropertyWithDefaults(properties, JIRA_ANONYMOUS_ACCESS, yesNoValues, propertiesFile));

        // Optional, no need to check if value is missing
        reportFilenamePattern = properties.getProperty(REPORT_FILENAME_PATTERN);

        // Optional, no need to check if value is missing
        unfoldMarker = properties.getProperty(UNFOLD_MARKER);
        if (unfoldMarker != null) {
            unfoldMarkerColumn = getIntProperty(properties, UNFOLD_MARKER_COLUMN, propertiesFile);
            issueRelationColumn = getIntProperty(properties, ISSUE_RELATION_COLUMN, propertiesFile);
            issueParentKeyColumn = getIntProperty(properties, ISSUE_PARENT_KEY_COLUMN, propertiesFile);
        }

        // Optional, no need to check if value is missing
        String proxy = properties.getProperty(JIRA_PROXY);
        if (proxy != null) {
            String[] proxyParameters = proxy.split(PROXY_DIVIDER);
            System.setProperty("https.proxyHost", proxyParameters[0]);
            System.setProperty("https.proxyPort", proxyParameters[1]);
        }
    }

    private String getStringProperty(Properties props, String propName, String fileName) throws IOException {
        if (props.getProperty(propName) == null) {
            throw new IOException("Missing " + propName + " property in file " + fileName);
        }
        return props.getProperty(propName);
    }

    private String[] getStringArrayProperty(Properties props, String propName, String fileName) throws IOException {
        if (props.getProperty(propName) == null) {
            throw new IOException("Missing " + propName + " property in file " + fileName);
        }
        return props.getProperty(propName).split(PROPERTIES_DIVIDER);
    }

    private String getStringPropertyWithDefaults(Properties props, String propName, String[] defValues, String fileName) throws IOException {
        String propertyValue = props.getProperty(propName, defValues[0]);

        if (propertyValue == null) {
            throw new IOException("Missing " + propName + " property in file " + fileName);
        }
        if (!Arrays.asList(defValues).contains(propertyValue)) {
            String expectedValues = defValues[0];
            for (int i = 1; i < defValues.length; i++) {
                expectedValues = expectedValues + ", " + defValues[i];
            }
            throw new IOException(propName + " property in file " + fileName + " contains unacceptable value: " + propertyValue + ". Only values "
                    + expectedValues + " are acceptable.");
        }
        return propertyValue;
    }

    private int getIntProperty(Properties props, String propName, String fileName) throws IOException {
        if (props.getProperty(propName) == null) {
            throw new IOException("Missing " + propName + " property in file " + fileName);
        }
        try {
            return Integer.parseInt(props.getProperty(propName));
        } catch (NumberFormatException e) {
            throw new IOException("Integer value is expected in property " + propName + " in file " + fileName);
        }
    }

    private boolean isActionRequested(String value) {
        return value.equals("y");
    }

    public String getJiraURL() {
        return jiraURL;
    }

    public String getRegularTabMarker() {
        return regularTabMarker;
    }

    public String getForbiddenTabMarker() {
        return forbiddenTabMarker;
    }

    public int getUpdateRow() {
        return updateRow - 1;
    }

    public int getUpdateColumn() {
        return updateColumn - 1;
    }

    public int getStartProcessingRow() {
        return startProcessingRow - 1;
    }

    public int getIssueSummaryColumn() {
        return issueSummaryColumn - 1;
    }

    public int getIssueKeyColumn() {
        return issueKeyColumn - 1;
    }

    public int getIssueEstimationColumn() {
        return issueEstimationColumn - 1;
    }

    public int getIssueSpentColumn() {
        return issueSpentColumn - 1;
    }

    public int getIssueRemainingColumn() {
        return issueRemainingColumn - 1;
    }

    public int getIssueAssigneeColumn() {
        return issueAssigneeColumn - 1;
    }

    public int getIssueComponentsColumn() {
        return issueComponentsColumn - 1;
    }

    public int getIssueStatusColumn() {
        return issueStatusColumn - 1;
    }

    public String[] getIssueKeyPrefixList() {
        return issueKeyPrefixList;
    }

    public boolean isIssueSummaryFill() {
        return issueSummaryFill;
    }

    public boolean isRecalculateFormulas() {
        return recalculateFormulas;
    }

    public boolean isJiraAnonymousAccess() {
        return jiraAnonymousAccess;
    }

    public String getReportFilenamePattern() {
        return reportFilenamePattern;
    }

    public String getUnfoldMarker() {
        return unfoldMarker;
    }

    public int getUnfoldMarkerColumn() {
        return unfoldMarkerColumn - 1;
    }

    public int getIssueRelationColumn() {
        return issueRelationColumn - 1;
    }

    public int getIssueParentKeyColumn() {
        return issueParentKeyColumn - 1;
    }

    public void setIssueSummaryFill(boolean newValue) {
        issueSummaryFill = newValue;
    }
}
