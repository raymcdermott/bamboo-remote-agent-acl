##Problem
Atlassian does not provide authorisation control for access to build agents. This means that a user with build rights on a plan can deploy any project to any environment.

See the bug opened February *2009* https://jira.atlassian.com/browse/BAM-3491

Atlassian are the owners of this problem but we need an interim solution.

##Goal
The solution should

provide the minimum features required to protect unauthorised deployments from production.
be simple to manage with minimum configuration

Solution
This plugin imposes restrictions on the use of remote agents. The admin can assign groups to one or more agents.

##Installation
Obtain the latest JAR file from Github (latest =    remote-agent-acl-1.1-SNAPSHOT.jar)

[ FYI group: mondraymond.plugins.bamboo, artefact: remote-agent-acl ]

##Configuration
The Bamboo system admin must define two server system properties as shown in the Atlassian docs

#AGENT_NAME_MATCH

Only the agents matching this property are evaluated for correct permissions. Examples

dev-linux-001, uat-linux-002, prod-linux-003 (standard agent names)
prod-.\* (agent names expanded with regular expression)
.\* (regular expression matching any agent)

We have something like 'lx.\*p0.\*,sunesbp0.\*,acmehof.\*p0.\*' to limit access to production agents

The property is matched as comma delimited expressions. Each token supports regular expression matching the agents.

#AGENT_ALLOWED_GROUPS

The given groups are matched and checked with the current user

developers, operations (standard group names)
admin.\* (group name expanded with regular expression)
.\* (regular expression matching any group)

For example you might have 'acme-operations' to limit production deployment to operations

The property is matched as comma delimited expressions. Each token supports regular expression matching the groups.

##Building
You can follow Atalassian instructions and build with their atlas tools.

The code has a decent number of unit tests but pull requests / issues welcomed.