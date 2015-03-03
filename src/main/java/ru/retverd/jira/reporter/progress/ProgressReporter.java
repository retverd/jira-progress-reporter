package ru.retverd.jira.reporter.progress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

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

import ru.retverd.jira.helpers.JiraSimplifiedClient;

import com.atlassian.jira.rest.client.api.domain.Issue;

public class ProgressReporter {
    // Divider between project prefix and issue number
    static private final String ISSUE_DIVIDER = "-";
    // Underline hyperlinks
    static private final byte LINK_UNDERLINE_STYLE = Font.U_SINGLE;
    // Color for hyperlinks
    static private final short LINK_FONT_COLOR = IndexedColors.BLUE.getIndex();
    private PropertyHolder properties;
    private XSSFWorkbook workbook;
    private JiraSimplifiedClient jc;

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
	System.out.format("done!%n");
    }

    public void connectToJIRA(String login, String pass) throws IOException, URISyntaxException {
	// TODO handle incorrect credentials!
	jc = new JiraSimplifiedClient(properties.getJiraURL(), login, pass);
    }

    public void disconnectFromJIRA() throws IOException, URISyntaxException {
	jc.closeConnection();
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
		// update last update date
		currentCell.setCellValue(reportHeader.withLocale(Locale.ENGLISH).print(today));
		// iterate through issues
		int rowNumber = properties.getStartProcessingRow();
		currentRow = sheet.getRow(rowNumber++);
		while (currentRow != null) {
		    currentCell = currentRow.getCell(properties.getIssueKeyColumn());
		    String currentCellValue = currentCell.getStringCellValue();
		    if (isIssueInScope(currentCellValue)) {
			publishIssueDetails(currentRow, currentCellValue);
		    }
		    // get next row
		    currentRow = sheet.getRow(rowNumber++);
		}
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
	// Required for hyperlinks
	CreationHelper createHelper = workbook.getCreationHelper();
	// Prepare style and font for cells with hyperlinks
	Font hlinkFont = workbook.createFont();
	hlinkFont.setUnderline(LINK_UNDERLINE_STYLE);
	hlinkFont.setColor(LINK_FONT_COLOR);
	XSSFCell cell = row.getCell(properties.getIssueKeyColumn());

	System.out.println("Retrieving issue " + issueKey);
	Issue issue = jc.getIssueByKey(issueKey);
	// Insert URL to cell if it is not present yet and update style
	if (cell.getHyperlink() == null) {
	    Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
	    link.setAddress(properties.getJiraURL() + "/i#browse/" + cell);
	    cell.setHyperlink(link);
	    XSSFCellStyle linkStyle = workbook.createCellStyle();
	    linkStyle.cloneStyleFrom(cell.getCellStyle());
	    linkStyle.setFont(hlinkFont);
	    cell.setCellStyle(linkStyle);
	}
	// Save summary if required
	if (properties.getIssueSummaryFill()) {
	    cell = row.getCell(properties.getIssueSummaryColumn());
	    cell.setCellValue(issue.getSummary());
	}
	// Save estimation
	cell = row.getCell(properties.getIssueEstimationColumn());
	cell.setCellValue(toHours(issue.getTimeTracking().getOriginalEstimateMinutes()));
	// Save spent time
	cell = row.getCell(properties.getIssueSpentColumn());
	cell.setCellValue(toHours(issue.getTimeTracking().getTimeSpentMinutes()));
	// Save remaining time
	cell = row.getCell(properties.getIssueRemainingColumn());
	cell.setCellValue(toHours(issue.getTimeTracking().getRemainingEstimateMinutes()));
	// Save status
	cell = row.getCell(properties.getIssueStatusColumn());
	cell.setCellValue(issue.getStatus().getName());
    }

    public boolean isIssueInScope(String currentIssue) {
	String[] issueKeyParts = currentIssue.split(ISSUE_DIVIDER);
	return Arrays.asList(properties.getIssueKeyPrefixList()).contains(issueKeyParts[0]);
    }

    static public double toHours(Integer value) {
	return (double) (value == null ? 0 : value.intValue()) / 60;
    }
}