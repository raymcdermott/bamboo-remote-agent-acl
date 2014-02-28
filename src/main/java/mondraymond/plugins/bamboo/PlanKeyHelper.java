package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.v2.build.BuildContext;

/**
 *
 * Exists to allow mocking of the obtaining of the plan key
 *
 * User: ray
 * Date: 16/03/11
 * Time: 19:26
 */
public class PlanKeyHelper implements PlanKeyFinder {

    public PlanKey getPlanKey(BuildContext buildContext) {
        return PlanKeys.getPlanKey(buildContext.getPlanKey());
    }

}
