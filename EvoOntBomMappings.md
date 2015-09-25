I've tried and integrate bits of Bugtrackers2baetleMappings with an analysis of EvoOnt BOM (v. 4.1), drawing from some work undertaken in the Nepomuk project (for the SWIM service deployed at Mandriva), and some P.O.C work done for the Helios project's WP3 to try and integrate Debian bugs in that same framework.

The resulting table was converted from spreadsheet doc to CSV, so not really pretty for the moment.

Any comments much welcome. -- OlivierBerger

| **bom:Issue** | _An Issue is an entity defining a certain topic concerning the development of a software system. An issue can be classified or discussed about._ |  |  |  |  |  |  |  |  |  |  | |
|:--------------|:-------------------------------------------------------------------------------------------------------------------------------------------------|:-|:-|:-|:-|:-|:-|:-|:-|:-|:-|:|
|               |                                                                                                                                                  |  |  |  |  | **Mappings** |  |  |  |  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | **Ontologies** |  | **Bugtrackers** |  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | **Baetle** |  | **Trac**  | **Jira** | **Bugzilla.dtd** | **Debbugs (Debian)** |
|               | **Attribute**                                                                                                                                    | **type** |  |  |  |  |  |  |  |  |  |  |
|               | bugURL                                                                                                                                           | string |  |  |  |  |  |  |  |  | Urlbase + bug\_id |  |
|               | dateOpened                                                                                                                                       | dateTime |  |  |  |  | created  |  | Created  | Created  | creation\_ts |  |
|               | description                                                                                                                                      | string |  |  |  |  | description  |  | (Full) Description  | Description  | long\_desc |  |
|               | keyword                                                                                                                                          | string |  |  |  |  |  |  |  |  |  | keywords |
|               | lastModified                                                                                                                                     | dateTime |  |  |  |  | updated  |  | last modified  | updated  |  |  |
|               | number                                                                                                                                           | int |  |  |  |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  | **Relation** | **reverse relation** | **related entity** |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  | blocks | dependsOn ? | Issue |  | blocks  |  | ?  | ?  | blocked | Blocks |
|               |                                                                                                                                                  |  | dependsOn | blocks ? | Issue |  | depends\_on  |  | -  | -  | dependson | Blocked-By |
|               |                                                                                                                                                  |  | hasActivity | isActivityOf | Activity |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  | hasAssignee | isAssigneeOf | ns:User |  | assigned\_to  |  | Assigned to / Owner  | Assignee  | assigned\_to |  |
|               |                                                                                                                                                  |  | hasAttachment | isAttachmentOf | Attachment |  | sioc:attachment  |  | Attachment  | Attachment  | attachment |  |
|               |                                                                                                                                                  |  | hasCcPerson | isCcPersonOf | foaf:Person |  | interested  |  | Cc  | Watchers  | cc |  |
|               |                                                                                                                                                  |  | hasComment | isCommentOf | Comment |  | comment  |  | Comment  | Comment  | attachment ? |  |
|               |                                                                                                                                                  |  | hasComputerSystem | isComputerSystemOf | ComputerSystem |  | environment  |  | -  | environment  |  |  |
|               |                                                                                                                                                  |  | hasMilestone | isMilestoneOf | Milestone |  | target\_milestone  |  | milestone  | -  | target\_milestone |  |
|               |                                                                                                                                                  |  | hasReporter | isReporterOf | ns:User |  | reporter  |  | Reported by  | Reporter  | reporter | Submitter |
|               |                                                                                                                                                  |  | hasResolution |  | Resolution |  | resolved\_with  |  | ?  | ?  | resolution |  |
|               |                                                                                                                                                  |  | hasSeverity |  | **Severity** (see bellow) |  |  |  | Severity (optional)  | -  | bug\_severity | Severity |
|               |                                                                                                                                                  |  | hasState |  | flow:State |  |  |  |  |  | bug\_status | Status |
|               |                                                                                                                                                  |  | inProject |  | doap:Project |  | project  |  | (implicit)  | project  |  |  |
|               |                                                                                                                                                  |  | isIssueOf |  | Component |  | about  |  | Component  | Component  | component |  |
|               |                                                                                                                                                  |  | isFixedBy | fixes | doap:Version |  | version  |  | Version  | Fix version/s  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  |  |  |  |  |  |  |
| _Issue is subcalss of flow:Task_ |                                                                                                                                                  |  |  |  |  |  |  |  |  |  |  |  |
| **flow:Task** |                                                                                                                                                  |  |  |  |  |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  | **Relations** | **reverse relations** | **related entity** |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  | hasPriority | isPriorityOf | Priority |  |  |  |  |  | priority |  |
|               |                                                                                                                                                  |  |  |  |  |  |  |  |  |  |  |  |
|               |                                                                                                                                                  |  |  |  |  | **Missing elements in BOM ? :** |  |  |  |  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | title  |  | Summary  | Summary  | short\_desc | Subject |
|               |                                                                                                                                                  |  |  |  |  |  | due\_date  |  | (milestone due)  | due date  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | category  |  | ?  | ?  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | qa\_contact  |  | -  | -  | qa\_contact | Owner ? |
|               |                                                                                                                                                  |  |  |  |  |  | duplicate  |  | -  | -  |  | Merged-With |
|               |                                                                                                                                                  |  |  |  |  |  | causes  |  | -  | -  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | subtask  |  | -  | -  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  | votes  |  | -  | votes  | votes |  |
|               |                                                                                                                                                  |  |  |  |  |  |  |  | Ticket #  | Issue key  | bug\_id | bug\_num |
|               |                                                                                                                                                  |  |  |  |  |  |  |  | -  | Custom fields  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  |  |  | -  | Fix version's  |  |  |
|               |                                                                                                                                                  |  |  |  |  |  |  |  |  |  |  | Package |

| **bom:Severity** subclasses |  |  |  |  |  |  |  |  |  |  |  | |
|:----------------------------|:-|:-|:-|:-|:-|:-|:-|:-|:-|:-|:-|:|
|                             |  |  |  |  |  | **Mappings** |  |  |  |  |  |  |
|                             |  |  |  |  |  |  | **Ontologies** |  | **Bugtrackers** |  |  |  |
|                             |  |  |  |  |  |  | **Baetle** |  | **Trac**  | **Jira** | **Bugzilla.dtd** | **Debbugs (Debian)** |
| **Blocker**                 |  |  |  |  |  |  |  |  |  |  |  |  |
| **Critical**                |  |  |  |  |  |  |  |  |  |  |  | critical |
| **Feature**                 |  |  |  |  |  |  |  |  |  |  |  | wishlist |
| **Major**                   |  |  |  |  |  |  |  |  |  |  |  |  |
| **Minor**                   |  |  |  |  |  |  |  |  |  |  |  | minor |
| **Trivial**                 |  |  |  |  |  |  |  |  |  |  |  |  |
|                             |  |  |  |  |  |  |  |  |  |  |  | fixed |
|                             |  |  |  |  |  |  |  |  |  |  |  | grave |
|                             |  |  |  |  |  |  |  |  |  |  |  | normal |
|                             |  |  |  |  |  |  |  |  |  |  |  | important |
|                             |  |  |  |  |  |  |  |  |  |  |  | serious |