package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.build.BuildLoggerManager;
import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.user.BambooUserManager;
import com.atlassian.bamboo.v2.build.BuildContext;
import com.atlassian.bamboo.v2.build.agent.BuildAgent;
import com.atlassian.bamboo.v2.build.agent.LocalBuildAgent;
import com.atlassian.bamboo.v2.build.agent.capability.RequirementSet;
import com.atlassian.bamboo.v2.build.trigger.ManualBuildTriggerReason;
import com.atlassian.user.User;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: ray
 * Date: 16/03/11
 * Time: 18:58
 */
public class TestRemoteAgentAccessControlFilter {

    @Test
    public void testStandardBambooEnvironmentWithLocalBuildsOnly() {
        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = mock(LocalBuildAgent.class);

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testMatchingAgentsSimplestCase() {
        String agentName = "remote-agent";
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, agentName);
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "Administrators");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testMatchingAgentsRegexAgentNameMatch() {
        String agentName = "remote-agent";
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "remote.*");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "Administrators");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testMatchingAgentsRegexAgentNameAndGroupNameMatch() {
        String agentName = "remote-agent";
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "remote.*");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "Admini.*");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testMultipleRegularExpressions() {
        String agentName = "remote-agent";
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "remote.*");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "Admini.*, Oper.*, something-*");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators, Operators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testOneLocalAndNotPermittedRemoteAgents() {
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "none");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, ".*");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        Collection<BuildAgent> buildAgents = new ArrayList<BuildAgent>();

        BuildAgent agent = new MockBuildAgent("local-agent").newBuildAgent();
        buildAgents.add(agent);
        buildAgents.add(mock(LocalBuildAgent.class));

        Assert.assertTrue(buildAgents.size() == 2);
        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), buildAgents, mock(RequirementSet.class)).size() == 2);
    }

    @Test
    public void testForRemoteBuildButZeroConfiguration() {
        String agentName = "remote-agent";

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test
    public void testNonMatchingNotPermittedAgents() {
        String agentName = "remote-agent";
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "none");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "Admini.*");

        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");

        BuildAgent agent = new MockBuildAgent(agentName).newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testCrazyRegularExpressionsOnAgentName() {
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "*");
        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");
        BuildAgent agent = new MockBuildAgent("remote-agent").newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    @Test(expected = IllegalStateException.class)
    public void testCrazyRegularExpressionsOnGroupName() {
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_NAME_MATCH, "remote-agent");
        System.setProperty(RemoteAgentAccessControlFilter.AGENT_ALLOWED_GROUPS, "*");
        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = getRemoteAgentAccessControlFilter("ray", "Administrators");
        BuildAgent agent = new MockBuildAgent("remote-agent").newBuildAgent();

        Assert.assertTrue(remoteAgentAccessControlFilter.filter(getBuildContext(), Arrays.asList(agent), mock(RequirementSet.class)).size() == 1);
    }

    private RemoteAgentAccessControlFilter getRemoteAgentAccessControlFilter(String user, String groups) {
        RemoteAgentAccessControlFilter remoteAgentAccessControlFilter = new RemoteAgentAccessControlFilter(new MockPlanKeyHelper());

        remoteAgentAccessControlFilter.setBambooUserManager(getBambooUserManager(user, groups));
        remoteAgentAccessControlFilter.setBuildLoggerManager(getBuildLoggerManager());

        return remoteAgentAccessControlFilter;
    }

    private BuildContext getBuildContext() {
        BuildContext buildContext = mock(BuildContext.class);
        ManualBuildTriggerReason manualBuildTriggerReason = mock(ManualBuildTriggerReason.class);
        when(buildContext.getTriggerReason()).thenReturn(manualBuildTriggerReason);

        return buildContext;
    }


    private BuildLoggerManager getBuildLoggerManager() {
        BuildLoggerManager buildLoggerManager = mock(BuildLoggerManager.class);
        BuildLogger buildLogger = new MockBuildLogger();
        when(buildLoggerManager.getBuildLogger(Mockito.<PlanKey>any())).thenReturn(buildLogger);

        return buildLoggerManager;
    }

    private BambooUserManager getBambooUserManager(String user, String groups) {
        User bambooUser = mock(User.class);
        when(bambooUser.getName()).thenReturn(user);

        BambooUserManager bambooUserManager = mock(BambooUserManager.class);
        when(bambooUserManager.getGroupNamesAsList(Mockito.<User>any())).thenReturn(Arrays.asList(groups));
        when(bambooUserManager.getUser(Mockito.<String>any())).thenReturn(bambooUser);

        return bambooUserManager;
    }
}
