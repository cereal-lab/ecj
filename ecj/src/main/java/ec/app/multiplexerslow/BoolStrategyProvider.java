package ec.app.multiplexerslow;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.transform.Strategy;
import ec.gp.transform.StrategyProvider;
import ec.util.Parameter;

public class BoolStrategyProvider extends StrategyProvider {

    private static final long serialVersionUID = 1L;

    private Strategy reduce;
    private Strategy oneShotRewrite;
    private Strategy oneShotRewriteAny;
    private Strategy tranPropag;
    private Strategy topDownRewrite;
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        //Experiment: Strategy.Choice(prob, st1, st2), st1 = reduce, st2 = expand -
        //null hyp: change in prob changes performance. prob = 0..0.1..1
        reduce = Strategy.Fixpoint(
            Strategy.FirstMatch(
                rules.r("x and x -> x"),
                rules.r("x or x -> x"),
                rules.r("not not x -> x"),
                rules.r("if x y y -> y"),
                rules.r("if (x and not x) y z"),
                rules.r("(x and not x) or y"),
                rules.r("y or (x and not x)"),
                rules.r("x and not x and y -> x and not x"),
                rules.r("y and x and not x -> x and not x"),
                rules.r("not (x and not x) -> x or not x"),
                rules.r("if (x or not x) y z"),
                rules.r("x or not x or y -> x or not x"),
                rules.r("y or x or not x -> x or not x"),
                rules.r("(x or not x) and y -> y"),
                rules.r("y and (x or not x) -> y"),
                rules.r("not (x or not x) -> x and not x"),

                rules.r("not x and x -> x and not x"),
                rules.r("not x or x -> x or not x")
            )
        );
        oneShotRewrite = Strategy.AnyMatch(            
            //distributivity
            rules.r("(y or z) and x -> (y and x) or (z and x)"),
            //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
            rules.r("x and (y or z) -> (x and y) or (x and z)"),  
            rules.r("(y and z) or x -> (y or x) and (z or x)"),
            rules.r("x or (y and z) -> (x or y) and (x or z)"),
            //associativity
            rules.r("x or (y or z) -> (x or y) or z"),
            rules.r("x and (y and z) -> (x and y) and z"),
            rules.r("(x or y) or z -> x or (y or z)"),
            rules.r("(x and y) and z -> x and (y and z)"),  
            //absorption
            rules.r("x or (x and y) -> x"), 
            rules.r("x and (x or y) -> x"),
            //DeMorgan 
            rules.r("not (x or y) -> not x and not y"),
            rules.r("not (x and y) -> not x or not y"),
            //DeMorgan rev
            rules.r("not x and not y -> not (x or y)"),
            rules.r("not x or not y -> not (x and y)")
        );
        oneShotRewriteAny = Strategy.Any(            
            //distributivity
            Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
            //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
            Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
            Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)")),
            Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)")),
            //associativity
            Strategy.AnyMatch(rules.r("x or (y or z) -> (x or y) or z")),
            Strategy.AnyMatch(rules.r("x and (y and z) -> (x and y) and z")),
            Strategy.AnyMatch(rules.r("(x or y) or z -> x or (y or z)")),
            Strategy.AnyMatch(rules.r("(x and y) and z -> x and (y and z)")),  
            //absorption
            Strategy.AnyMatch(rules.r("x or (x and y) -> x")), 
            Strategy.AnyMatch(rules.r("x and (x or y) -> x")),
            //DeMorgan 
            Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
            Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
            //DeMorgan rev
            Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
            Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)"))
        );
        tranPropag = Strategy.NTimesMax(5, 
            Strategy.Any(    
                //grouped all and ops        
                Strategy.Any(
                    Strategy.AnyMatch(rules.r("x and y -> not (not x or not y)")),
                    Strategy.AnyMatch(rules.r("not (not x or not y) -> x and y")),
                    Strategy.AnyMatch(rules.r("x and y -> if x y x")),
                    Strategy.AnyMatch(rules.r("if x y x -> x and y"))
                ),
                //grouped or ops
                Strategy.Any(
                    Strategy.AnyMatch(rules.r("x or y -> not (not x and not y)")),
                    Strategy.AnyMatch(rules.r("not (not x and not y) -> x or y")),
                    Strategy.AnyMatch(rules.r("x or y -> if x x y")),
                    Strategy.AnyMatch(rules.r("if x x y -> x or y"))
                ),

                //grouped if ops
                Strategy.Any(
                    Strategy.AnyMatch(rules.r("if x y z -> (x and y) or (not x and z)")), 
                    Strategy.AnyMatch(rules.r("if x y z -> if (not x) z y")), 
                    Strategy.AnyMatch(rules.r("if (not x) z y -> if x y z")), 
                    Strategy.AnyMatch(rules.r("not (if x y z) -> if x (not y) (not z)")),
                    Strategy.AnyMatch(rules.r("if x (not y) (not z) -> not (if x y z)"))
                ),
                
                //grouped reduction ops
                Strategy.Any(
                    Strategy.AnyMatch(rules.r("not not x -> x")),
                    Strategy.AnyMatch(rules.r("x and x -> x")),
                    Strategy.AnyMatch(rules.r("x or x -> x")),
                    Strategy.AnyMatch(rules.r("if x y y -> y")),
                    Strategy.AnyMatch(rules.r("if (x and not x) y z")),
                    Strategy.AnyMatch(rules.r("if (not x and x) y z"))
                ),
                
                //movement ops                 
                Strategy.Any(
                    //DeMorgan
                    Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
                    Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
                    //DeMorgan rev
                    Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
                    Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)")),

                    Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
                    //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
                    Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
                    Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)")),
                    Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)"))                
                )
                // Strategy.AnyMatch(rules.r("x -> not not x"))
            ));        
        // topDownRewrite = 
        //     Strategy.TopDownAny(0.5, //check for .1, .2, .3, .4, .5
        //         rules.r("(y or z) and x -> (y and x) or (z and x)"), //apply rule in both ways 
        //         rules.r("x and (y or z) -> (x and y) or (x and z)"),  
        //         rules.r("(y and z) or x -> (y or x) and (z or x)"),
        //         rules.r("x or (y and z) -> (x or y) and (x or z)")
        //     );
    }

    //Experiment: reduce or expand depending on stats of ast (with goal to balance stats - # of ast nonterminals per type)
    // rules map to Map<Class<?>, Integer>
    // or simpler - increases # of node, decreases # of node, no change
    //null hyp: this concrete strategy has no effect  - statistics of ast can be used to improve performance
    @Override
    public Strategy getStrategy(GPNode i) {
        switch (this.strategyName) {
            case "reduce": return reduce;
            case "oneRewrite": return oneShotRewrite;
            case "oneRewriteAny": return oneShotRewriteAny;
            case "aggressiveTopDown": return topDownRewrite;
            case "tranPropag": return tranPropag;
            default: return null;
        }
    }
    
}
