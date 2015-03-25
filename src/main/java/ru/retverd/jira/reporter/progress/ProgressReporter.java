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

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

class ProgressReporter {
    // Divider between project prefix and issue number
    static private final String ISSUE_DIVIDER = "-";
    // Underline hyperlinks
    static private final byte LINK_UNDERLINE_STYLE = Font.U_SINGLE;
    // Color for hyperlinks
    static private final short LINK_FONT_COLOR = IndexedColors.BLUE.getIndex();
    // Status code for wrong credentials
    static private final Integer AUTH_FAIL_STATUS = 401;
    // Joiner for Jira url from properties and issue key
    static private final String LINK_MISSING_CHAIN = "/browse/";
    // String for subtasks
    static private final String SUBTASK_OF_VALUE = "subtask of";

    private PropertyHolder properties;
    private XSSFWorkbook workbook;
    private Font hlinkFont;
    private JiraRestClient jiraClient;
    private HashMap<String, String> linksList;
    private String labels;

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
                    throw new IOException("Authentication error! Please check your credentials!");
                }
            }
            throw e;
        }
        initObjects();
    }

    public void connectToJiraWithCredentials(String login, String pass) throws IOException, URISyntaxException {
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

    private void initObjects() throws IOException {
        linksList = new HashMap<String, String>();
        Iterable<IssuelinksType> issueLinkTypes = jiraClient.getMetadataClient().getIssueLinkTypes().claim();
        String[] links = properties.getUnfoldLinksList();
        if (links != null) {
            for (String link : links) {
                for (IssuelinksType issueLinkType : issueLinkTypes) {
                    if (issueLinkType.getInward().equals(link)) {
                        linksList.put(link, issueLinkType.getOutward());
                        break;
                    } else if (issueLinkType.getOutward().equals(link)) {
                        linksList.put(link, issueLinkType.getInward());
                        break;
                    }
                }
                if (!linksList.containsKey(link)) {
                    throw new IOException("Link '" + link + "' is missing on server!");
                }
            }
        }
    }

    public void updateReport() {
        // Today
        DateTime today = new DateTime();
        DateTimeFormatter reportHeader = DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ");

        // iterate through sheets
        for (XSSFSheet sheet : workbook) {
            String sheetName = sheet.getSheetName();
            if (sheetName.contains(properties.getRegularTabMarker())) {
                System.out.println("Processing sheet \"" + sheetName.replace(properties.getRegularTabMarker(), "") + "\"...");
                XSSFRow currentRow = sheet.getRow(properties.getUpdateRow());
                XSSFCell currentCell = currentRow.getCell(properties.getUpdateColumn(), Row.CREATE_NULL_AS_BLANK);
                currentCell.setCellValue(reportHeader.withLocale(Locale.ENGLISH).print(today));
                if (isLabelRequired(sheet)) {
                    int searchStep = 50;
                    int searchPos = 0;
                    // Retrieve all issues from relevant projects with requested label(s)
                    String jqlString = "project in (" + String.join(", ", properties.getProjectKeysList()) + ") AND labels in (" + labels + ") ORDER BY issuekey ASC";
                    System.out.println("Searching for issues with label(s) " + labels + " in project(s) " + String.join(", ", properties.getProjectKeysList()));
                    // Number of issues that can be retrieved via search at once is limited
                    SearchResult searchResult = jiraClient.getSearchClient().searchJql(jqlString, searchStep, searchPos, null).claim();
                    int totalSearchResults = searchResult.getTotal();
                    System.out.println("Found " + totalSearchResults + " issues!");

                    if (totalSearchResults > 0) {
                        // Remove all rows with issues
                        removeRows(sheet, properties.getStartProcessingRow());
                        // Go through retrieved issues and publish details
                        do {
                            Iterator<Issue> issues = searchResult.getIssues().iterator();

                            while (issues.hasNext()) {
                                Issue issue = issues.next();
                                appendNewIssueRecord(sheet, "", issue, "");
                            }
                            searchPos += searchStep;
                            searchResult = jiraClient.getSearchClient().searchJql(jqlString, searchStep, searchPos, null).claim();
                        } while (searchPos < totalSearchResults);

                        adjustCellsWidth(sheet);
                        System.out.println("Sheet \"" + sheetName.replace(properties.getRegularTabMarker(), "") + "\" processed!");
                        return;
                    }
                }
                if (isUnfoldRequired(sheet)) {
                    // Remove all rows below
                    removeRows(sheet, properties.getStartProcessingRow() + 1);
                    // Publish details for root issue
                    currentRow = sheet.getRow(properties.getStartProcessingRow());
                    currentCell = currentRow.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK);
                    String issueKey = currentCell.getStringCellValue();
                    System.out.println("Retrieving issue " + issueKey);
                    publishIssueDetails(currentRow, jiraClient.getIssueClient().getIssue(issueKey).claim());

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
                                String issueKey = currentCell.getStringCellValue();
                                System.out.println("Retrieving issue " + issueKey);
                                publishIssueDetails(currentRow, jiraClient.getIssueClient().getIssue(issueKey).claim());
                            }
                        }
                        currentRow = sheet.getRow(rowNumber++);
                    }
                }

                adjustCellsWidth(sheet);
                System.out.println("Sheet \"" + sheetName.replace(properties.getRegularTabMarker(), "") + "\" processed!");
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

    void publishIssueDetails(XSSFRow row, Issue issue) {
        XSSFCell cell = row.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK);

        // Add hyperlink to current issue if required
        if (cell.getHyperlink() == null) {
            addHyperLink(cell);
        }

        // Add hyperlink to parent issue if required
        cell = row.getCell(properties.getIssueParentKeyColumn(), Row.CREATE_NULL_AS_BLANK);
        if (cell.getHyperlink() == null && !cell.getStringCellValue().isEmpty()) {
            addHyperLink(cell);
        }

        if (properties.isIssueSummaryFill()) {
            row.getCell(properties.getIssueSummaryColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getSummary());
        }

        TimeTracking time = issue.getTimeTracking();
        if (time != null) {
            row.getCell(properties.getIssueEstimationColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getOriginalEstimateMinutes()));
            row.getCell(properties.getIssueSpentColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getTimeSpentMinutes()));
            row.getCell(properties.getIssueRemainingColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(toHours(time.getRemainingEstimateMinutes()));
        } else {
            row.getCell(properties.getIssueEstimationColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue("N/A");
            row.getCell(properties.getIssueSpentColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue("N/A");
            row.getCell(properties.getIssueRemainingColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue("N/A");
        }
        row.getCell(properties.getIssueStatusColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getStatus().getName());

        Iterator<BasicComponent> iComponents = issue.getComponents().iterator();
        String components = "";
        while (iComponents.hasNext()) {
            if (components.isEmpty()) {
                components = iComponents.next().getName();
            } else {
                components = components + ", " + iComponents.next().getName();
            }
        }
        row.getCell(properties.getIssueComponentsColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(components);
        row.getCell(properties.getIssueAssigneeColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getAssignee() == null ? "" : issue.getAssignee().getDisplayName());
    }

    boolean isIssueInScope(String issueKey) {
        String[] issueKeyParts = issueKey.split(ISSUE_DIVIDER);
        return Arrays.asList(properties.getProjectKeysList()).contains(issueKeyParts[0]);
    }

    void publishDependentIssues(XSSFSheet sheet, String issueKey) {
        Issue rootIssue = jiraClient.getIssueClient().getIssue(issueKey).claim();

        Iterable<IssueLink> issueLinks = rootIssue.getIssueLinks();
        if (issueLinks != null) {
            // Handle referenced issues
            // TODO Testing required
            for (IssueLink issueLink : issueLinks) {
                if (linksList.containsKey(issueLink.getIssueLinkType().getDescription())) {
                    System.out.println("Retrieving issue " + issueLink.getTargetIssueKey());

                    appendNewIssueRecord(sheet, linksList.get(issueLink.getIssueLinkType().getDescription()), jiraClient.getIssueClient().getIssue(issueLink.getTargetIssueKey()).claim(), issueKey);
                    publishDependentIssues(sheet, issueLink.getTargetIssueKey());
                }
            }
        }

        Iterable<Subtask> subTasks = rootIssue.getSubtasks();

        if ((subTasks != null) && (properties.isUnfoldSubtasks())) {
            // Handle subtasks
            for (Subtask subtask : subTasks) {
                System.out.println("Retrieving issue " + subtask.getIssueKey());
                appendNewIssueRecord(sheet, SUBTASK_OF_VALUE, jiraClient.getIssueClient().getIssue(subtask.getIssueKey()).claim(), issueKey);
            }
        }
    }

    void appendNewIssueRecord(XSSFSheet sheet, String relation, Issue issue, String parentKey) {
        XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);

        createCells(row);
        row.getCell(properties.getIssueKeyColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getKey());
        row.getCell(properties.getIssueRelationColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(relation);
        row.getCell(properties.getIssueParentKeyColumn(), Row.CREATE_NULL_AS_BLANK).setCellValue(parentKey);

        publishIssueDetails(row, issue);
    }

    void adjustCellsWidth(XSSFSheet sheet) {
        sheet.autoSizeColumn(properties.getIssueSummaryColumn(), true);
        sheet.autoSizeColumn(properties.getIssueKeyColumn(), true);
        sheet.autoSizeColumn(properties.getIssueRelationColumn(), true);
        sheet.autoSizeColumn(properties.getIssueParentKeyColumn(), true);
        sheet.autoSizeColumn(properties.getIssueEstimationColumn(), true);
        sheet.autoSizeColumn(properties.getIssueSpentColumn(), true);
        sheet.autoSizeColumn(properties.getIssueRemainingColumn(), true);
        sheet.autoSizeColumn(properties.getIssueStatusColumn(), true);
        sheet.autoSizeColumn(properties.getIssueComponentsColumn(), true);
        sheet.autoSizeColumn(properties.getIssueAssigneeColumn(), true);
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

    void removeRows(XSSFSheet sheet, int startRow) {
        for (int i = startRow; i <= sheet.getLastRowNum(); ) {
            XSSFRow currentRow = sheet.getRow(i);
            // Skip missing rows
            if (currentRow != null) {
                sheet.removeRow(currentRow);
            } else {
                i++;
            }
        }
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

    boolean isLabelRequired(XSSFSheet sheet) {
        if (properties.isLabelUsed() == false) return false;
        XSSFRow currentRow = sheet.getRow(properties.getLabelRow());
        if (currentRow == null) return false;
        XSSFCell currentCell = currentRow.getCell(properties.getLabelColumn());
        if (currentCell == null) return false;
        labels = currentCell.getStringCellValue();
        if (labels.isEmpty()) return false;
        return true;
    }


    private static double toHours(Integer value) {
        return (double) (value == null ? 0 : value) / 60;
    }

    public boolean isJiraAnonymouslyAccessible() {
        return properties.isJiraAnonymousAccess();
    }
}