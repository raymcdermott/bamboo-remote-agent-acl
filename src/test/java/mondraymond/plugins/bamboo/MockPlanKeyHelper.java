package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.v2.build.BuildContext;

/**
 * User: ray
 * Date: 16/03/11
 * Time: 20:58
 */
public class MockPlanKeyHelper implements PlanKeyFinder {

    public PlanKey getPlanKey(BuildContext buildContext) {
        return PlanKeys.getPlanKey("TEST-PLAN");
    }

}
