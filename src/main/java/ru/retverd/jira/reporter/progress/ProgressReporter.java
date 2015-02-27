package ru.retverd.jira.reporter.progress;

import java.util.Locale;

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
    public static XSSFWorkbook updateReport(PropertyHolder properties, XSSFWorkbook workbook, JiraSimplifiedClient jc) throws Exception {
	// Today
	DateTime today = new DateTime();
	DateTimeFormatter reportHeader = DateTimeFormat.forPattern("dd MMM yyyy HH:mm ZZ");

	// Get the number of sheets in the xlsx file
	int numberOfSheets = workbook.getNumberOfSheets();
	// Required for hyperlinks
	CreationHelper createHelper = workbook.getCreationHelper();
	// Prepare style and font for cells with hyperlinks
	Font hlinkFont = workbook.createFont();
	hlinkFont.setUnderline(Font.U_SINGLE);
	hlinkFont.setColor(IndexedColors.BLUE.getIndex());

	// loop through each of the sheets
	for (int i = 0; i < numberOfSheets; i++) {
	    XSSFSheet sheet = workbook.getSheetAt(i);
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
		currentCell = currentRow.getCell(properties.getIssueKeyColumn());
		while (!currentCell.getStringCellValue().isEmpty()) {
		    String currentCellValue = currentCell.getStringCellValue();
		    if (!currentCellValue.equalsIgnoreCase(properties.getIssueKeySkipValue())) {
			System.out.println("Retrieving issue " + currentCellValue);
			Issue issue = jc.getIssueByKey(currentCellValue);
			// Insert URL to cell if it is not present yet and update style
			if (currentCell.getHyperlink() == null) {
			    Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
			    link.setAddress(properties.getJiraURL() + "/i#browse/" + currentCellValue);
			    currentCell.setHyperlink(link);
			    XSSFCellStyle linkStyle = workbook.createCellStyle();
			    linkStyle.cloneStyleFrom(currentCell.getCellStyle());
			    linkStyle.setFont(hlinkFont);
			    currentCell.setCellStyle(linkStyle);
			}
			// Save summary if required
			if (properties.getIssueSummaryFill()) {
			    currentCell = currentRow.getCell(properties.getIssueSummaryColumn());
			    currentCell.setCellValue(issue.getSummary());
			}
			// Save estimation
			currentCell = currentRow.getCell(properties.getIssueEstimationColumn());
			currentCell.setCellValue(toHours(issue.getTimeTracking().getOriginalEstimateMinutes()));
			// Save spent time
			currentCell = currentRow.getCell(properties.getIssueSpentColumn());
			currentCell.setCellValue(toHours(issue.getTimeTracking().getTimeSpentMinutes()));
			// Save remaining time
			currentCell = currentRow.getCell(properties.getIssueRemainingColumn());
			currentCell.setCellValue(toHours(issue.getTimeTracking().getRemainingEstimateMinutes()));
			// Save status
			currentCell = currentRow.getCell(properties.getIssueStatusColumn());
			currentCell.setCellValue(issue.getStatus().getName());
		    }
		    // get new cell
		    currentRow = sheet.getRow(rowNumber++);
		    if (currentRow == null)
			break;
		    currentCell = currentRow.getCell(properties.getIssueKeyColumn());
		    if (currentCell == null)
			break;
		}
		System.out.println("...done!");
	    } else {
		System.out.println("Sheet \"" + sheetName + "\" is skipped.");
	    }
	}

	// Refresh all formulas if required
	// Leads to exceptions on some Excel files with error message:
	// Unexpected ptg class (org.apache.poi.ss.formula.ptg.ArrayPtg)
	if (properties.getRecalculateFormulas()) {
	    System.out.format("Recalculating formulas...");
	    XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
	    System.out.format("done!%n");
	}

	return workbook;
    }

    static public double toHours(Integer value) {
	return (double) (value == null ? 0 : value.intValue()) / 60;
    }
}