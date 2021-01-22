package ec.app.multiplexer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.koza.MutationPipeline;
import ec.util.Parameter;
import ec.app.multiplexer.Strategy.ReplaceFailed;
import ec.app.multiplexerslow.func.*;

public class NeutralMutator extends MutationPipeline {
    private static final long serialVersionUID = 1L;
    // final String P_MUTATION = "nmutate";

    private GPNode c(Class<?> c, GPNode... children) {
        GPNode res;
        try {
            res = (GPNode) c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            return null;
        }
        res.children = children;
        return res;
    }
    
    final Strategy rewrite = 
    // Strategy.All(

        // Transform.AnyOfMatches(
            Transform.FirstMatch( // "distribution AND-OR 1",
                c(And.class, 
                    c(Or.class, Nodes.Any("y"), Nodes.Any("z")), 
                    Nodes.Any("x")),
                c(Or.class, 
                    c(And.class, Nodes.Any("y"), Nodes.Any("x")), 
                    c(And.class, Nodes.Any("z"), Nodes.Any("x"))));
            // Transform.AnyMatch( // "distribution AND-OR rev 1",
            //     c(Or.class, 
            //         c(And.class, Nodes.Any("y"), Nodes.Any("x")), 
            //         c(And.class, Nodes.Any("z"), Nodes.Any("x"))),
            //     c(And.class, Nodes.Any("x"), 
            //         c(Or.class, Nodes.Any("y"), Nodes.Any("z")))), // TODO
        // ),

        // Transform.AnyOfMatches(
            // Transform.AnyMatch( // "distribution AND-OR 2",
            //     c(And.class, 
            //         Nodes.Any("x"), 
            //         c(Or.class, Nodes.Any("y"), Nodes.Any("z"))),
            //     c(Or.class, 
            //         c(And.class, Nodes.Any("x"), Nodes.Any("y")), 
            //         c(And.class, Nodes.Any("x"), Nodes.Any("z")))),
            
            // Transform.AnyMatch( // "distribution AND-OR rev 2",
            //     c(Or.class, 
            //         c(And.class, Nodes.Any("x"), Nodes.Any("y")), 
            //         c(And.class, Nodes.Any("x"), Nodes.Any("z"))),
            //     c(And.class, Nodes.Any("x"), 
            //         c(Or.class, Nodes.Any("y"), Nodes.Any("z"))))        
        // ), 
            
        // Transform.AnyOfMatches(
            // Transform.AnyMatch( // "distribution OR-AND",
            //     c(Or.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z"))),
            //         c(And.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), c(Or.class, Nodes.Any("x"), Nodes.Any("z")))),
            // Transform.AnyMatch( // "distribution OR-AND",
            //     c(Or.class, c(And.class, Nodes.Any("y"), Nodes.Any("z")), Nodes.Any("x")),
            //         c(And.class, c(Or.class, Nodes.Any("y"), Nodes.Any("x")), c(Or.class, Nodes.Any("z"), Nodes.Any("x"))))
        // ), 
        // Transform.AnyOfMatches(
        //     Transform.AnyMatch( // "IF-1",
        //         c(If.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t")),
        //             c(Or.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t")))
        //         ),
        //     Transform.AnyMatch( // "IF-1 rev",
        //         c(Or.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t"))),
        //             c(If.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t"))),
        //     Transform.AnyMatch( // "IF-2",
        //         c(If.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t")),
        //             c(And.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")), c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t")))),
        //     Transform.AnyMatch(// "IF-2 rev",
        //         c(And.class, c(If.class, Nodes.Any("x"), Nodes.Any("z"), Nodes.Any("t")),
        //             c(If.class, Nodes.Any("y"), Nodes.Any("z"), Nodes.Any("t"))), c(If.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"), Nodes.Any("t"))),
        //     Transform.AnyMatch(// "IF-3",
        //         c(If.class, c(Not.class, Nodes.Any("x")), Nodes.Any("z"), Nodes.Any("t")),
        //             c(If.class, Nodes.Any("x"), Nodes.Any("t"), Nodes.Any("z"))),
        //     Transform.AnyMatch(// "IF-3 rev",
        //         c(If.class, Nodes.Any("x"), Nodes.Any("t"), Nodes.Any("z")),
        //             c(If.class, c(Not.class, Nodes.Any("x")), Nodes.Any("z"), Nodes.Any("t")))
        // ),
        
        // Transform.AnyOfMatches(
        //     Transform.AnyMatch( // "assoc OR",
        //         c(Or.class, Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z"))),
        //             c(Or.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"))),
        //     Transform.AnyMatch( // "assoc OR rev",
        //         c(Or.class, c(Or.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z")),
        //             c(Or.class, Nodes.Any("x"), c(Or.class, Nodes.Any("y"), Nodes.Any("z"))))
        // ),
        
        // Transform.AnyOfMatches(
        //     Transform.AnyMatch( // "assoc And",
        //         c(And.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z"))),
        //             c(And.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z"))),
        //     Transform.AnyMatch( // "assoc And rev",
        //         c(And.class, c(And.class, Nodes.Any("x"), Nodes.Any("y")), Nodes.Any("z")),
        //             c(And.class, Nodes.Any("x"), c(And.class, Nodes.Any("y"), Nodes.Any("z"))))
        // ),
        // Transform.AnyMatch( // "commutativity And",
        //     c(And.class, Nodes.Any("x"), Nodes.Any("y")), 
        //         c(And.class, Nodes.Any("y"), Nodes.Any("x"))),
        // Transform.AnyMatch( // "commutativity Or",
        //     c(Or.class, Nodes.Any("x"), Nodes.Any("y")), 
        //         c(Or.class, Nodes.Any("y"), Nodes.Any("x"))));

    private int treesBeforeLog = 0;
    private int treesAfterLog = 0;

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        try {
            treesBeforeLog = state.output.addLog(new File("treesBefore.txt"), false);
        } catch (IOException e) {
            state.output.fatal("Cannot create log treesBefore.txt");
        }
        try {
            treesAfterLog = state.output.addLog(new File("treesAfter.txt"), false);
        } catch (IOException e) {
            state.output.fatal("Cannot create log treesAfter.txt");
        }
    }

    @Override
    public int produce(int min, int max, int subpopulation, ArrayList<Individual> inds, EvolutionState state,
            int thread, HashMap<String, Object> misc) {
        int start = inds.size();

        int n = sources[0].produce(min, max, subpopulation, inds, state, thread, misc);

        if (!state.random[thread].nextBoolean(likelihood))
            return n;

        for (int q = start; q < n + start; q++) {
            GPIndividual i = (GPIndividual) inds.get(q);

            // state.output.message("-------------->Mutating individual");
            // state.output.println("", log);
            i.printTrees(state, treesBeforeLog);

            try {
                rewrite.apply(i.trees[0].child, state.random[thread]);
            } catch (ReplaceFailed e) {
                state.output.fatal("Replacement failed due to metadata missmatch");
            }
            i.printTrees(state, treesAfterLog);

            // state.output.message("-------------->Done");                    
        }
        return n;
    }            
}

//DO experiments with current strategy of rewrite (rand match), next exper with different strategies
//start with simple set of axioms 
//1. Impact on convergence (gen num)
//2. Num of time when soluton was found (0 was captured)
//3. Time on platoe 
// 
//
//4. Diversity - develop custom Stats - AFTER

