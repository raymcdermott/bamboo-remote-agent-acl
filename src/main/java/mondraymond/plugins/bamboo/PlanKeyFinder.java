package mondraymond.plugins.bamboo;

import com.atlassian.bamboo.plan.PlanKey;
import com.atlassian.bamboo.v2.build.BuildContext;

/**
 * User: ray
 * Date: 16/03/11
 * Time: 21:01
 */
public interface PlanKeyFinder {
    public PlanKey getPlanKey(BuildContext buildContext);
}
