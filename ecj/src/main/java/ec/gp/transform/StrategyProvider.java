package ec.gp.transform;

import ec.EvolutionState;
import ec.Prototype;
import ec.gp.GPDefaults;
import ec.gp.GPNode;
import ec.util.Parameter;

public abstract class StrategyProvider implements Prototype {
    public static final String P_RULE_PROVIDER = "rules";
    public static final String P_STRATEGY_PROVIDER = "strategies";
    public static final String P_STRATEGY_NAME = "name";
    protected RuleProvider rules; //this is rule storage
    private static final long serialVersionUID = 1L;
    protected String strategyName;

    abstract public Strategy getStrategy(GPNode i);

    @Override
    public Object clone() {
        return this; //be default no need to clone strategy - it is stateless 
    }

    public void setup(final EvolutionState state, final Parameter base) {
        Parameter def = defaultBase();
        Parameter p = base.push(P_RULE_PROVIDER);
        Parameter d = def.push(P_RULE_PROVIDER);
        //TODO - default parameter for rule provider
        this.rules = (RuleProvider)(state.parameters.getInstanceForParameter(p, d, RuleProvider.class));
        p = base.push(P_STRATEGY_NAME);
        d = def.push(P_STRATEGY_NAME);
        this.strategyName = state.parameters.getStringWithDefault(p, d, "");
        //this.rules.setup(state, ...)

    }

    @Override
    public Parameter defaultBase() {
        return GPDefaults.base().push(P_STRATEGY_PROVIDER);
    }

}
