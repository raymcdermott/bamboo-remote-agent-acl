##Problem
Atlassian does not provide authorisation control for access to build agents. This means that a user with build rights on a plan can deploy any project to any environment.

Atlassian are the owners of this problem but we need to provide an interim solution.

##Goal
The solution should

provide the minimum features required to protect unauthorised deployments from production.
be simple to manage with minimum configuration
Solution
We have written a plugin that imposes restrictions on the use of remote agents. The admin can assign groups to one or more agents.

##Installation
Obtain the latest JAR file from Nexus

group: mondraymond.plugins.bamboo, artefact: remote-agent-acl
direct link https://cde.toyota-europe.com/nexus/content/repositories/TmePrivateSnapshot/mondraymond/plugins/bamboo/remote-agent-acl
Follow the Bamboo documentation for installing a plugin

##Configuration
The Bamboo system admin must define two server system properties as shown in the Atlassian docs

##AGENT_NAME_MATCH

Only the agents matching this property are evaluated for correct permissions. Examples

dev-linux-001, uat-linux-002, prod-linux-003 (standard agent names)
prod-.* (agent names expanded with regular expression)
.* (regular expression matching any agent)
	At TME we will usually have something like 'lx.*p0.*,sunesbp0.*,tmehof.*p0.*' to limit access to production agents
The property is matched as comma delimited expressions. Each token supports regular expression matching the agents.

##AGENT_ALLOWED_GROUPS

The given groups are matched and checked with the current user

developers, operations (standard group names)
admin.* (group name expanded with regular expression)
.* (regular expression matching any group)
	For example you might have 'acme-operations' to limit production deployment to operations
The property is matched as comma delimited expressions. Each token supports regular expression matching the groups.

