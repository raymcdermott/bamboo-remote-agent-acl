package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.agent.BuildAgent;
import com.atlassian.bamboo.v2.build.agent.BuildAgentRequirementFilter;
import com.atlassian.bamboo.v2.build.agent.LocalBuildAgent;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementSet;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.user.User;

import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 * RemoteAgentAccessControlFilter provides ACLs for Remote Agent access
 * <p/>
 * By default this method is provided with a list of agents that match its specified requirements.
 * These requirements may overlap with the capabilities of many agents so that bamboo can load balance and run as
 * many builds on as many agents as possible. Whilst this has some advantages, it also means that there is the
 * possibility of offering a production agent for a development (or other).
 * <p/>
 * With this solution Bamboo admins define two system wide properties:
 * <p/>
 * System Property: AGENT_NAME_MATCH
 * <p/>
 * The value is a matched as comma delimited set of regular expressions.
 * <p/>
 * Only the agents matching this property are evaluated for correct permissions
 * <p/>
 * System Property: AGENT_ALLOWED_GROUPS
 * <p/>
 * The value is a matched as comma delimited set of regular expressions.
 * <p/>
 * The expanded groups are matched and checked with the current user
 */
public class RemoteAgentAccessControlFilter implements BuildAgentRequirementFilter {

    public static final String AGENT_NAME_MATCH = "AGENT_NAME_MATCH";
    public static final String AGENT_ALLOWED_GROUPS = "AGENT_ALLOWED_GROUPS";

    private boolean AGENT_ALLOWED_GROUPS_isLogged = false;
    private boolean AGENT_NAME_MATCH_isLogged = false;

    private final PlanKeyFinder planKeyFinder;

    private BuildLogger buildLogger;
    private BuildLoggerManager buildLoggerManager;

    private BambooUserManager bambooUserManager;

    public RemoteAgentAccessControlFilter() {
        planKeyFinder = new PlanKeyHelper();
    }

    // Enable mocking of the PlanKeyFinder
    public RemoteAgentAccessControlFilter(final PlanKeyFinder planKeyFinder) {
        this.planKeyFinder = planKeyFinder;
    }

    public Collection<BuildAgent> filter(final BuildContext buildContext, final Collection<BuildAgent> buildAgents,
                                         final RequirementSet requirementSet) {

        PlanKey planKey = planKeyFinder.getPlanKey(buildContext);

        if (planKey == null)
            throw new RuntimeException("We are not able to find the plan key - Bamboo API failure");

        buildLogger = buildLoggerManager.getBuildLogger(planKey);

        // If all offered build agents are local agents, we can let them pass
        final Collection<BuildAgent> localAgents = filterOnLocalAgents(buildAgents);
        if (localAgents.size() == buildAgents.size())
            return buildAgents;

        // We only need to perform ACL check on remote agents
        Collection<BuildAgent> remoteAgents = filterOnRemoteAgents(buildAgents);

        // Obtain the list of permitted Agent names configured by the Bamboo administrators
        Collection<BuildAgent> validatedAgents = new ArrayList<BuildAgent>();

        try {
            validatedAgents = filterOnMatchingAgents(remoteAgents);
        } catch (PatternSyntaxException pse) {
            cancelTheBuild("The regular expression used to define matching agents cannot be parsed " + AGENT_NAME_MATCH);
        }

        // Stop here if there are no agents in the restricted group
        if (validatedAgents.isEmpty())
            return buildAgents;

        // We ensure that remote agents are only deployed manually,
        // since Bamboo cannot provide user data for automatically triggered builds
        if (buildContext.getTriggerReason() instanceof ManualBuildTriggerReason) {
            final User user = bambooUserManager
                    .getUser(((ManualBuildTriggerReason) buildContext.getTriggerReason()).getUserName());

            if (user == null)
                cancelTheBuild("Cannot determine the user ID");

            Collection<BuildAgent> allowedAgents = new ArrayList<BuildAgent>();
            try {
                allowedAgents = findAgentsAllowedForThisUser(bambooUserManager.getGroupNamesAsList(user), validatedAgents);
            } catch (PatternSyntaxException pse) {
                cancelTheBuild("The regular expression used to define allowed groups cannot be parsed " + AGENT_ALLOWED_GROUPS);
            }

            // Cancel the build if there are no authorized remote agents and no local agents.
            // Otherwise return local agents
            if (allowedAgents.isEmpty())
                if (localAgents.isEmpty())
                    cancelTheBuild("There are no authorized agents for " + user.getName());
                else
                    return localAgents;

            // The list of allowed agents
            return allowedAgents;
        } else {
            if (localAgents.isEmpty())
                cancelTheBuild("Bamboo does not support user checking for dependent builds so this is blocked");
            else
                return localAgents;
        }

        // This should never be executed since local agents or an exception will already have been returned.
        // However, it is always a safe and secure option
        return localAgents;
    }

    private Collection<BuildAgent> filterOnRemoteAgents(final Collection<BuildAgent> buildAgents) {
        final Collection<BuildAgent> remoteAgents = new ArrayList<BuildAgent>();

        for (BuildAgent buildAgent : buildAgents)
            if (!(buildAgent instanceof LocalBuildAgent))
                remoteAgents.add(buildAgent);

        return remoteAgents;
    }

    private Collection<BuildAgent> filterOnLocalAgents(final Collection<BuildAgent> buildAgents) {
        final Collection<BuildAgent> localAgents = new ArrayList<BuildAgent>();

        for (BuildAgent buildAgent : buildAgents)
            if (buildAgent instanceof LocalBuildAgent)
                localAgents.add(buildAgent);

        return localAgents;
    }

    private void cancelTheBuild(final String reason) {
        buildLogger.addErrorLogEntry("Cancelling the build: " + reason);
        throw new IllegalStateException(reason);
    }

    // User groups are separated by ',' and can include regular expressions
    private Collection<BuildAgent> findAgentsAllowedForThisUser(final List<String> userGroups,
                                                                final Collection<BuildAgent> agents) throws PatternSyntaxException {
        final Set<BuildAgent> allowedAgents = new HashSet<BuildAgent>();

        final String agentAllowedGroups = System.getProperty(AGENT_ALLOWED_GROUPS);

        if (!AGENT_ALLOWED_GROUPS_isLogged) {
            buildLogger.addBuildLogEntry("System property " + AGENT_ALLOWED_GROUPS + " is set to " + agentAllowedGroups);
            AGENT_ALLOWED_GROUPS_isLogged = true;
        }

        if (agentAllowedGroups == null) {
            buildLogger.addErrorLogEntry("System property " + AGENT_ALLOWED_GROUPS + " is not set");
            return allowedAgents;
        }

        // Obtain the agents matching the specified pattern
        for (BuildAgent agent : agents) {
            final StringTokenizer agentGroupTokens = new StringTokenizer(agentAllowedGroups, ",");

            while (agentGroupTokens.hasMoreElements()) {
                final String agentGroup = agentGroupTokens.nextToken().trim();
                for (String userGroup : userGroups)
                    if (userGroup.matches(agentGroup))
                        allowedAgents.add(agent);
            }
        }

        return allowedAgents;
    }

    // Agent names are separated by ',' and can include regular expressions
    private Collection<BuildAgent> filterOnMatchingAgents(final Collection<BuildAgent> agents) throws PatternSyntaxException {

        final Collection<BuildAgent> matchingAgents = new ArrayList<BuildAgent>();

        final String agentMatchPatterns = System.getProperty(AGENT_NAME_MATCH);

        if (!AGENT_NAME_MATCH_isLogged) {
            buildLogger.addBuildLogEntry("System property " + AGENT_NAME_MATCH + " is set to " + agentMatchPatterns);
            AGENT_NAME_MATCH_isLogged = true;
        }

        if (agentMatchPatterns == null) {
            buildLogger.addErrorLogEntry("System property " + AGENT_NAME_MATCH + " is not set");
            return matchingAgents;
        }

        // Obtain the agents matching the specified pattern
        final StringTokenizer agentMatchTokens = new StringTokenizer(agentMatchPatterns, ",");

        while (agentMatchTokens.hasMoreElements()) {
            final String pattern = agentMatchTokens.nextToken();

            for (BuildAgent agent : agents) {
                final String name = agent.getName();
                buildLogger.addBuildLogEntry("Agent name is set to " + name);
                if (name == null) buildLogger
                        .addErrorLogEntry("Null agent name detected - is that right? Agent definition = " + agent);
                else if (name.matches(pattern))
                    matchingAgents.add(agent);
            }
        }

        return matchingAgents;
    }

    // Injected by Bamboo
    public void setBambooUserManager(BambooUserManager bambooUserManager) {
        this.bambooUserManager = bambooUserManager;
    }

    public void setBuildLoggerManager(BuildLoggerManager buildLoggerManager) {
        this.buildLoggerManager = buildLoggerManager;
    }
}
