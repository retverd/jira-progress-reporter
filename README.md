# jira-progress-reporter
JIRA Progress Reporter is Java command-line application which allows to build different progress reports in Excel from JIRA based on estimated, spent and remaining time.

Based on jira-rest-java-client-re:
* Repository: https://github.com/retverd/jira-rest-java-client-re
* Issue tracker: https://github.com/retverd/jira-rest-java-client-re/issues

# disclaimer
I'm not a Java expert, I've just automated some reporting routines to simplify my work. So there are a lot of points to improve the code. If you want to support me, you'll be welcome:
* submit bugs, enhancements or questions: https://github.com/retverd/jira-progress-reporter/issues;
* feel free to help me with some issues: https://github.com/retverd/jira-progress-reporter/labels/help%20wanted;
* fork and pull request.

# limitations
* Only xlsx files are expected to be supported;
* Tested with JIRA v6.4#64014, v6.3.12#6343 and v6.4#64014;
* Only one root issue per sheet is supported;
* Root issue should be first, other rows will be deleted;
* Links for sub-tasks are not being analyzed;
* Use Java 7u67+ for Windows to avoid problems with column auto sizing;
* Time in report is only in hours;
* ...

# how to use
* download any release you want https://github.com/retverd/jira-progress-reporter/releases;
* update default.xlsx (put your own issues keys and summaries, root issues if required);
* update default.properties according to changes in default.xlsx;
* update default.bat if file names were changes;
* launch bat-file and follow prompts;
* submit bugs if you'll find any, but please consider existing issues: https://github.com/retverd/jira-progress-reporter/issues.

# F.A.Q.
* Q: ...
    * A: ...