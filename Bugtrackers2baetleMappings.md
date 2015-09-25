# Introduction #

This page shows intended **mappings from different bugtrackers to baetle**.

Feel free to update this page!!

The goals of this page are to:
  1. Be a guideline for how one should map bugtracker properties to baetle
  1. Help to verify that baetle is "complete"



# Details #

The tables below shows how different concepts and properties of different bugtrackers should be mapped to baetle. _Concepts and properties within parentheses needs some clarification_ - see the mailing list for possible details.

## baetle:Issue ##
baetle:Issue inherits from wf:Task. It has Defect and Enhancement as subclasses. There's a discussion on the mailing list on where to map (Jira and Tracs) Task. Other custom Issue types that are used are also of interest to discuss.

| **Baetle** | **Trac** | **Jira** | _please add other bugtrackers_ |
|:-----------|:---------|:---------|:-------------------------------|
| Task       | Task     | Task     |                                |
| Issue      | (Custom) | (Custom) |                                |
| Defect     | Defect   | Bug      |                                |
| Enhancement | Enhancement | New feature, Improvement |                                |

## baetle:priority ##

baetle:Priority has the subclasses Critical, Major, Blocker, Trivial and Minor

| **Baetle** | **Trac** | **Jira** | _please add other bugtrackers_ |
|:-----------|:---------|:---------|:-------------------------------|
| Priority   | (Custom) |          |                                |
| Critical   | critical | Critical |                                |
| Major      | major    | Major    |                                |
| Trivial    | trivial  | Trivial  |                                |
| Blocker    | blocker  | Blocker  |                                |
| Minor      | minor    | Minor    |                                |

## wf:state ##

| **Baetle** | **Trac** | **Jira** | _please add other bugtrackers_ |
|:-----------|:---------|:---------|:-------------------------------|
| Open       | -        | Open     |                                |
|  New       | New      | -        |                                |
|  Unconfirmed | -        | -        |                                |
|  Started   | Assigned | In progress |                                |
|  Reopened  | Reopened | Reopened |                                |
| Resolved   |          |          |                                |
|  NotReproducible | -        | resolved:cannot reproduce |                                |
|  WorksForMe | closed:worksforme | -        |                                |
|  WontFix   | closed:wontfix | resolved:won't fix |                                |
|  Incomplete | closed:Invalid | resolved:Incomplete |                                |
|  Fixed     | closed:Fixed | resolved:Fixed |                                |
|  Later     | -        | -        |                                |
|  Remind    | -        | -        |                                |
|  Duplicate | closed:duplicate | resolved:duplicate |                                |
| Verified   |          |          |                                |
| Closed     | ?        | Closed:(Fixed,won't fix, duplicate, incomplete, cannot reproduce)? |                                |

Is there too much complexity here? Some states should probably be merged? See the mailing list..



## Other baetle:Issue properties ##

| **Baetle** | **Trac** | **Jira** | _please add other bugtrackers_ |
|:-----------|:---------|:---------|:-------------------------------|
| title/summary (depends which version : schema or .n3 spec) | Summary  | Summary  |                                |
| created    | Created  | Created  |                                |
| updated    | last modified | updated  |                                |
| due\_date  | (milestone due) | due date |                                |
| category   | ?        | ?        |                                |
| comment    | Comment  | Comment  |                                |
| description | (Full) Description | Description |                                |
| reporter   | Reported by | Reporter |                                |
| qa\_contact | -        | -        |                                |
| interested | Cc       | Watchers |                                |
| assigned\_to | Assigned to / Owner | Assignee |                                |
| about      | Component | Component |                                |
| duplicate  | -        | -        |                                |
| causes     | -        | -        |                                |
| depends\_on | -        | -        |                                |
| subtask    | -        | -        |                                |
| target\_milestone | milestone | -        |                                |
| blocks     | ?        | ?        |                                |
| votes      | -        | votes    |                                |
| environment | -        | environment |                                |
| project    | (implicit) | project  |                                |
| resolved\_with | ?        | ?        |                                |
| version    | Version  | Fix version/s |                                |
| sioc:attachment | Attachment | Attachment |                                |

Should modified and updated be one field? See the mailing list..
Should status be removed (left to wf:state) ? See the mailing list..

## Unmapped bugtracker properties ##

| Trac | Jira | _please add other bugtrackers_ |
|:-----|:-----|:-------------------------------|
| Ticket # | Issue key |                                |
| Severity (optional) | -    |                                |
| -    | Custom fields |                                |
| -    | Fix version's |                                |

We should probably don't care about Trac's severity or Jira's custom fields.


## TODO ##
  * Add other bugtrackers
  * Someone should verify this =)