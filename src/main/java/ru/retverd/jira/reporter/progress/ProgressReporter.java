package ru.retverd.jira.reporter.progress;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Optional;
import org.apache.poi.POIXMLException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

class ProgressReporter {
    // Divider between project prefix and issue number
    static private final String ISSUE_DIVIDER = "-";
    // Underline hyperlinks
    static private final byte LINK_UNDERLINE_STYLE = Font.U_SINGLE;
    // Color for hyperlinks
    static private final short LINK_FONT_COLOR = IndexedColors.BLUE.getIndex();
    // Status code for wrong credentials
    static private final Integer AUTH_FAIL_STATUS = 401;
    // Name of Link type for "part of" / "" relation
    static private final String PART_OF_FLAG = "Composition";
    // Joiner for Jira url from properties and issue key
    static private final String LINK_MISSING_CHAIN = "/i#browse/";
    // Values for indication of relation between issues in report
    static private final String PART_OF_VALUE = "part of";
    static private final String SUBTASK_OF_VALUE = "subtask of";

    private PropertyHolder properties;
    private XSSFWorkbook workbook;
    private Font hlinkFont;
    private JiraRestClient jiraClient;

    public ProgressReporter(String fileWithProperties, String fileWithReportTemplate) throws IOException {
        // Load and check file with properties
        System.out.format("Loading and parsing properties from " + fileWithProperties + "...");
        properties = new PropertyHolder(fileWithProperties);
        System.out.format("done!%n");

        // Load Excel file with report template
        System.out.format("Reading report file " + fileWithReportTemplate + "...");
        // OPCPackage is not used due to problems with saving results: org.apache.poi.openxml4j.exceptions.OpenXML4JException: The part /docProps/app.xml fail
        // to be saved in the stream with marshaller org.apache.poi.openxml4j.opc.internal.marshallers.DefaultMarshaller@1c67c1a6
        FileInputStream fis = new FileInputStream(fileWithReportTemplate);
        try {
            workbook = new XSSFWorkbook(fis);
        } catch (POIXMLException e) {
            throw new IOException("Report file " + fileWithReportTemplate + " has incorrect format.");
        } finally {
            fis.close();
        }
        // Create font for cells with hyperlinks
        hlinkFont = workbook.createFont();
        hlinkFont.setUnderline(LINK_UNDERLINE_STYLE);
        hlinkFont.setColor(LINK_FONT_COLOR);
        System.out.format("done!%n");
    }

    private void checkConnectionToJira(String errorMessage) throws IOException {
        try {
            ServerInfo si = jiraClient.getMetadataClient().getServerInfo().claim();
            System.out.println("Just connected to Jira instance v." + si.getVersion());
        } catch (RestClientException e) {
            disconnectFromJIRA();
            Optional<Integer> statusCode = e.getStatusCode();
            if (statusCode.isPresent()) {
                // Handle exception for incorrect credentials
                if (statusCode.get().equals(AUTH_FAIL_STATUS)) {
                    System.out.println(errorMessage);
                    return;
                }
            }
            throw e;
        }
    }

    public void connectToJiraWithCredentials(String login,String pass) throws IOException, URISyntaxException {
        // TODO handle incorrect credentials!
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        jiraClient = factory.createWithBasicHttpAuthentication(new URI(properties.getJiraURL()), login, pass);
        checkConnectionToJira("Authentication error! Please check your credentials!");
    }

    public void connectToJiraAnonymously() throws IOException, URISyntaxException {
        // TODO handle missing access!
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        jiraClient = factory.createWithAnonymousAccess(new URI(properties.getJiraURL()));
        checkConnectionToJira("Anonymous access is prohibited!");
    }

    public void disconnectFromJIRA() throws IOException {
        jiraClient.close();
    }

    public void updateReport() {
        // Today
        DateTime today = new DateTime();
        DateTimeFormatter reportHeader = DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ");

        // iterate through sheets
        for (XSSFSheet sheet : workbook) {
            String sheetName = sheet.getSheetName();
            if (sheetName.contains(properties.getRegularTabMarker())) {
                System.out.println("Processing sheet " + sheetName.replace(properties.getRegularTabMarker(), "") + "...");
                XSSFRow currentRow = sheet.getRow(properties.getUpdateRow());
                XSSFCell currentCell = currentRow.getCell(properties.getUpdateColumn(), Row.CREATE_NULL_AS_BLANK);
                currentCell.setCellValue(reportHeader.withLocale(Locale.ENGLISH).print(today));
                if (isUnfoldRequired(sheet)) {
                    // Remove all rows below
                    for (int i = properties.getStartProcessingRow() + 1; i <= sheet.getLastRowNum(); ) {
                        currentRow = sheet.getRow(i);
                        // Skip missing rows
                        if (currentRow != null) {
                            sheet.removeRow(currentRow);
                        } else {
                            i++;
                        }
                    }
                    // Publish details for root issue
                    currentRow = sheet.getRow(properties.getStartProcessingRow());
                    currentCell = currentRow.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK);
                    publishIssueDetails(currentRow, currentCell.getStringCellValue());

                    // Walk through linked issues and subtasks and put add rows with details
                    boolean flag = properties.isIssueSummaryFill();
                    properties.setIssueSummaryFill(true);
                    publishDependentIssues(sheet, currentCell.getStringCellValue());
                    properties.setIssueSummaryFill(flag);
                    // Remove unfold marker
                    currentRow.getCell(properties.getUnfoldMarkerColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue("");
                } else {
                    int rowNumber = properties.getStartProcessingRow();
                    currentRow = sheet.getRow(rowNumber++);
                    while (currentRow != null) {
                        currentCell = currentRow.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK);
                        if (currentCell != null) {
                            if (isIssueInScope(currentCell.getStringCellValue())) {
                                publishIssueDetails(currentRow, currentCell.getStringCellValue());
                            }
                        }
                        currentRow = sheet.getRow(rowNumber++);
                    }
                }

                adjustCellsWidth(sheet);
                System.out.println("...done!");
            } else {
                System.out.println("Sheet \"" + sheetName + "\" is skipped.");
            }
        }

        // Refresh all formulas if required
        // Leads to exceptions on some Excel files with error message: Unexpected ptg class (org.apache.poi.ss.formula.ptg.ArrayPtg)
        // See https://github.com/retverd/jira-progress-reporter/issues/1 (Problems with evaluateAllFormulaCells)
        if (properties.isRecalculateFormulas()) {
            System.out.format("Recalculating formulas...");
            XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            System.out.format("done!%n");
        }
    }

    public void saveReport(String fileWithReport) throws IOException {
        String notification = "Rewriting";

        if (properties.getReportFilenamePattern() != null) {
            DateTimeFormatter dateFormForReport = DateTimeFormat.forPattern(properties.getReportFilenamePattern());
            fileWithReport = dateFormForReport.print(new DateTime()) + ".xlsx";
            notification = "Saving report to";
        }

        System.out.format(notification + " file " + fileWithReport + "...");
        FileOutputStream fos = new FileOutputStream(new File(fileWithReport));
        workbook.write(fos);
        fos.close();
        System.out.format("done!%n");
    }

    void publishIssueDetails(XSSFRow row, String issueKey) {
        XSSFCell cell = row.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK);

        // Add hyperlink to current issue if required
        if (cell.getHyperlink() == null) {
            addHyperLink(cell);
        }

        // Add hyperlink to parent issue if required
        cell = row.getCell(properties.getIssueParentKeyColumn(), Row.CREATE_NULL_AS_BLANK);
        if ((cell.getHyperlink() == null) && (cell.getStringCellValue() != null)) {
            addHyperLink(cell);
        }

        System.out.println("Retrieving issue " + issueKey);
        Issue issue = jiraClient.getIssueClient().getIssue(issueKey).claim();
        if (properties.isIssueSummaryFill()) {
            row.getCell(properties.getIssueSummaryColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getSummary());
        }

        TimeTracking time = issue.getTimeTracking();
        if (time != null) {
            row.getCell(properties.getIssueEstimationColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getOriginalEstimateMinutes()));
            row.getCell(properties.getIssueSpentColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getTimeSpentMinutes()));
            row.getCell(properties.getIssueRemainingColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getRemainingEstimateMinutes()));
        } else {
            throw new IllegalArgumentException("Time tracking is not supported for current Jira instance (" + properties.getJiraURL() + ").");
        }
        row.getCell(properties.getIssueStatusColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getStatus().getName());


        Iterator<BasicComponent> iComponents = issue.getComponents().iterator();
        String components = "";
        while(iComponents.hasNext()) {
            if (components.isEmpty()) {
                components = iComponents.next().getName();
            } else {
                components = components + "," + iComponents.next().getName();
            }
        }
        row.getCell(properties.getIssueComponentsColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(components);
        row.getCell(properties.getIssueAssigneeColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getAssignee() == null ? "" : issue.getAssignee().getDisplayName());
    }

    boolean isIssueInScope(String issueKey) {
        String[] issueKeyParts = issueKey.split(ISSUE_DIVIDER);
        return Arrays.asList(properties.getIssueKeyPrefixList()).contains(issueKeyParts[0]);
    }

    void publishDependentIssues(XSSFSheet sheet, String issueKey) {
        properties.setIssueSummaryFill(true);

        Issue rootIssue = jiraClient.getIssueClient().getIssue(issueKey).claim();

        Iterable<IssueLink> issueLinks = rootIssue.getIssueLinks();
        if (issueLinks != null) {
            // Handle referenced issues
            // TODO Testing required
            for (IssueLink issueLink : issueLinks) {
                if (issueLink.getIssueLinkType().getDirection().equals(IssueLinkType.Direction.OUTBOUND) && issueLink.getIssueLinkType().getName().equals(PART_OF_FLAG)) {
                    appendNewIssueRecord(sheet, PART_OF_VALUE, issueLink.getTargetIssueKey(), issueKey);
                    publishDependentIssues(sheet, issueLink.getTargetIssueKey());
                }
            }
        }

        Iterable<Subtask> subTasks = rootIssue.getSubtasks();

        if (subTasks != null) {
            // Handle subtasks
            for (Subtask subtask : subTasks) {
                appendNewIssueRecord(sheet, SUBTASK_OF_VALUE, subtask.getIssueKey(), issueKey);
            }
        }
    }

    void appendNewIssueRecord(XSSFSheet sheet, String relation, String issueKey, String parentKey) {
        XSSFRow row;

        row = sheet.createRow(sheet.getLastRowNum() + 1);
        createCells(row);
        row.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issueKey);
        row.getCell(properties.getIssueRelationColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(relation);
        row.getCell(properties.getIssueParentKeyColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(parentKey);
        publishIssueDetails(row, issueKey);
    }

    void adjustCellsWidth(XSSFSheet sheet) {
        sheet.autoSizeColumn(properties.getIssueSummaryColumn());
        sheet.autoSizeColumn(properties.getIssueKeyColumn());
        sheet.autoSizeColumn(properties.getIssueRelationColumn());
        sheet.autoSizeColumn(properties.getIssueParentKeyColumn());
        sheet.autoSizeColumn(properties.getIssueEstimationColumn());
        sheet.autoSizeColumn(properties.getIssueSpentColumn());
        sheet.autoSizeColumn(properties.getIssueRemainingColumn());
        sheet.autoSizeColumn(properties.getIssueStatusColumn());
        sheet.autoSizeColumn(properties.getIssueComponentsColumn());
        sheet.autoSizeColumn(properties.getIssueAssigneeColumn());
    }

    void createCells(XSSFRow row) {
        row.createCell(properties.getIssueSummaryColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueKeyColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueRelationColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueParentKeyColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueEstimationColumn(), Cell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueSpentColumn(), Cell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueRemainingColumn(), Cell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueStatusColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueComponentsColumn(), Cell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueAssigneeColumn(), Cell.CELL_TYPE_STRING);
    }

    void addHyperLink(XSSFCell cell) {
        // Required for hyperlinks
        CreationHelper createHelper = workbook.getCreationHelper();

        Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
        link.setAddress(properties.getJiraURL() + LINK_MISSING_CHAIN + cell.getStringCellValue());
        cell.setHyperlink(link);
        XSSFCellStyle linkStyle = workbook.createCellStyle();
        linkStyle.cloneStyleFrom(cell.getCellStyle());
        linkStyle.setFont(hlinkFont);
        cell.setCellStyle(linkStyle);
    }

    boolean isUnfoldRequired(XSSFSheet sheet) {
        if (properties.getUnfoldMarker() != null) {
            // Root issue is expected only in first row
            XSSFRow currentRow = sheet.getRow(properties.getStartProcessingRow());
            String issueKey = currentRow.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK).getStringCellValue();
            String unfoldMarker = currentRow.getCell(properties.getUnfoldMarkerColumn(), Row.CREATE_NULL_AS_BLANK).getStringCellValue();
            if (isIssueInScope(issueKey) && properties.getUnfoldMarker().equals(unfoldMarker)) {
                return true;
            }
        }
        return false;
    }

    private static double toHours(Integer value) {
        return (double) (value == null ? 0 : value) / 60;
    }

    public boolean isJiraAnonymouslyAccessible() {
        return properties.isJiraAnonymousAccess();
    }
}