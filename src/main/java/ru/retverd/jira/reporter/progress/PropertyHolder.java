package ru.retverd.jira.reporter.progress;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

public class PropertyHolder {
    static private final String JIRA_URL_NAME = "jira.url";
    static private final String REGULAR_TAB_MARKER = "excel.tab.regular.marker";
    static private final String FORBIDDEN_TAB_MARKER = "excel.tab.forbidden.marker";
    static private final String UPDATE_ROW = "excel.update.row";
    static private final String UPDATE_COLUMN = "excel.update.column";
    static private final String START_PROCESSING_ROW = "excel.start_processing.row";
    static private final String ISSUE_DESCRIPTION_COLUMN = "excel.issue.description.column";
    static private final String ISSUE_KEY_COLUMN = "excel.issue.key.column";
    static private final String ISSUE_ESTIMATION_COLUMN = "excel.issue.estimation.column";
    static private final String ISSUE_SPENT_COLUMN = "excel.issue.spent.column";
    static private final String ISSUE_REMAINING_COLUMN = "excel.issue.remaining.column";
    static private final String ISSUE_STATUS_COLUMN = "excel.issue.status.column";
    static private final String ISSUE_KEY_SKIP_VALUE = "excel.issue.key.skip.value";
    static private final String ISSUE_DESCRIPTION_FILL = "excel.issue.description.fill";
    static private final String RECALCULATE_FORMULAS = "excel.recalculate.formulas";
    // First value is default
    static private final String[] descriptionFillValues = { "n", "y" };
    static private final String[] recalculateFormulasValues = { "n", "y" };

    // Where to connect
    private String jiraURL;
    // Tabs that starts with this string will be processed
    private String regularTabMarker;
    // Tab that starts with this string contains issues where time should not be
    // estimated or logged
    private String forbiddenTabMarker;
    // Coordinates of cell with date of update
    private int updateRow;
    private int updateColumn;
    // Row number for start processing
    private int startProcessingRow;
    // Column number for corresponding values
    private int issueDescriptionColumn;
    private int issueKeyColumn;
    private int issueEstimationColumn;
    private int issueSpentColumn;
    private int issueRemainingColumn;
    private int issueStatusColumn;
    // Line with this issue key will be skipped
    private String issueKeySkipValue;
    // If description empty put real issue description (y/n)
    private boolean issueDescriptionFill;
    // If description empty put real issue description (y/n)
    private boolean recalculateFormulas;

    public PropertyHolder(String propertiesFile) throws IOException {
	Properties properties = new Properties();

	// Load properties from given file
	InputStream pathToPropertiesFile = new FileInputStream(propertiesFile);
	properties.load(pathToPropertiesFile);
	pathToPropertiesFile.close();

	// I need hint how to do it better :)
	jiraURL = setStringProperty(properties, JIRA_URL_NAME, propertiesFile);

	regularTabMarker = setStringProperty(properties, REGULAR_TAB_MARKER, propertiesFile);

	forbiddenTabMarker = setStringProperty(properties, FORBIDDEN_TAB_MARKER, propertiesFile);

	updateRow = setIntProperty(properties, UPDATE_ROW, propertiesFile);

	updateColumn = setIntProperty(properties, UPDATE_COLUMN, propertiesFile);

	issueDescriptionColumn = setIntProperty(properties, ISSUE_DESCRIPTION_COLUMN, propertiesFile);

	issueKeyColumn = setIntProperty(properties, ISSUE_KEY_COLUMN, propertiesFile);

	issueEstimationColumn = setIntProperty(properties, ISSUE_ESTIMATION_COLUMN, propertiesFile);

	issueSpentColumn = setIntProperty(properties, ISSUE_SPENT_COLUMN, propertiesFile);

	issueRemainingColumn = setIntProperty(properties, ISSUE_REMAINING_COLUMN, propertiesFile);

	issueStatusColumn = setIntProperty(properties, ISSUE_STATUS_COLUMN, propertiesFile);

	startProcessingRow = setIntProperty(properties, START_PROCESSING_ROW, propertiesFile);

	issueKeySkipValue = setStringProperty(properties, ISSUE_KEY_SKIP_VALUE, propertiesFile);

	issueDescriptionFill = stringToBoolean(setStringPropertyWithDefaults(properties, ISSUE_DESCRIPTION_FILL, descriptionFillValues, propertiesFile));

	recalculateFormulas = stringToBoolean(setStringPropertyWithDefaults(properties, RECALCULATE_FORMULAS, recalculateFormulasValues, propertiesFile));
    }

    private String setStringProperty(Properties props, String propName, String fileName) throws IOException {
	if (props.getProperty(propName) == null) {
	    throw new IOException("Missing " + propName + " property in file " + fileName);
	}
	return props.getProperty(propName);
    }

    private String setStringPropertyWithDefaults(Properties props, String propName, String[] defValues, String fileName) throws IOException {
	String propertyValue = props.getProperty(propName, defValues[0]);

	if (propertyValue == null) {
	    throw new IOException("Missing " + propName + " property in file " + fileName);
	}
	if (!Arrays.asList(defValues).contains(propertyValue)) {
	    String expectedValues = defValues[0];
	    for (int i = 1; i < defValues.length; i++) {
		expectedValues = expectedValues + ", " + defValues[i];
	    }
	    throw new IOException(propName + " property in file " + fileName + " contains inacceptable value: " + propertyValue + ". Only values "
		    + expectedValues + " are acceptable.");
	}
	return propertyValue;
    }

    private int setIntProperty(Properties props, String propName, String fileName) throws IOException {
	if (props.getProperty(propName) == null) {
	    throw new IOException("Missing " + propName + " property in file " + fileName);
	}
	try {
	    return Integer.parseInt(props.getProperty(propName));
	} catch (NumberFormatException e) {
	    throw new IOException("Integer value is expected in property " + propName + " in file " + fileName);
	}
    }

    private boolean stringToBoolean(String value) {
	if (value.equals("y")) {
	    return true;
	} else {
	    return false;
	}
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

    public int getIssueDescriptionColumn() {
	return issueDescriptionColumn - 1;
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

    public int getIssueStatusColumn() {
	return issueStatusColumn - 1;
    }

    public String getIssueKeySkipValue() {
	return issueKeySkipValue;
    }

    public boolean getIssueDescriptionFill() {
	return issueDescriptionFill;
    }

    public boolean getRecalculateFormulas() {
	return recalculateFormulas;
    }
}
