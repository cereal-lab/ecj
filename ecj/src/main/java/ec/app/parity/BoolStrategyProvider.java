package ec.app.parity;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.transform.Strategy;
import ec.gp.transform.StrategyProvider;
import ec.util.Parameter;

public class BoolStrategyProvider extends StrategyProvider {

    private static final long serialVersionUID = 1L;

    // private Strategy reduce;
    private Strategy oneShotRewrite;
    // private Strategy topDownRewrite;
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        // reduce = Strategy.Fixpoint(
        //     Strategy.FirstMatch(
        //         rules.r("x and x -> x"),
        //         rules.r("x or x -> x"),
        //         rules.r("not not x -> x"),
        //         rules.r("if x y y -> y"),
        //         rules.r("if (x and not x) y z"),
        //         rules.r("(x and not x) or y"),
        //         rules.r("y or (x and not x)"),
        //         rules.r("x and not x and y -> x and not x"),
        //         rules.r("y and x and not x -> x and not x"),
        //         rules.r("not (x and not x) -> x or not x"),
        //         rules.r("if (x or not x) y z"),
        //         rules.r("x or not x or y -> x or not x"),
        //         rules.r("y or x or not x -> x or not x"),
        //         rules.r("(x or not x) and y -> y"),
        //         rules.r("y and (x or not x) -> y"),
        //         rules.r("not (x or not x) -> x and not x"),

        //         rules.r("not x and x -> x and not x"),
        //         rules.r("not x or x -> x or not x")
        //     )
        // );
        oneShotRewrite = Strategy.AnyMatch(            
            //distributivity
            rules.r("(y or z) and x -> (y and x) or (z and x)"),
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
            //NAND
            rules.r("a and b -> (a nand b) nand (a nand b)"),
            rules.r("(a nand b) nand (a nand b) -> a and b"),
            rules.r("a or b -> (a nand a) nand (b nand b)"),
            rules.r("(a nand a) nand (b nand b) -> a or b"),
            rules.r("a nor b -> ((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b))"),
            rules.r("((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b)) -> a nor b"),
            //NOR
            rules.r("a or b -> (a nor b) nor (a nor b)"),
            rules.r("(a nor b) nor (a nor b) -> a or b"),
            rules.r("a and b -> (a nor a) nor (b nor b)"),
            rules.r("(a nor a) nor (b nor b) -> a and b"),
            rules.r("a nand b -> ((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b))"),
            rules.r("((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b)) -> a nand b"),
            //Other Nand Nor 
            rules.r("((a nor a) nor (a nor a)) -> a"),
            rules.r("((a nand a) nand (a nand a)) -> a"),
            rules.r("a nand a -> a nor a"),
            rules.r("a nor a -> a nand a")
        );
        // topDownRewrite = 
        //     Strategy.TopDownAny(0.5, //check for .1, .2, .3, .4, .5
        //         rules.r("(y or z) and x -> (y and x) or (z and x)"), //apply rule in both ways 
        //         rules.r("x and (y or z) -> (x and y) or (x and z)"),  
        //         rules.r("(y and z) or x -> (y or x) and (z or x)"),
        //         rules.r("x or (y and z) -> (x or y) and (x or z)")
        //     );  
    }

    @Override
    public Strategy getStrategy(GPNode i) {
        switch (this.strategyName) {
            // case "reduce": return reduce;
            case "oneRewrite": return oneShotRewrite;
            // case "aggressiveTopDown": return topDownRewrite;
            default: return null;
        }
    }
    
}
