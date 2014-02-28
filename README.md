#Problem
Atlassian does not provide authorisation control for access to build agents. This means that a user with build rights on a plan can deploy any project to any environment.

See [this bug opened February 2009](https://jira.atlassian.com/browse/BAM-3491)

Yes you read that right. As of today, 5 years later, it's still open and I have finally caved and published.

Atlassian are the owners of this problem but we need an, ahem, interim solution.

#Goal
The solution should

- provide the minimum features required to protect unauthorised deployments from production.
- be simple to manage with minimum configuration

Solution
This plugin imposes restrictions on the use of remote agents. The admin can assign groups to one or more agents.

#Installation
Obtain the latest JAR file from Github (latest = remote-agent-acl-1.1-SNAPSHOT.jar)

[ Maven group: mondraymond.plugins.bamboo, artefact: remote-agent-acl ]

##Configuration
The Bamboo system admin must define two server system properties as shown in the [Atlassian docs](https://confluence.atlassian.com/display/BAMBOO/Configuring+system+properties)

##AGENT_NAME_MATCH

Only the agents matching this property are evaluated for correct permissions.

The property is matched as comma delimited expressions.

Each token supports regular expression matching the agents.

###Examples

AGENT_NAME_MATCH='dev-linux-001, uat-linux-002, prod-linux-003' \#standard agent names

AGENT_NAME_MATCH='prod-.\*' \#agent names expanded with regular expression

AGENT_NAME_MATCH='.\*' \#regular expression matching any agent

#AGENT_ALLOWED_GROUPS

The given groups are matched and checked with the current user

The property is matched as comma delimited expressions.

Each token supports regular expression matching the groups.

###Examples

AGENT_ALLOWED_GROUPS='developers, operations' #standard group names

AGENT_ALLOWED_GROUPS='admin.\*' \#group name expanded with regular expression

AGENT_ALLOWED_GROUPS='.\*' \# regular expression matching any group

#Building

You can follow Atlassian's instructions and build with their atlas tools.

The code has a decent number of unit tests but pull requests / issues welcomed.

#Warning

This code bears no warranty of any sort
