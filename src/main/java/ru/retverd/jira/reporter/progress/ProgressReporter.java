package ru.retverd.jira.reporter.progress;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.apache.log4j.Logger;
import org.apache.poi.POIXMLException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import ru.retverd.jira.reporter.progress.types.ConfigType;
import ru.retverd.jira.reporter.progress.types.IssueColumnsType;
import ru.retverd.jira.reporter.progress.types.ReportType;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URI;
import java.util.*;

public class ProgressReporter {
    // Divider between links prefix and issue number
    static private final String ISSUE_DIVIDER = "-";
    // String for subtasks
    static private final String SUBTASK_OF_VALUE = "subtask of";
    //
    static private final Integer AUTH_FAIL_STATUS = 401;
    //
    private static final Logger log = Logger.getLogger(ProgressReporter.class.getName());
    private String reportFile;
    // Relevant for one sheet only
    private String issueSummaryPrefixToHide;
    private Map<String, String> linksList;
    private List<String> affVersionsList;
    private List<String> componentsList;
    private List<String> labelsList;
    //
    private ConfigType config;
    private XSSFWorkbook workbook;
    private JiraRestClient jiraClient;
    private Set<String> searchFields;
    private List<String> projects;

    public ProgressReporter(String[] args) throws Throwable {
        String configFile = args[0];
        reportFile = args[1];

        // Load file with properties
        log.info("Loading and parsing properties from " + configFile);
        Unmarshaller unmarshaller = JAXBContext.newInstance(ConfigType.class).createUnmarshaller();
        unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());

        try {
            config = (ConfigType) unmarshaller.unmarshal(new File(configFile));
        } catch (JAXBException ex) {
            if (ex.getMessage() == null) {
                log.fatal(ex.getLinkedException().getMessage(), ex.getLinkedException());
                throw ex.getLinkedException();
            }
        }

        if (config.getJira().getProxy() != null) {
            System.setProperty("https.proxyHost", config.getJira().getProxy().getHost());
            System.setProperty("https.proxyPort", config.getJira().getProxy().getPort());
            log.info("Proxy server " + config.getJira().getProxy().getHost() + ":" + config.getJira().getProxy().getPort() + " is being used!");
        }

        // Connect to Jira
        if (config.getJira().isAnonymous()) {
            // TODO handle missing access!
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            jiraClient = factory.createWithAnonymousAccess(new URI(config.getJira().getUrl()));
            log.info("Anonymous factory was used.");
            checkConnection("Anonymous access is prohibited!");
        } else {
            // Request credentials for JIRA
            String login;
            String pass;
            // TODO handle empty credentials!
            if (args.length == 4) {
                login = args[2];
                pass = args[3];
            } else {
                String loginPrompt = "Please enter your login: ";
                String passPrompt = "Please enter your password: ";
                Console console = System.console();
                System.out.print(loginPrompt);
                if (console == null) {
                    Scanner in = new Scanner(System.in);
                    login = in.next();
                    System.out.print(passPrompt);
                    pass = in.next();
                    in.close();
                } else {
                    login = console.readLine();
                    System.out.print(passPrompt);
                    pass = new String(console.readPassword());
                }
            }
            // TODO handle incorrect credentials!
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            jiraClient = factory.createWithBasicHttpAuthentication(new URI(config.getJira().getUrl()), login, pass);
            log.info("Basic http authentication factory was used.");
            checkConnection("Authentication error! Please check your credentials!");
        }

        this.searchFields = new HashSet<String>();
        searchFields.add("summary");
        searchFields.add("created");
        searchFields.add("updated");
        searchFields.add("links");
        searchFields.add("status");
        searchFields.add("issuetype");
        searchFields.add("assignee");
        searchFields.add("components");
        searchFields.add("timetracking");
        searchFields.add("labels");
        searchFields.add("versions");
        searchFields.add("project");

        projects = config.getJira().getProjects();

        linksList = new HashMap<String, String>();
        if (config.getReport().getRootIssue() != null) {
            log.info("Getting IssueLinkTypes from server...");
            Iterable<IssuelinksType> issueLinkTypes = jiraClient.getMetadataClient().getIssueLinkTypes().claim();
            for (String link : config.getReport().getRootIssue().getLinks()) {
                for (IssuelinksType issueLinkType : issueLinkTypes) {
                    if (issueLinkType.getInward().equals(link)) {
                        linksList.put(link, issueLinkType.getOutward());
                        break;
                    } else if (issueLinkType.getOutward().equals(link)) {
                        linksList.put(link, issueLinkType.getInward());
                        break;
                    }
                }
                log.error("Issue link type '" + link + "' is missing on server!");
            }
        }

        // Load Excel file with report timePattern
        log.info("Reading report file " + reportFile);
        // OPCPackage is not used due to problems with saving results: org.apache.poi.openxml4j.exceptions.OpenXML4JException: The part /docProps/app.xml fail
        // to be saved in the stream with marshaller org.apache.poi.openxml4j.opc.internal.marshallers.DefaultMarshaller@1c67c1a6
        FileInputStream fis = new FileInputStream(reportFile);
        try {
            workbook = new XSSFWorkbook(fis);
        } catch (POIXMLException e) {
            log.fatal("Report file " + reportFile + " has incorrect format.", e);
            throw new ConfigurationException("Report file " + reportFile + " has incorrect format.");
        } catch (Throwable e) {
            log.fatal("Unexpected exception - " + e.getMessage(), e);
            throw e;
        } finally {
            fis.close();
        }
    }

    public void updateReport() {
        // Prepare time stamp if required
        String todayPrintable = "";
        if (config.getReport().getUpdateDate() != null) {
            DateTime today = new DateTime();
            DateTimeFormatter reportHeader = DateTimeFormat.forPattern(config.getReport().getUpdateDate().getTimePattern());
            todayPrintable = reportHeader.withLocale(Locale.ENGLISH).print(today);
        }

        // Iterate through sheets
        for (XSSFSheet sheet : workbook) {
            String sheetName = sheet.getSheetName();
            if (sheetName.contains(config.getReport().getMarker())) {
                log.info("Processing sheet \"" + sheetName.replace(config.getReport().getMarker(), "") + "\"...");
                if (config.getReport().getUpdateDate() != null) {
                    XSSFRow updateRow = sheet.getRow(config.getReport().getUpdateDate().getRow());
                    if (updateRow == null) {
                        log.error("Row " + humanizeRow(config.getReport().getUpdateDate().getRow()) + " for update date is missing on sheet " + sheetName + ".");
                    } else {
                        updateRow.getCell(config.getReport().getUpdateDate().getCol(), Row.CREATE_NULL_AS_BLANK).setCellValue(todayPrintable);
                    }
                }
                // Prefix for issue summary to be hidden
                issueSummaryPrefixToHide = "";
                if (config.getReport().getToHide() != null) {
                    if (config.getReport().getToHide().getIssuePrefixRow() != null && config.getReport().getToHide().getIssuePrefixCol() != null) {
                        XSSFRow row = sheet.getRow(config.getReport().getToHide().getIssuePrefixRow());
                        if (row != null) {
                            XSSFCell cell = row.getCell(config.getReport().getToHide().getIssuePrefixCol());
                            if (cell != null) {
                                issueSummaryPrefixToHide = cell.getStringCellValue();
                            }
                        }
                    }
                }
                // List of affVersions to be hidden
                affVersionsList = new ArrayList<String>();
                if (config.getReport().getToHide() != null) {
                    if (config.getReport().getToHide().getAffectedVersionRow() != null && config.getReport().getToHide().getAffectedVersionCol() != null) {
                        XSSFRow row = sheet.getRow(config.getReport().getToHide().getAffectedVersionRow());
                        if (row != null) {
                            XSSFCell cell = row.getCell(config.getReport().getToHide().getAffectedVersionCol());
                            if (cell != null) {
                                affVersionsList = Arrays.asList(cell.getStringCellValue().trim().split(","));
                            }
                        }
                    }
                }
                // List of components to be hidden
                componentsList = new ArrayList<String>();
                if (config.getReport().getToHide() != null) {
                    if (config.getReport().getToHide().getComponentsRow() != null && config.getReport().getToHide().getComponentsCol() != null) {
                        XSSFRow row = sheet.getRow(config.getReport().getToHide().getComponentsRow());
                        if (row != null) {
                            XSSFCell cell = row.getCell(config.getReport().getToHide().getComponentsCol());
                            if (cell != null) {
                                componentsList = Arrays.asList(cell.getStringCellValue().trim().split(","));
                            }
                        }
                    }
                }
                // List of labels to be hidden
                labelsList = new ArrayList<String>();
                if (config.getReport().getToHide() != null) {
                    if (config.getReport().getToHide().getLabelsRow() != null && config.getReport().getToHide().getLabelsCol() != null) {
                        XSSFRow row = sheet.getRow(config.getReport().getToHide().getLabelsRow());
                        if (row != null) {
                            XSSFCell cell = row.getCell(config.getReport().getToHide().getLabelsCol());
                            if (cell != null) {
                                labelsList = Arrays.asList(cell.getStringCellValue().replaceAll("\\s", "").split(","));
                            }
                        }
                    }
                }

                String jqlQuery = getJQLQuery(sheet);
                String rootIssueKey = getRootIssueKey(sheet);
                if (jqlQuery != null) {
                    // Remove all rows with issues
                    removeRows(sheet, config.getReport().getStartProcessingRow());
                    log.info("Search for issues will be initiated, all rows starting from issueKeyRow " + humanizeRow(config.getReport().getStartProcessingRow()) + " were deleted.");

                    int searchPos = 0;
                    int searchStep = config.getReport().getJqlQuery().getSearchStep();
                    // Retrieve all issues from relevant projects with requested label(s)
                    log.info("Searching for issues using request: " + jqlQuery + "...");
                    SearchResult searchResult = jiraClient.getSearchClient().searchJql(jqlQuery, searchStep, searchPos, searchFields).claim();
                    int totalSearchResults = searchResult.getTotal();
                    log.info(totalSearchResults + " issue(s) found.");

                    if (totalSearchResults > 0) {
                        boolean flag = config.getReport().getProcessingFlags().isIssueSummaryUpdate();
                        config.getReport().getProcessingFlags().setIssueSummaryUpdate(true);
                        // Go through retrieved issues and publish details
                        do {
                            for (Issue issue : searchResult.getIssues()) {
                                XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);

                                publishIssueDetails(row, issue, null, null);
                            }
                            searchPos += searchStep;
                            searchResult = jiraClient.getSearchClient().searchJql(jqlQuery, searchStep, searchPos, null).claim();
                        } while (searchPos < totalSearchResults);
                        config.getReport().getProcessingFlags().setIssueSummaryUpdate(flag);
                    }
                } else if (rootIssueKey != null) {
                    // Remove all rows with issues
                    removeRows(sheet, config.getReport().getStartProcessingRow());
                    log.info("Search for issues will be initiated, all rows starting from issueKeyRow " + humanizeRow(config.getReport().getStartProcessingRow()) + " were deleted.");
                    // Publish details for root issue
                    XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                    log.info("Retrieving issue " + rootIssueKey);
                    publishIssueDetails(row, jiraClient.getIssueClient().getIssue(rootIssueKey).claim(), null, null);

                    // Walk through linked issues and subtasks and add rows with details
                    boolean flag = config.getReport().getProcessingFlags().isIssueSummaryUpdate();
                    config.getReport().getProcessingFlags().setIssueSummaryUpdate(true);
                    publishDependentIssues(sheet, rootIssueKey);
                    config.getReport().getProcessingFlags().setIssueSummaryUpdate(flag);
                } else {
                    int rowNumber = config.getReport().getStartProcessingRow();
                    XSSFRow row = sheet.getRow(rowNumber++);
                    while (row != null) {
                        XSSFCell cell = row.getCell(config.getReport().getIssueColumns().getKey(), Row.CREATE_NULL_AS_BLANK);
                        if (cell != null) {
                            String issueKey = cell.getStringCellValue();
                            if (isIssueInScope(issueKey)) {
                                log.info("Retrieving issue " + issueKey);
                                publishIssueDetails(row, jiraClient.getIssueClient().getIssue(issueKey).claim(), null, null);
                            }
                        }
                        row = sheet.getRow(rowNumber++);
                    }
                }

                if (config.getReport().getProcessingFlags().isAutosizeColumns()) {
                    adjustCellsWidth(sheet);
                }
                log.info("Sheet \"" + sheetName.replace(config.getReport().getMarker(), "") + "\" processed!");
            } else {
                log.info("Sheet \"" + sheetName + "\" is skipped.");
            }

            // Refresh all formulas if required
            // Leads to exceptions on some Excel files with error message: Unexpected ptg class (org.apache.poi.ss.formula.ptg.ArrayPtg)
            // See https://github.com/retverd/jira-progress-reporter/issues/1 (Problems with evaluateAllFormulaCells)

            if (config.getReport().getProcessingFlags().isRecalculateFormulas()) {
                log.info("Recalculating formulas...");
                XSSFFormulaEvaluator.evaluateAllFormulaCells(workbook);
            }
        }

    }

    private void publishDependentIssues(XSSFSheet sheet, String issueKey) {
        Issue rootIssue = jiraClient.getIssueClient().getIssue(issueKey).claim();

        Iterable<IssueLink> issueLinks = rootIssue.getIssueLinks();
        if (issueLinks != null) {
            // Handle referenced issues
            for (IssueLink issueLink : issueLinks) {
                if (linksList.containsKey(issueLink.getIssueLinkType().getDescription())) {
                    log.info("Retrieving issue " + issueLink.getTargetIssueKey());
                    XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                    publishIssueDetails(row, jiraClient.getIssueClient().getIssue(issueLink.getTargetIssueKey()).claim(), linksList.get(issueLink.getIssueLinkType().getDescription()), issueKey);
                    publishDependentIssues(sheet, issueLink.getTargetIssueKey());
                }
            }
        }

        Iterable<Subtask> subTasks = rootIssue.getSubtasks();

        if (subTasks != null && config.getReport().getRootIssue().isUnfoldSubtasks()) {
            // Handle subtasks
            for (Subtask subtask : subTasks) {
                log.info("Retrieving issue " + subtask.getIssueKey());
                XSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
                publishIssueDetails(row, jiraClient.getIssueClient().getIssue(subtask.getIssueKey()).claim(), SUBTASK_OF_VALUE, issueKey);
            }
        }
    }

    private void publishIssueDetails(XSSFRow row, Issue issue, String relation, String parentKey) {
        String est = "N/A";
        String spent = "N/A";
        String rem = "N/A";
        ReportType report = config.getReport();

        if (report.getProcessingFlags().isIssueSummaryUpdate() && report.getIssueColumns().getSummary() != null) {
            row.getCell(report.getIssueColumns().getSummary(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getSummary().replaceFirst(issueSummaryPrefixToHide, ""));
        }

        row.getCell(report.getIssueColumns().getKey(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getKey());

        if (relation != null && report.getIssueColumns().getRelation() != null) {
            row.getCell(report.getIssueColumns().getRelation(), Row.CREATE_NULL_AS_BLANK).setCellValue(relation);
        }

        if (parentKey != null && report.getIssueColumns().getParentKey() != null) {
            row.getCell(report.getIssueColumns().getParentKey(), Row.CREATE_NULL_AS_BLANK).setCellValue(parentKey);
        }

        TimeTracking time = issue.getTimeTracking();
        if (time != null) {
            est = String.valueOf(toHours(time.getOriginalEstimateMinutes()));
            spent = String.valueOf(toHours(time.getTimeSpentMinutes()));
            rem = String.valueOf(toHours(time.getRemainingEstimateMinutes()));
        }

        if (report.getIssueColumns().getEstimation() != null) {
            row.getCell(report.getIssueColumns().getEstimation(), Row.CREATE_NULL_AS_BLANK).setCellValue(est);
        }

        if (report.getIssueColumns().getSpentTime() != null) {
            row.getCell(report.getIssueColumns().getSpentTime(), Row.CREATE_NULL_AS_BLANK).setCellValue(spent);
        }

        if (report.getIssueColumns().getRemainingTime() != null) {
            row.getCell(report.getIssueColumns().getRemainingTime(), Row.CREATE_NULL_AS_BLANK).setCellValue(rem);
        }

        if (report.getIssueColumns().getStatus() != null) {
            row.getCell(report.getIssueColumns().getStatus(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getStatus().getName());
        }

        if (report.getIssueColumns().getAfVersion() != null && issue.getAffectedVersions() != null) {
            Iterator<Version> iVersions = issue.getAffectedVersions().iterator();
            String versions = "";
            while (iVersions.hasNext()) {
                String iVersion = iVersions.next().getName();
                // Check for redundant affected versions
                if (!affVersionsList.contains(iVersion)) {
                    versions = versions + ", " + iVersion;
                }
            }
            versions = versions.replaceFirst(", ", "");
            row.getCell(report.getIssueColumns().getAfVersion(), Row.CREATE_NULL_AS_BLANK).setCellValue(versions);
        }

        if (report.getIssueColumns().getComponents() != null && issue.getComponents() != null) {
            Iterator<BasicComponent> iComponents = issue.getComponents().iterator();
            String components = "";
            while (iComponents.hasNext()) {
                String iComponent = iComponents.next().getName();
                // Check for redundant components
                if (!componentsList.contains(iComponent)) {
                    components = components + ", " + iComponent;
                }
            }
            components = components.replaceFirst(", ", "");
            row.getCell(report.getIssueColumns().getComponents(), Row.CREATE_NULL_AS_BLANK).setCellValue(components);
        }

        if (report.getIssueColumns().getLabels() != null && issue.getLabels() != null) {
            Iterator<String> iLabels = issue.getLabels().iterator();
            String labels = "";
            while (iLabels.hasNext()) {
                String iLabel = iLabels.next();
                // Check for redundant labels
                if (!labelsList.contains(iLabel)) {
                    labels = labels + ", " + iLabel;
                }
            }
            labels = labels.replaceFirst(", ", "");
            row.getCell(report.getIssueColumns().getLabels(), Row.CREATE_NULL_AS_BLANK).setCellValue(labels);
        }

        if (report.getIssueColumns().getAssignee() != null) {
            row.getCell(report.getIssueColumns().getAssignee(), Row.CREATE_NULL_AS_BLANK).setCellValue(issue.getAssignee() == null ? "" : issue.getAssignee().getDisplayName());
        }

    }

    private void adjustCellsWidth(XSSFSheet sheet) {
        IssueColumnsType columns = config.getReport().getIssueColumns();

        if (columns.getSummary() != null) sheet.autoSizeColumn(columns.getSummary(), true);
        if (columns.getKey() != null) sheet.autoSizeColumn(columns.getKey(), true);
        if (columns.getRelation() != null) sheet.autoSizeColumn(columns.getRelation(), true);
        if (columns.getParentKey() != null) sheet.autoSizeColumn(columns.getParentKey(), true);
        if (columns.getEstimation() != null) sheet.autoSizeColumn(columns.getEstimation(), true);
        if (columns.getSpentTime() != null) sheet.autoSizeColumn(columns.getSpentTime(), true);
        if (columns.getRemainingTime() != null) sheet.autoSizeColumn(columns.getRemainingTime(), true);
        if (columns.getStatus() != null) sheet.autoSizeColumn(columns.getStatus(), true);
        if (columns.getAfVersion() != null) sheet.autoSizeColumn(columns.getAfVersion(), true);
        if (columns.getComponents() != null) sheet.autoSizeColumn(columns.getComponents(), true);
        if (columns.getLabels() != null) sheet.autoSizeColumn(columns.getLabels(), true);
        if (columns.getAssignee() != null) sheet.autoSizeColumn(columns.getAssignee(), true);
    }

    private int humanizeRow(int row) {
        return row + 1;
    }

    String humanizeColumn(int column) {
        return CellReference.convertNumToColString(column);
    }

    private void removeRows(XSSFSheet sheet, int startRow) {
        for (int i = startRow; i <= sheet.getLastRowNum(); ) {
            XSSFRow currentRow = sheet.getRow(i++);
            // Skip missing rows
            if (currentRow != null) {
                sheet.removeRow(currentRow);
            }
        }
    }

    private double toHours(Integer value) {
        return (double) (value == null ? 0 : value) / 60;
    }

    private boolean isIssueInScope(String issueKey) {
        String[] issueKeyParts = issueKey.split(ISSUE_DIVIDER);
        if (issueKeyParts.length != 2) {
            return false;
        }
        if (projects != null) {
            if (!projects.contains(issueKeyParts[0])) {
                return false;
            }
        }
        return true;

    }

    private String getRootIssueKey(XSSFSheet sheet) {
        if (config.getReport().getRootIssue() == null) {
            return null;
        }
        XSSFRow row = sheet.getRow(config.getReport().getRootIssue().getIssueKeyRow());
        if (row == null) return null;
        XSSFCell cell = row.getCell(config.getReport().getRootIssue().getIssueKeyCol());
        if (cell == null) return null;
        if (cell.getStringCellValue().isEmpty()) return null;
        String key = cell.getStringCellValue();
        if (!isIssueInScope(key)) return null;
        return key;
    }

    private String getJQLQuery(XSSFSheet sheet) {
        if (config.getReport().getJqlQuery() == null) {
            return null;
        }
        XSSFRow row = sheet.getRow(config.getReport().getJqlQuery().getRow());
        if (row == null) return null;
        XSSFCell cell = row.getCell(config.getReport().getJqlQuery().getCol());
        if (cell == null) return null;
        if (cell.getStringCellValue().isEmpty()) return null;
        return cell.getStringCellValue();
    }

    public void saveReport() throws IOException {
        String notification = "Rewriting";
        String targetFile = reportFile;

        if (config.getReport().getReportName() != null) {
            targetFile = config.getReport().getReportName().getFullName(new DateTime()) + ".xlsx";
            notification = "Saving report to";
        }

        log.info(notification + " file " + targetFile + "...");
        FileOutputStream fos = new FileOutputStream(new File(targetFile));
        workbook.write(fos);
        fos.close();
    }

    private void checkConnection(String errorMessage) throws Exception {
        try {
            ServerInfo si = jiraClient.getMetadataClient().getServerInfo().claim();
            log.info("Successfully connected to Jira instance v." + si.getVersion());
        } catch (RestClientException e) {
            disconnect();
            com.google.common.base.Optional<Integer> statusCode = e.getStatusCode();
            if (statusCode.isPresent()) {
                // Handle exception for incorrect credentials
                if (statusCode.get().equals(AUTH_FAIL_STATUS)) {
                    log.fatal(errorMessage, e);
                    throw new IOException(errorMessage);
                }
            }
            log.fatal("Unexpected status code - " + e.getStatusCode(), e);
            throw e;
        } catch (Exception e) {
            disconnect();
            log.fatal("Unexpected exception - " + e.getMessage(), e);
            throw e;
        }
    }

    public void disconnect() throws IOException {
        jiraClient.close();
    }
}