package ec.domain.bool;

import java.util.List;

import ec.gp.transform.Nodes;
import ec.gp.transform.RuleProvider;
import ec.gp.transform.Transform;

public class BoolRuleProvider extends RuleProvider {

    @Override
    public List<Transform> getRules() {
        return List.of(
            //reduction rules 
            Transform.of("x and x -> x", c(And.class, Nodes.Any("x"), Nodes.Any("x")), Nodes.Any("x")), 
            Transform.of("x -> x and x", Nodes.Any("x"), c(And.class, Nodes.Any("x"), Nodes.Any("x"))), 
            Transform.of("x or x -> x", c(Or.class, Nodes.Any("x"), Nodes.Any("x")), Nodes.Any("x")),
            Transform.of("x -> x or x", Nodes.Any("x"), c(Or.class, Nodes.Any("x"), Nodes.Any("x"))),
            Transform.of("x -> if x x x", Nodes.Any("x"), c(If.class, Nodes.Any("x"), Nodes.Any("x"), Nodes.Any("x"))),
            Transform.of("not not x -> x", c(Not.class, c(Not.class, Nodes.Any("x"))), Nodes.Any("x")),
            Transform.of("x -> not not x", Nodes.Any("x"), c(Not.class, c(Not.class, Nodes.Any("x")))),
            Transform.of("not x and x -> 0", c(And.class, c(Not.class, Nodes.Any("x")), Nodes.Any("x")), 
                            c(False.class)),
            Transform.of("x and not x -> 0", c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), 
                            c(False.class)),
            Transform.of("not x or x -> 1", c(Or.class, c(Not.class, Nodes.Any("x")), Nodes.Any("x")), 
                            c(True.class)),      
            Transform.of("x or not x -> 1", c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), 
                            c(True.class)),                                                   
            Transform.of("if x y y -> y", c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("y")), Nodes.Any("y")),
            Transform.of("if 0 y z -> z", c(If.class, c(False.class), Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("z")),
            Transform.of("if 1 y z -> y", c(If.class, c(True.class), Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("y")),
            Transform.of("0 and x -> 0", c(And.class, c(False.class), Nodes.Any("x")), c(False.class)),
            Transform.of("x and 0 -> 0", c(And.class, Nodes.Any("x"), c(False.class)), c(False.class)),
            Transform.of("1 and x -> x", c(And.class, c(True.class), Nodes.Any("x")), Nodes.Any("x")),
            Transform.of("x and 1 -> x", c(And.class, Nodes.Any("x"), c(True.class)), Nodes.Any("x")),
            Transform.of("0 or x -> x", c(Or.class, c(False.class), Nodes.Any("x")), Nodes.Any("x")),
            Transform.of("x or 0 -> x", c(Or.class, Nodes.Any("x"), c(False.class)), Nodes.Any("x")),
            Transform.of("1 or x -> 1", c(Or.class, c(True.class), Nodes.Any("x")), c(True.class)),
            Transform.of("x or 1 -> 1", c(Or.class, Nodes.Any("x"), c(True.class)), c(True.class)),
            Transform.of("not 0 -> 1", c(Not.class, c(False.class)), c(True.class)),
            Transform.of("not 1 -> 0", c(Not.class, c(True.class)), c(False.class)),
            Transform.of("0 -> x and not x", c(False.class), c(And.class, Nodes.Gen("x", BusBit::rand), Nodes.Any("x"))),
            Transform.of("1 -> x or not x", c(True.class), c(Or.class, Nodes.Gen("x", BusBit::rand), Nodes.Any("x"))),

            Transform.of("x nand x -> not x", c(Nand.class, Nodes.Any("x"), Nodes.Any("x")), c(Not.class, Nodes.Any("x"))),
            Transform.of("not x -> x nand x", c(Not.class, Nodes.Any("x")), c(Nand.class, Nodes.Any("x"), Nodes.Any("x"))),

            Transform.of("x nor x -> not x", c(Nor.class, Nodes.Any("x"), Nodes.Any("x")), c(Not.class, Nodes.Any("x"))),
            Transform.of("not x -> x nor x", c(Not.class, Nodes.Any("x")), c(Nor.class, Nodes.Any("x"), Nodes.Any("x"))),


            //expressing other operators through NAND 
            Transform.of("a and b -> (a nand b) nand (a nand b)", 
                c(And.class, Nodes.Any("a"), Nodes.Any("b")), 
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("b")), c(Nand.class, Nodes.Any("a"), Nodes.Any("b")))), 

            //NOTE: generalize pattern to ignore arg positions - for now one pattern only
            Transform.of("(a nand b) nand (a nand b) -> a and b", 
                c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("b")), c(Nand.class, Nodes.Any("a"), Nodes.Any("b"))),
                c(And.class, Nodes.Any("a"), Nodes.Any("b"))), 
    
            Transform.of("a or b -> (a nand a) nand (b nand b)", 
                c(Or.class, Nodes.Any("a"), Nodes.Any("b")), 
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b")))), 

            Transform.of("(a nand a) nand (b nand b) -> a or b", 
                c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b"))),
                c(Or.class, Nodes.Any("a"), Nodes.Any("b"))), 

            Transform.of("a nor b -> ((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b))", 
                c(Nor.class, Nodes.Any("a"), Nodes.Any("b")),
                c(Nand.class, 
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b"))),
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b"))))),
                        
            Transform.of("((a nand a) nand (b nand b)) nand ((a nand a) nand (b nand b)) -> a nor b", 
                c(Nand.class, 
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b"))),
                    c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("b"), Nodes.Any("b")))),
                c(Nor.class, Nodes.Any("a"), Nodes.Any("b"))),

            //expressing other operators through NOR
            Transform.of("a or b -> (a nor b) nor (a nor b)", 
                c(Or.class, Nodes.Any("a"), Nodes.Any("b")), 
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("b")), c(Nor.class, Nodes.Any("a"), Nodes.Any("b")))), 

            //NOTE: generalize pattern to ignore arg positions - for now one pattern only
            Transform.of("(a nor b) nor (a nor b) -> a or b", 
                c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("b")), c(Nor.class, Nodes.Any("a"), Nodes.Any("b"))),
                c(Or.class, Nodes.Any("a"), Nodes.Any("b"))), 
    
            Transform.of("a and b -> (a nor a) nor (b nor b)", 
                c(And.class, Nodes.Any("a"), Nodes.Any("b")), 
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b")))), 

            Transform.of("(a nor a) nor (b nor b) -> a and b", 
                c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b"))),
                c(And.class, Nodes.Any("a"), Nodes.Any("b"))), 

            Transform.of("a nand b -> ((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b))", 
                c(Nand.class, Nodes.Any("a"), Nodes.Any("b")),
                c(Nor.class, 
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b"))),
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b"))))),
                        
            Transform.of("((a nor a) nor (b nor b)) nor ((a nor a) nor (b nor b)) -> a nand b",                 
                c(Nor.class, 
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b"))),
                    c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("b"), Nodes.Any("b")))),
                c(Nand.class, Nodes.Any("a"), Nodes.Any("b"))),

            //There is no NOT and IF in funcs of parity 
            // Transform.of("not not x -> x", c(Not.class, c(Not.class, Nodes.Any("x"))), Nodes.Any("x")),
            Transform.of("((a nor a) nor (a nor a)) -> a",
                c(Nor.class, c(Nor.class, Nodes.Any("a"), Nodes.Any("a")), c(Nor.class, Nodes.Any("a"), Nodes.Any("a"))),
                Nodes.Any("a")),

            Transform.of("((a nand a) nand (a nand a)) -> a",
                c(Nand.class, c(Nand.class, Nodes.Any("a"), Nodes.Any("a")), c(Nand.class, Nodes.Any("a"), Nodes.Any("a"))),
                Nodes.Any("a")),


            //NOTE: next two transforms should be more generic - searching of x and not x anywhere in children - pattern matching should  be more advanced for this
            // Transform.of("x and not x and y -> x and not x", c(And.class, c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), Nodes.Any("y")),
            //                     c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            // Transform.of("y and x and not x -> x and not x", c(And.class, Nodes.Any("y"), c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))), 
            //                     c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //should be formed from other rules 
            // Transform.of("not (x and not x) -> x or not x", c(Not.class, c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //                     c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            // Transform.of("if (x or not x) y z", c(If.class, c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), Nodes.Any("y"), Nodes.Any("z")),
            //                     Nodes.Any("y")),
            // Transform.of("x or not x or y -> x or not x", c(Or.class, c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), Nodes.Any("y")),
            //                     c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            // Transform.of("y or x or not x -> x or not x", c(Or.class, Nodes.Any("y"), c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //                     c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            // Transform.of("(x or not x) and y -> y", c(And.class, c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x"))), Nodes.Any("y")),
            //                     Nodes.Any("y")),
            // Transform.of("y and (x or not x) -> y", c(And.class, Nodes.Any("y"), c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //                     Nodes.Any("y")),
            // Transform.of("not (x or not x) -> x and not x", c(Not.class, c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //                     c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //unification 
            // Transform.of("not x and x -> x and not x", c(And.class, c(Not.class, Nodes.Any("x")), Nodes.Any("x")), c(And.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            // Transform.of("not x or x -> x or not x", c(Or.class, c(Not.class, Nodes.Any("x")), Nodes.Any("x")), c(Or.class, Nodes.Any("x"), c(Not.class, Nodes.Any("x")))),
            //distribution 
            Transform.of("(y or z) and x -> (y and x) or (z and x)", c(And.class, c(Or.class, Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("x")), 
                                c(Or.class, c(And.class, Nodes.Any("y"), Nodes.Any("x")), c(And.class, Nodes.Any("z"), Nodes.Any("x")))),
            Transform.of("x and (y or z) -> (x and y) or (x and z)", c(And.class,  Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z"))),
                                c(Or.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), c(And.class, Nodes.Any("x"), Nodes.Any("z")))),
            Transform.of("(y and z) or x -> (y or x) and (z or x)", c(Or.class, c(And.class, Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("x")),
                                c(And.class, c(Or.class, Nodes.Any("y"), Nodes.Any("x")), c(Or.class, Nodes.Any("z"), Nodes.Any("x")))),
            Transform.of("x or (y and z) -> (x or y) and (x or z)", c(Or.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z"))),
                                c(And.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), c(Or.class, Nodes.Any("x"), Nodes.Any("z")))),
            //distribution reverse (should be more generic - x could be in any place of two ands/ors)
            Transform.of("(y and x) or (z and x) -> (y or z) and x", 
                                c(Or.class, c(And.class, Nodes.Any("y"), Nodes.Any("x")), c(And.class, Nodes.Any("z"), Nodes.Any("x"))), 
                                c(And.class, c(Or.class, Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("x"))),
            Transform.of("(x and y) or (x and z) -> x and (y or z)", c(Or.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), c(And.class, Nodes.Any("x"), Nodes.Any("z"))),
                                c(And.class,  Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z")))),
            Transform.of("(y or x) and (z or x) -> (y and z) or x", c(And.class, c(Or.class, Nodes.Any("y"), Nodes.Any("x")), c(Or.class, Nodes.Any("z"), Nodes.Any("x"))),
                                c(Or.class, c(And.class, Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("x"))),
            Transform.of("(x or y) and (x or z) -> x or (y and z)", c(And.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), c(Or.class, Nodes.Any("x"), Nodes.Any("z"))),
                                c(Or.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z")))),
            //if transforms in this comments are wrong - nonneutral 
            // Transform.of("if (x or y) z t -> (if x z t) or (if y z t)", c(If.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t")),
            //                     c(Or.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t")))),
            // Transform.of("(if x z t) or (if y z t) -> if (x or y) z t", c(Or.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t"))),
            //                     c(If.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t"))),
            // Transform.of("if (x and y) z t -> (if x z t) and (if y z t)", c(If.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t")),
            //                     c(And.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t")))),
            //if transforms 
            Transform.of("if x y z -> (x and y) or (not x and z)", c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("z")), 
                            c(Or.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), c(And.class, c(Not.class, Nodes.Any("x")), Nodes.Any("z")))),
            Transform.of("if x y z -> if (not x) z y", c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("z")), 
                            c(If.class, c(Not.class, Nodes.Any("x")), Nodes.Any("z"), Nodes.Any("y"))),
            Transform.of("if (not x) z y -> if x y z", c(If.class, c(Not.class, Nodes.Any("x")), Nodes.Any("z"), Nodes.Any("y")),
                            c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("z"))),
            //assoc - no new genetic matherial - probably useless - need  to check - can be usefull for other transforms
            Transform.of("x or (y or z) -> (x or y) or z", c(Or.class, Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z"))),
                            c(Or.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"))),
            Transform.of("x and (y and z) -> (x and y) and z", c(And.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z"))),
                            c(And.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"))),
            Transform.of("(x or y) or z -> x or (y or z)", c(Or.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z")),
                            c(Or.class, Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z")))),
            Transform.of("(x and y) and z -> x and (y and z)", c(And.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z")),
                            c(And.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z")))),                        
            //commutativity
            Transform.of("x and y -> y and x", c(And.class, Nodes.Any("x"), Nodes.Any("y")), c(And.class, Nodes.Any("y"), Nodes.Any("x"))),
            Transform.of("x or y -> y or x", c(Or.class, Nodes.Any("x"), Nodes.Any("y")), c(Or.class, Nodes.Any("y"), Nodes.Any("x"))),
            // absorption - should be more generic 
            Transform.of("x or (x and y) -> x",  c(Or.class, Nodes.Any("x"), c(And.class, Nodes.Any("x"), Nodes.Any("y"))), Nodes.Any("x")),
            Transform.of("x and (x or y) -> x", c(And.class, Nodes.Any("x"), c(Or.class, Nodes.Any("x"), Nodes.Any("y"))), Nodes.Any("x")),

            // DeMorgan
            Transform.of("not (x or y) -> not x and not y", c(Not.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y"))),
                            c(And.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y")))),
            Transform.of("not (x and y) -> not x or not y", c(Not.class, c(And.class, Nodes.Any("x"), Nodes.Any("y"))),
                            c(Or.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y")))),                        
            // DeMorgan rev
            Transform.of("not x and not y -> not (x or y)", c(And.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y"))),
                            c(Not.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")))),
            Transform.of("not x or not y -> not (x and y)", c(Or.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y"))),
                            c(Not.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")))),
            //producing genetic material
            // Transform.of("x -> not not x", Nodes.Any("x"), c(Not.class, c(Not.class, Nodes.Any("x")))),
            //transform and to other ops
            Transform.of("x and y -> not (not x or not y)", c(And.class, Nodes.Any("x"), Nodes.Any("y")), 
                            c(Not.class, c(Or.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y"))))),
            Transform.of("not (not x or not y) -> x and y", c(Not.class, c(Or.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y")))),
                            c(And.class, Nodes.Any("x"), Nodes.Any("y"))),
            Transform.of("x and y -> if x y x", c(And.class, Nodes.Any("x"), Nodes.Any("y")), 
                            c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("x"))),
            Transform.of("if x y x -> x and y", c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("x")),
                            c(And.class, Nodes.Any("x"), Nodes.Any("y"))),
            //transform or to other ops
            Transform.of("x or y -> not (not x and not y)", c(Or.class, Nodes.Any("x"), Nodes.Any("y")), 
                            c(Not.class, c(And.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y"))))),
            Transform.of("not (not x and not y) -> x or y", c(Not.class, c(And.class, c(Not.class, Nodes.Any("x")), c(Not.class, Nodes.Any("y")))),
                            c(Or.class, Nodes.Any("x"), Nodes.Any("y"))),
            Transform.of("x or y -> if x x y", c(Or.class, Nodes.Any("x"), Nodes.Any("y")), 
                            c(If.class, Nodes.Any("x"), Nodes.Any("x"), Nodes.Any("y"))),
            Transform.of("if x x y -> x or y", c(If.class, Nodes.Any("x"), Nodes.Any("x"), Nodes.Any("y")),
                            c(Or.class, Nodes.Any("x"), Nodes.Any("y"))),
            //not propagation into if        
            Transform.of("not (if x y z) -> if x (not y) (not z)", c(Not.class, c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("z"))), 
                            c(If.class, Nodes.Any("x"), c(Not.class, Nodes.Any("y")), c(Not.class, Nodes.Any("z")))),
            //not propagation out of if     
            Transform.of("if x (not y) (not z) -> not (if x y z)", c(If.class, Nodes.Any("x"), c(Not.class, Nodes.Any("y")), c(Not.class, Nodes.Any("z"))),
                            c(Not.class, c(If.class, Nodes.Any("x"), Nodes.Any("y"), Nodes.Any("z")))),
            //generative
            Transform.of("x -> (n or not n) and x", Nodes.Any("x"), c(And.class, c(Or.class, Nodes.Any("n"), c(Not.class, Nodes.Any("n"))), Nodes.Any("x"))),
            Transform.of("x -> (n and not n) or x", Nodes.Any("x"), c(Or.class, c(And.class, Nodes.Any("n"), c(Not.class, Nodes.Any("n"))), Nodes.Any("x")))
        );
    }
    
}
