# jira-progress-reporter
JIRA Progress Reporter is Java command-line application which allows to build different progress reports in Excel from JIRA based on estimated, spent and remaining time.

Based on jira-rest-java-client-re:
* Repository: https://github.com/retverd/jira-rest-java-client-re
* Issue tracker: https://github.com/retverd/jira-rest-java-client-re/issues

# how to use
* download any release you want https://github.com/retverd/jira-progress-reporter/releases or use latest commit;
* customize default.xlsx according to your needs;
* generate xml from progress_reporter.xsd and customize it or default_config.xml according to changes in report template considering annotations in progress_reporter.xsd; 
* validate configuration xml file against schema;
* update default.bat if file names were changes;
* launch bat-file and follow prompts;
* submit bugs if you'll find any, but please consider existing ones: https://github.com/retverd/jira-progress-reporter/issues.

# how it works
* Load and parse file with properties;
* Establish connection to Jira;
* Load file with report template;
* Go through all sheets one by one:
    * If sheet has sequence of chars from tag config -> report -> marker then sheet is being processed, otherwise sheet is skipped:
        * If config -> report -> updateDate properties are set then timestamp is added to corresponding cell;
        * If config -> report -> jqlQuery properties are set and corresponding cell is not empty then:
    		* All rows starting from config -> report -> startProcessingRow and below are being deleted;
			* All issues found are published starting from excel.start_processing.row and below;
        * Else if config -> report -> rootIssue properties are set and corresponding cell contains issue key that belongs to project(s) listed in config -> jira -> projects then:
        	* All rows starting from config -> report -> startProcessingRow and below are being deleted;
        	* Details for root issue are published, all links of the issue that listed in config -> report -> rootIssue -> links are being analyzed recurrent;
        	* For each issue it's details and details about subtasks if any found are published;
		* Else all rows are being analyzed, if issue key that belongs to project(s) listed in config -> jira -> projects is found then issue details are being published;
		* If property config -> report -> processingFlags -> autosizeColumns is set to true then all columns specified in config -> report -> issueColumns are adjusted to their new sizes; 
* If property config -> report -> processingFlags -> recalculateFormulas is set to true, then all formulas in document are being recalculated;
* If properties config -> report -> reportName are set, then report saved to new file;
* Otherwise existing file is being overwritten.

# disclaimer
I'm not a Java expert, I've just automated some reporting routines to simplify my work. So there are a lot of points to improve the code. If you want to support me, you'll be welcome:
* submit bugs, enhancements or questions: https://github.com/retverd/jira-progress-reporter/issues;
* feel free to help me with some issues: https://github.com/retverd/jira-progress-reporter/labels/help%20wanted;
* fork and pull request.

# limitations
* Tested with Excel 2010;
* Tested with JIRA v6.4#64014, v6.3.12#6343 and v6.4#64014;
* Only xlsx files are expected to be supported;
* Retrieve issues using jql query:
    * For issues retrieved by search parent issue and relation will not be shown;
* Retrieve issues linked to root one:
	* There is no check for duplicates, so issue linked to several issues will be published several times;
    * Links for sub-tasks are not being analyzed;
* Use Java 7u67+ for Windows to avoid problems with column auto sizing;
* Time in report is only in hours;
* ...

# F.A.Q.
* Q: ...
    * A: ...