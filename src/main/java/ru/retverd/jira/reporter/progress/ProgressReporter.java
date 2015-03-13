package ru.retverd.jira.reporter.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import com.atlassian.jira.rest.client.api.domain.*;
import org.apache.poi.POIXMLException;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.base.Optional;

public class ProgressReporter {
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

    public void connectToJIRA(String login, String pass) throws IOException, URISyntaxException {
        // TODO handle incorrect credentials!
        JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI uri = new URI(properties.getJiraURL());
        jiraClient = factory.createWithBasicHttpAuthentication(uri, login, pass);

        try {
            ServerInfo si = jiraClient.getMetadataClient().getServerInfo().claim();
            System.out.println("Just connected to Jira instance v." + si.getVersion());
        } catch (RestClientException e) {
            disconnectFromJIRA();
            Optional<Integer> statusCode = e.getStatusCode();
            if (statusCode.isPresent()) {
                // Handle exception for incorrect credentials
                if (statusCode.get().equals(AUTH_FAIL_STATUS)) {
                    System.out.println("Authentication error! Please check your credentials!");
                    return;
                }
            }
            throw e;
        }
    }

    public void disconnectFromJIRA() throws IOException, URISyntaxException {
        jiraClient.close();
    }

    public void updateReport() throws Exception {
        // Today
        DateTime today = new DateTime();
        DateTimeFormatter reportHeader = DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ");

        // iterate through sheets
        for (XSSFSheet sheet : workbook) {
            String sheetName = sheet.getSheetName();
            if (sheetName.contains(properties.getRegularTabMarker())) {
                System.out.println("Processing sheet " + sheetName.replace(properties.getRegularTabMarker(), "") + "...");
                XSSFRow currentRow = sheet.getRow(properties.getUpdateRow());
                XSSFCell currentCell = currentRow.getCell(properties.getUpdateColumn());
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
                    currentCell = currentRow.getCell(properties.getIssueKeyColumn());
                    publishIssueDetails(currentRow, currentCell.getStringCellValue());

                    // Walk through linked issues and subtasks and put add rows with details
                    boolean flag = properties.getIssueSummaryFill();
                    properties.setIssueSummaryFill(true);
                    publishDependentIssues(sheet, currentCell.getStringCellValue());
                    properties.setIssueSummaryFill(flag);
                    // Remove unfold marker
                    currentRow.getCell(properties.getUnfoldMarkerColumn()).setCellValue("");
                } else {
                    int rowNumber = properties.getStartProcessingRow();
                    currentRow = sheet.getRow(rowNumber++);
                    while (currentRow != null) {
                        currentCell = currentRow.getCell(properties.getIssueKeyColumn());
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
        if (properties.getRecalculateFormulas()) {
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

    public void publishIssueDetails(XSSFRow row, String issueKey) {
        XSSFCell cell = row.getCell(properties.getIssueKeyColumn());

        // Add hyperlink to current issue if required
        if (cell.getHyperlink() == null) {
            addHyperLink(cell);
        }

        // Add hyperlink to parent issue if required
        cell = row.getCell(properties.getIssueParentKeyColumn());
        if ((cell.getHyperlink() == null) && (cell.getStringCellValue() != null)) {
            addHyperLink(cell);
        }

        System.out.println("Retrieving issue " + issueKey);
        Issue issue = jiraClient.getIssueClient().getIssue(issueKey).claim();
        if (properties.getIssueSummaryFill()) {
            row.getCell(properties.getIssueSummaryColumn()).setCellValue(issue.getSummary());
        }
        row.getCell(properties.getIssueEstimationColumn()).setCellValue(toHours(issue.getTimeTracking().getOriginalEstimateMinutes()));
        row.getCell(properties.getIssueSpentColumn()).setCellValue(toHours(issue.getTimeTracking().getTimeSpentMinutes()));
        row.getCell(properties.getIssueRemainingColumn()).setCellValue(toHours(issue.getTimeTracking().getRemainingEstimateMinutes()));
        row.getCell(properties.getIssueStatusColumn()).setCellValue(issue.getStatus().getName());
    }

    public boolean isIssueInScope(String issueKey) {
        String[] issueKeyParts = issueKey.split(ISSUE_DIVIDER);
        return Arrays.asList(properties.getIssueKeyPrefixList()).contains(issueKeyParts[0]);
    }

    public void publishDependentIssues(XSSFSheet sheet, String issueKey) {
        properties.setIssueSummaryFill(true);

        Issue rootIssue = jiraClient.getIssueClient().getIssue(issueKey).claim();

        Iterable<IssueLink> issueLinks = rootIssue.getIssueLinks();
        if (issueLinks != null) {
            // Handle referenced issues
            // TODO Testing required
            for (IssueLink issueLink : issueLinks) {
                if(issueLink.getIssueLinkType().getDirection().equals(IssueLinkType.Direction.OUTBOUND) && issueLink.getIssueLinkType().getName().equals(PART_OF_FLAG)){
                    appendNewIssueRecord(sheet, PART_OF_VALUE, issueLink.getTargetIssueKey(), issueKey);
                    publishDependentIssues(sheet,issueLink.getTargetIssueKey());
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

    public void appendNewIssueRecord(XSSFSheet sheet, String relation, String issueKey, String parentKey) {
        XSSFRow row;

        row = sheet.createRow(sheet.getLastRowNum() + 1);
        createCells(row);
        row.getCell(properties.getIssueKeyColumn()).setCellValue(issueKey);
        row.getCell(properties.getIssueRelationColumn()).setCellValue(relation);
        row.getCell(properties.getIssueParentKeyColumn()).setCellValue(parentKey);
        publishIssueDetails(row, issueKey);
    }

    public void adjustCellsWidth(XSSFSheet sheet) {
        sheet.autoSizeColumn(properties.getIssueSummaryColumn());
        sheet.autoSizeColumn(properties.getIssueKeyColumn());
        sheet.autoSizeColumn(properties.getIssueRelationColumn());
        sheet.autoSizeColumn(properties.getIssueParentKeyColumn());
        sheet.autoSizeColumn(properties.getUnfoldMarkerColumn());
        sheet.autoSizeColumn(properties.getIssueEstimationColumn());
        sheet.autoSizeColumn(properties.getIssueSpentColumn());
        sheet.autoSizeColumn(properties.getIssueRemainingColumn());
        sheet.autoSizeColumn(properties.getIssueStatusColumn());
    }

    public void createCells(XSSFRow row) {
        row.createCell(properties.getIssueSummaryColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueKeyColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueRelationColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueParentKeyColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_STRING);
        row.createCell(properties.getIssueEstimationColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueSpentColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueRemainingColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_NUMERIC);
        row.createCell(properties.getIssueStatusColumn(), org.apache.poi.xssf.usermodel.XSSFCell.CELL_TYPE_STRING);
    }

    public void addHyperLink(XSSFCell cell) {
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

    public boolean isUnfoldRequired(XSSFSheet sheet) {
        if (properties.getUnfoldMarker() != null) {
            // Root issue is expected only in first row
            XSSFRow currentRow = sheet.getRow(properties.getStartProcessingRow());
            String issueKey = currentRow.getCell(properties.getIssueKeyColumn()).getStringCellValue();
            String unfoldMarker = currentRow.getCell(properties.getUnfoldMarkerColumn()).getStringCellValue();
            if (isIssueInScope(issueKey) && properties.getUnfoldMarker().equals(unfoldMarker)) {
                return true;
            }
        }
        return false;
    }

    static public double toHours(Integer value) {
        return (double) (value == null ? 0 : value) / 60;
    }
}