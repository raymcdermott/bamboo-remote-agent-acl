package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.buildqueue.PipelineDefinition;
import com.atlassian.bamboo.buildqueue.properties.CapabilityProperties;
import com.atlassian.bamboo.v2.build.agent.BuildAgent;
import com.atlassian.bamboo.v2.build.agent.capability.Capability;
import com.atlassian.bamboo.v2.build.agent.capability.CapabilitySet;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: ray
 * Date: 16/03/11
 * Time: 17:30
 */
public class MockBuildAgent {

    private String name = "dev-agent-001";

    public MockBuildAgent(final String agentName) {
        this.name = agentName;
    }

    // Can get one of these and set the properties as you wish...
    public BuildAgent newBuildAgent() {
        BuildAgent buildAgent = mock(BuildAgent.class);
        when(buildAgent.getName()).thenReturn(getName());

        return buildAgent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
