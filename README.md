# jira-progress-reporter
JIRA Progress Reporter is Java command-line application which allows to build different progress reports in Excel from JIRA based on estimated, spent and remaining time.

Based on jira-rest-java-client-re:
* Repository: https://github.com/retverd/jira-rest-java-client-re
* Issue tracker: https://github.com/retverd/jira-rest-java-client-re/issues

# how to use
* download any release you want https://github.com/retverd/jira-progress-reporter/releases;
* update default.xlsx (put your own issues keys and summaries, root issues or labels if required);
* update default.properties according to changes in default.xlsx;
* update default.bat if file names were changes;
* launch bat-file and follow prompts;
* submit bugs if you'll find any, but please consider existing issues: https://github.com/retverd/jira-progress-reporter/issues.

# how it works
* Load and parse file with properties;
* Load file with report template;
* Go through all sheets one by one:
    * If sheet has sequence of chars from property excel.tab.regular.marker then sheet is being processed, otherwise sheet is skipped:
        * If excel.update properties are set then timestamp is added to corresponding cell;
        * If excel.label properties are set, corresponding cell is not empty and there are issues that belongs to project(s) mentioned in property excel.project.key and have specified label(s) then:
    		* All rows starting from excel.start_processing.row and below are being deleted;
			* All issues found are published starting from excel.start_processing.row and below;
			* Processing is being switched to the new sheet;
        * If excel.unfold.marker property is set, marker was found in excel.unfold.marker.column and excel.start_processing.row and issue in excel.start_processing.row belongs to project mentioned in property excel.project.key then:
        	* All rows starting from excel.start_processing.row + 1 and below are being deleted;
        	* Details for root issue (that is going to be unfolded) are published, all links of the issue are being analyzed;
        	* All issues that linked using relation mentioned in excel.unfold.links.list recurrent analysis is being applied;
        	* For each issue published it's details and details about subtasks if any found;
        	* Processing is being switched to the new sheet;
		* All rows are being analyzed, if issue key that belongs to project mentioned in property excel.project.key is found then issue details are being published;
		* When last row is reached, processing is being switched to the new sheet;
* If property excel.recalculate.formulas is set to y, then all formulas in document are being recalculated;
* If property report.filename.pattern is set, then report saved to new file;
* Otherwise existing file is being overwritten.

# disclaimer
I'm not a Java expert, I've just automated some reporting routines to simplify my work. So there are a lot of points to improve the code. If you want to support me, you'll be welcome:
* submit bugs, enhancements or questions: https://github.com/retverd/jira-progress-reporter/issues;
* feel free to help me with some issues: https://github.com/retverd/jira-progress-reporter/labels/help%20wanted;
* fork and pull request.

# limitations
* Only xlsx files are expected to be supported;
* Tested with JIRA v6.4#64014, v6.3.12#6343 and v6.4#64014;
* Retrieve issues with label:
    * Only intersection of labels is supported;
    * All rows will be deleted before population sheet with search results;
    * Issues with labels are retrieved only from project(s) specified in parameter excel.project.key;
    * Labels has higher priority than root issues;
    * For issues retrieved by search parent issue and relation will not be shown;
    * Issues ordered by issueKey ASC;
* Unfold issues (retrieve all linked issues):
    * Only one root issue per sheet is supported;
    * Root issue should be first;
    * All rows below root issue will be deleted;
    * Links for sub-tasks are not being analyzed;
* Use Java 7u67+ for Windows to avoid problems with column auto sizing;
* Time in report is only in hours;
* ...

# F.A.Q.
* Q: ...
    * A: ...