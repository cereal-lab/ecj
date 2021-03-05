package ec.domain.bool;

import java.util.Set;

import ec.EvolutionState;
import ec.gp.GPNode;
import ec.gp.transform.Strategy;
import ec.gp.transform.StrategyProvider;
import ec.util.Parameter;

public class BoolStrategyProvider extends StrategyProvider {

    private static final long serialVersionUID = 1L;

    private Strategy reduce;
    private Strategy reduceOnDepth;
    private Strategy axioms;
    private Strategy tranPropag;
    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);

        //Experiment: Strategy.Choice(prob, st1, st2), st1 = reduce, st2 = expand -
        //null hyp: change in prob changes performance. prob = 0..0.1..1
        // reduce = Strategy.NTimesMax(2, Strategy.All(
        //     Strategy.FirstMatch(rules.r("x and x -> x")),
        //     Strategy.FirstMatch(rules.r("x or x -> x")),
        //     Strategy.FirstMatch(rules.r("not not x -> x")),    
        //     //reduction to const
        //     // Strategy.All(
        //     Strategy.FirstMatch(rules.r("not x and x -> 0")),
        //     Strategy.FirstMatch(rules.r("x and not x -> 0")),
        //     Strategy.FirstMatch(rules.r("not x or x -> 1")),
        //     Strategy.FirstMatch(rules.r("x or not x -> 1")),
        //     // ),
        //     // Strategy.AnyMatch(rules.r("if x y y -> y")),
        //     // Strategy.AnyMatch(rules.r("if 0 y z -> z")),
        //     // Strategy.AnyMatch(rules.r("if 1 y z -> y")),

        //     // Strategy.All(
        //     Strategy.FirstMatch(rules.r("0 and x -> 0")),
        //     Strategy.FirstMatch(rules.r("x and 0 -> 0")),
        //     Strategy.FirstMatch(rules.r("1 and x -> x")),
        //     Strategy.FirstMatch(rules.r("x and 1 -> x")),
        //     Strategy.FirstMatch(rules.r("0 or x -> x")),
        //     Strategy.FirstMatch(rules.r("x or 0 -> x")),
        //     Strategy.FirstMatch(rules.r("1 or x -> 1")),
        //     Strategy.FirstMatch(rules.r("x or 1 -> 1")),
        //     Strategy.FirstMatch(rules.r("not 0 -> 1")),
        //     Strategy.FirstMatch(rules.r("not 1 -> 0"))
        //     // )
        //     // Strategy.AnyMatch(rules.r("((a nor a) nor (a nor a)) -> a")),
        //     // Strategy.AnyMatch(rules.r("((a nand a) nand (a nand a)) -> a"))                  
        // ));

        reduce = Strategy.Fixpoint(
            Strategy.FirstMatch(rules.r("x and x -> x")),
            Strategy.FirstMatch(rules.r("x or x -> x")),
            Strategy.FirstMatch(rules.r("not not x -> x")),    
            //reduction to const
            // Strategy.All(
            
            Strategy.FirstMatch(rules.r("not x and x -> 0")),
            // Strategy.FirstMatch(rules.r("x and not x -> 0")),
            Strategy.FirstMatch(rules.r("not x or x -> 1")),
            // Strategy.FirstMatch(rules.r("x or not x -> 1")),
            
            // ),
            // Strategy.AnyMatch(rules.r("if x y y -> y")),
            // Strategy.AnyMatch(rules.r("if 0 y z -> z")),
            // Strategy.AnyMatch(rules.r("if 1 y z -> y")),

            // Strategy.All(
            Strategy.FirstMatch(rules.r("0 and x -> 0")),
            // Strategy.FirstMatch(rules.r("x and 0 -> 0")),
            Strategy.FirstMatch(rules.r("1 and x -> x")),
            // Strategy.FirstMatch(rules.r("x and 1 -> x")),
            Strategy.FirstMatch(rules.r("0 or x -> x")),
            // Strategy.FirstMatch(rules.r("x or 0 -> x")),
            Strategy.FirstMatch(rules.r("1 or x -> 1")),
            // Strategy.FirstMatch(rules.r("x or 1 -> 1")),
            Strategy.FirstMatch(rules.r("not 0 -> 1")),
            Strategy.FirstMatch(rules.r("not 1 -> 0")),
            Strategy.FirstMatch(rules.r("x or (x and y) -> x")),
            Strategy.FirstMatch(rules.r("x and (x or y) -> x"))
            // )
            // Strategy.AnyMatch(rules.r("((a nor a) nor (a nor a)) -> a")),
            // Strategy.AnyMatch(rules.r("((a nand a) nand (a nand a)) -> a"))                  
        );        

        reduce.set(this.getCommutativeFuncs());

        reduceOnDepth = Strategy.On(Strategy.Depth(14, reduce));

        reduceOnDepth.set(this.getCommutativeFuncs());

        axioms = Strategy.NTimesMax(5, Strategy.Any(            
            //distributivity
            Strategy.Any( 
                Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
                // Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
                Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)"))
                // Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)"))
            ),
            //commutativity 
            // Strategy.Any( 
            //     Strategy.AnyMatch(rules.r("x and y -> y and x")),
            //     Strategy.AnyMatch(rules.r("x or y -> y or x"))
            // ),
            //associativity
            Strategy.Any( 
                Strategy.AnyMatch(rules.r("x or (y or z) -> (x or y) or z")),
                Strategy.AnyMatch(rules.r("x and (y and z) -> (x and y) and z"))
                // Strategy.AnyMatch(rules.r("(x or y) or z -> x or (y or z)")),
                // Strategy.AnyMatch(rules.r("(x and y) and z -> x and (y and z)"))
            ),  
            //absorption
            Strategy.Any( 
                Strategy.AnyMatch(rules.r("x or (x and y) -> x")), 
                Strategy.AnyMatch(rules.r("x and (x or y) -> x"))
            ),
            // Strategy.FirstMatch(rules.r("x and x -> x")),
            // Strategy.FirstMatch(rules.r("x or x -> x")),
            // Strategy.FirstMatch(rules.r("not not x -> x")),    
            //reduction to const
            // Strategy.All(
            //complements
            Strategy.Any(
                Strategy.AnyMatch(rules.r("not x and x -> 0")),
                // Strategy.AnyMatch(rules.r("x and not x -> 0")),
                Strategy.AnyMatch(rules.r("not x or x -> 1"))
                // Strategy.AnyMatch(rules.r("x or not x -> 1"))
            ),
            // ),
            // Strategy.AnyMatch(rules.r("if x y y -> y")),
            // Strategy.AnyMatch(rules.r("if 0 y z -> z")),
            // Strategy.AnyMatch(rules.r("if 1 y z -> y")),

            // Strategy.All(
            //identity
            Strategy.Any(
                Strategy.AnyMatch(rules.r("0 and x -> 0")),
                // Strategy.AnyMatch(rules.r("x and 0 -> 0")),
                Strategy.AnyMatch(rules.r("1 and x -> x")),
                // Strategy.AnyMatch(rules.r("x and 1 -> x")),
                Strategy.AnyMatch(rules.r("0 or x -> x")),
                // Strategy.AnyMatch(rules.r("x or 0 -> x")),
                Strategy.AnyMatch(rules.r("1 or x -> 1")),
                // Strategy.AnyMatch(rules.r("x or 1 -> 1")),
                Strategy.AnyMatch(rules.r("not 0 -> 1")),
                Strategy.AnyMatch(rules.r("not 1 -> 0"))
            ),
            //DeMorgan 
            Strategy.Any( 
                Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
                Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
                //DeMorgan rev
                Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
                Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)"))
            )
        ));

        axioms.set(this.getCommutativeFuncs());

        // Strategy.Fixpoint(
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
        // oneShotRewrite = Strategy.AnyMatch(            
        //     //distributivity
        //     rules.r("(y or z) and x -> (y and x) or (z and x)"),
        //     //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
        //     rules.r("x and (y or z) -> (x and y) or (x and z)"),  
        //     rules.r("(y and z) or x -> (y or x) and (z or x)"),
        //     rules.r("x or (y and z) -> (x or y) and (x or z)"),
        //     //associativity
        //     rules.r("x or (y or z) -> (x or y) or z"),
        //     rules.r("x and (y and z) -> (x and y) and z"),
        //     rules.r("(x or y) or z -> x or (y or z)"),
        //     rules.r("(x and y) and z -> x and (y and z)"),  
        //     //absorption
        //     rules.r("x or (x and y) -> x"), 
        //     rules.r("x and (x or y) -> x"),
        //     //DeMorgan 
        //     rules.r("not (x or y) -> not x and not y"),
        //     rules.r("not (x and y) -> not x or not y"),
        //     //DeMorgan rev
        //     rules.r("not x and not y -> not (x or y)"),
        //     rules.r("not x or not y -> not (x and y)")
        // );
        // oneShotRewriteAny = Strategy.Any(            
        //     //distributivity
        //     Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
        //     //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
        //     Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
        //     Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)")),
        //     Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)")),
        //     //associativity
        //     Strategy.AnyMatch(rules.r("x or (y or z) -> (x or y) or z")),
        //     Strategy.AnyMatch(rules.r("x and (y and z) -> (x and y) and z")),
        //     Strategy.AnyMatch(rules.r("(x or y) or z -> x or (y or z)")),
        //     Strategy.AnyMatch(rules.r("(x and y) and z -> x and (y and z)")),  
        //     //absorption
        //     Strategy.AnyMatch(rules.r("x or (x and y) -> x")), 
        //     Strategy.AnyMatch(rules.r("x and (x or y) -> x")),
        //     //DeMorgan 
        //     Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
        //     Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
        //     //DeMorgan rev
        //     Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
        //     Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)"))
        // );
        tranPropag = Strategy.On(
            Strategy.Depth(10, reduce),
            Strategy.MissingERC(BusBit::testPresense, "n", 
                Strategy.Any(
                    Strategy.AnyMatch(rules.r("x -> (n or not n) and x")),
                    Strategy.AnyMatch(rules.r("x -> (n and not n) or x")))
            ),            
            Strategy.Default(
                Strategy.NTimesMax(2,
                    Strategy.Any(
                        //and
                        Strategy.AnyMatch(rules.r("x and y -> not (not x or not y)")),
                        Strategy.AnyMatch(rules.r("not (not x or not y) -> x and y")),
                        //or
                        Strategy.AnyMatch(rules.r("x or y -> not (not x and not y)")),
                        Strategy.AnyMatch(rules.r("not (not x and not y) -> x or y")),
                        //movement
                        //DeMorgan
                        Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
                        Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
                        //DeMorgan rev
                        Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
                        Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)")),

                        Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
                        // Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
                        Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)")),
                        // Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)")),
                        //assoc                         
                        Strategy.AnyMatch(rules.r("x or (y or z) -> (x or y) or z")),
                        Strategy.AnyMatch(rules.r("x and (y and z) -> (x and y) and z"))
                        // Strategy.AnyMatch(rules.r("(x or y) or z -> x or (y or z)")),
                        // Strategy.AnyMatch(rules.r("(x and y) and z -> x and (y and z)"))
                    )
                )
            )
        );

        tranPropag.set(this.getCommutativeFuncs());

        // Strategy.NTimesMax(5, 
        //     Strategy.Any(    

        //         //reduction
        //         // Strategy.Any(
        //         //     Strategy.AnyMatch(rules.r("x and x -> x")),
        //         //     Strategy.AnyMatch(rules.r("x or x -> x")),
        //         //     Strategy.AnyMatch(rules.r("not not x -> x")),    
        //         //     //reduction to const
        //         //     Strategy.All(
        //         //         Strategy.AnyMatch(rules.r("not x and x -> 0")),
        //         //         Strategy.AnyMatch(rules.r("x and not x -> 0")),
        //         //         Strategy.AnyMatch(rules.r("not x or x -> 1")),
        //         //         Strategy.AnyMatch(rules.r("x or not x -> 1"))
        //         //     ),
        //         //     // Strategy.AnyMatch(rules.r("if x y y -> y")),
        //         //     // Strategy.AnyMatch(rules.r("if 0 y z -> z")),
        //         //     // Strategy.AnyMatch(rules.r("if 1 y z -> y")),

        //         //     Strategy.All(
        //         //         Strategy.AnyMatch(rules.r("0 and x -> 0")),
        //         //         Strategy.AnyMatch(rules.r("x and 0 -> 0")),
        //         //         Strategy.AnyMatch(rules.r("1 and x -> x")),
        //         //         Strategy.AnyMatch(rules.r("x and 1 -> x")),
        //         //         Strategy.AnyMatch(rules.r("0 or x -> x")),
        //         //         Strategy.AnyMatch(rules.r("x or 0 -> x")),
        //         //         Strategy.AnyMatch(rules.r("1 or x -> 1")),
        //         //         Strategy.AnyMatch(rules.r("x or 1 -> 1")),
        //         //         Strategy.AnyMatch(rules.r("not 0 -> 1")),
        //         //         Strategy.AnyMatch(rules.r("not 1 -> 0"))
        //         //     )
        //         //     // Strategy.AnyMatch(rules.r("((a nor a) nor (a nor a)) -> a")),
        //         //     // Strategy.AnyMatch(rules.r("((a nand a) nand (a nand a)) -> a"))                  
        //         // ),            
        //         //expansion
        //         //now it is done on missing nodes
        //         // Strategy.On(
        //         //     Strategy.Depth(12, reduce),
        //         //     Strategy.Missing(Not.class, Strategy.AnyMatch(rules.r("x -> not not x"))),
        //         //     Strategy.Missing(And.class, Strategy.AnyMatch(rules.r("x -> x and x"))),
        //         //     Strategy.Missing(Or.class, Strategy.AnyMatch(rules.r("x -> x or x"))),
        //         //     Strategy.MissingERC(BusBit::testPresense, "n", 
        //         //         Strategy.Any(
        //         //             Strategy.AnyMatch(rules.r("x -> (n or not n) and x")),
        //         //             Strategy.AnyMatch(rules.r("x -> (n and not n) or x")))
        //         //         ))
        //         // ),                
        //         // Strategy.Any(
        //         //     Strategy.AnyMatch(rules.r("x -> x and x")),
        //         //     Strategy.AnyMatch(rules.r("x -> x or x")),
        //         //     Strategy.AnyMatch(rules.r("x -> not not x"))
        //         //     // Strategy.AnyMatch(rules.r("0 -> x and not x")),
        //         //     // Strategy.AnyMatch(rules.r("1 -> x or not x"))
        //         //     // Strategy.AnyMatch(rules.r("x -> if x x x"))
        //         // ),            
        //         //AND
        //         Strategy.Any(
        //             Strategy.AnyMatch(rules.r("x and y -> not (not x or not y)")),
        //             Strategy.AnyMatch(rules.r("not (not x or not y) -> x and y"))
        //             // Strategy.AnyMatch(rules.r("x and y -> if x y x")),
        //             // Strategy.AnyMatch(rules.r("if x y x -> x and y")),
        //             // Strategy.AnyMatch(rules.r("a and b -> (a nand b) nand (a nand b)")),
        //             // Strategy.AnyMatch(rules.r("(a nand b) nand (a nand b) -> a and b")),
        //             // Strategy.AnyMatch(rules.r("a and b -> (a nor a) nor (b nor b)")),
        //             // Strategy.AnyMatch(rules.r("(a nor a) nor (b nor b) -> a and b"))
        //         ),
        //         //grouped or ops
        //         Strategy.Any(
        //             Strategy.AnyMatch(rules.r("x or y -> not (not x and not y)")),
        //             Strategy.AnyMatch(rules.r("not (not x and not y) -> x or y"))
        //             // Strategy.AnyMatch(rules.r("x or y -> if x x y")),
        //             // Strategy.AnyMatch(rules.r("if x x y -> x or y")),
        //             // Strategy.AnyMatch(rules.r("a or b -> (a nand a) nand (b nand b)")),
        //             // Strategy.AnyMatch(rules.r("(a nand a) nand (b nand b) -> a or b")),
        //             // Strategy.AnyMatch(rules.r("a or b -> (a nor b) nor (a nor b)")),
        //             // Strategy.AnyMatch(rules.r("(a nor b) nor (a nor b) -> a or b"))
        //         ),

        //         //grouped if ops
        //         // Strategy.Any(
        //         //     Strategy.AnyMatch(rules.r("if x y z -> (x and y) or (not x and z)")), 
        //         //     Strategy.AnyMatch(rules.r("if x y z -> if (not x) z y")), 
        //         //     Strategy.AnyMatch(rules.r("if (not x) z y -> if x y z")), 
        //         //     Strategy.AnyMatch(rules.r("not (if x y z) -> if x (not y) (not z)")),
        //         //     Strategy.AnyMatch(rules.r("if x (not y) (not z) -> not (if x y z)"))
        //         // ),
        //         //NAND
        //         // Strategy.Any(
        //         //     Strategy.AnyMatch(rules.r("a nand b -> ((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b))")),
        //         //     // Strategy.AnyMatch(rules.r("((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b)) -> a nand b")),
        //         //     // Strategy.AnyMatch(rules.r("a nand a -> a nor a")),
        //         //     Strategy.AnyMatch(rules.r("a nand b -> not (a and b)")),
        //         //     Strategy.AnyMatch(rules.r("not (a and b) -> a nand b"))
        //         // ),

        //         //NOR
        //         // Strategy.Any(
        //         //     Strategy.AnyMatch(rules.r("a nor b -> ((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b))")),
        //         //     // Strategy.AnyMatch(rules.r("((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b)) -> a nor b")),
        //         //     // Strategy.AnyMatch(rules.r("a nor a -> a nand a")),
        //         //     Strategy.AnyMatch(rules.r("a nor b -> not (a or b)")),
        //         //     Strategy.AnyMatch(rules.r("not (a or b) -> a nor b"))
        //         // ),
                
        //         //movement                 
        //         Strategy.Any(
        //             //DeMorgan
        //             Strategy.AnyMatch(rules.r("not (x or y) -> not x and not y")),
        //             Strategy.AnyMatch(rules.r("not (x and y) -> not x or not y")),
        //             //DeMorgan rev
        //             Strategy.AnyMatch(rules.r("not x and not y -> not (x or y)")),
        //             Strategy.AnyMatch(rules.r("not x or not y -> not (x and y)")),

        //             Strategy.AnyMatch(rules.r("(y or z) and x -> (y and x) or (z and x)")),
        //             //(y or z) and x -> (y and x) or (z and x) -> (y or (z and x)) and (x or (z and x))
        //             Strategy.AnyMatch(rules.r("x and (y or z) -> (x and y) or (x and z)")),  
        //             Strategy.AnyMatch(rules.r("(y and z) or x -> (y or x) and (z or x)")),
        //             Strategy.AnyMatch(rules.r("x or (y and z) -> (x or y) and (x or z)"))                
        //         )
        //         // Strategy.AnyMatch(rules.r("x -> not not x"))
        //     ));        
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
            case "axioms": return axioms;
            case "tranPropag": return tranPropag;
            case "reduceOnDepth": return reduceOnDepth;
            default: return null;
        }
    }

    @Override
    public Set<Class<?>> getCommutativeFuncs() {
        return Set.of(And.class, Or.class);
    }    
    
}
