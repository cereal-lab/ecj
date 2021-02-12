package ec.gp.transform;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import ec.EvolutionState;
import ec.Individual;
import ec.domain.bool.BoolProblem;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.koza.KozaFitness;
import ec.gp.koza.MutationPipeline;
import ec.gp.transform.Strategy.StrategyResult;
import ec.gp.transform.Strategy.StrategyStats;
import ec.gp.transform.Transform.ReplaceFailed;
import ec.util.Output;
import ec.util.Parameter;
// import ec.app.multiplexerslow.func.*;

public class NeutralMutator extends MutationPipeline {

    private static class TransformStats {
        private int numTransformed;
        private int numTotal;

        public TransformStats(final int numTransformed, final int numTotal) {
            this.numTransformed = numTransformed;
            this.numTotal = numTotal;
        }
    }

    private static final long serialVersionUID = 1L;
    // final String P_MUTATION = "nmutate";
    public static final String P_SHOW_STATS = "showStats";
    public static final String P_STRATEGY_PROVIDER = "strategy";
    private Boolean showTransformStats;
    private TransformStats stats;
    private StrategyStats sstats;
    private StrategyProvider strategies;
    private int treesBeforeLog = 0;
    private int treesAfterLog = 0;    
    
    //
    //1. Run NM on all 4 Problems -> select 'best' problem
    //2. Apply agressive mutation  -> best outcome 
    //3. Compare agressive with one site mutator on best problem and best setup from 2
    //
    //Monday ~4pm - Lecture hall 
    //before going to smart neutral mutator - explore bloating problem - Sean Luke works.
    //[Selection pressure] K-tournament 2 of 1024 vs 2 of 2. 
    //group of inds with same fitness - each of them should have same prob with K-tournament. 
    //effects of neutral mutator under different selection pressures. 
    //BestSelector vs TournamentSelection in presence of NeutralMutator env. n best (5 of 64 for ex). 

    //null hypothesis - neutral mutator is insignificent 
                

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state, base);
        Parameter def = defaultBase();
        Parameter p = base.push(P_SHOW_STATS);
        Parameter d = def.push(P_SHOW_STATS);
        this.showTransformStats = state.parameters.getBoolean(p, d, false);
        p = base.push(P_STRATEGY_PROVIDER);
        d = def.push(P_STRATEGY_PROVIDER);
        this.strategies = (StrategyProvider)(state.parameters.getInstanceForParameter(p, d, StrategyProvider.class));
        this.strategies.setup(state, base);
        this.sstats = new StrategyStats();
        
        // if (this.debugOut.equals("all"))
        // {
        // try {
        //     treesBeforeLog = state.output.addLog(new File("treesBefore.txt"), false);
        // } catch (IOException e) {
        //     state.output.fatal("Cannot create log treesBefore.txt");
        // }
        // try {
        //     treesAfterLog = state.output.addLog(new File("treesAfter.txt"), false);
        // } catch (IOException e) {
        //     state.output.fatal("Cannot create log treesAfter.txt");
        // }
    }

    @Override 
    public void prepareToProduce(EvolutionState state, int subpopulation, int thread) {
        super.prepareToProduce(state, subpopulation, thread);
        stats = new TransformStats(0, 0);
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

            // Map<Class<?>, Integer> nonTermStats = new HashMap<Class<?>, Integer>();
            // nonTermStats.put(If.class, 0);
            // nonTermStats.put(And.class, 0);
            // nonTermStats.put(Or.class, 0);
            // nonTermStats.put(Not.class, 0);
            // Map<Class<?>, Integer> termStats = new HashMap<Class<?>, Integer>();
            // collectStats(i.trees[0].child, nonTermStats, termStats);                    

            // if (nonTermStats.entrySet().stream().anyMatch(kv -> kv.getValue() > 0)) {
            //     StringBuilder sb = new StringBuilder();
            //     for (Class<?> key: nonTermStats.keySet()) {
            //         sb.append(key.getSimpleName()).append(": ").append(nonTermStats.get(key)).append("  ");
            //     }
            //     state.output.message("--- " + sb.toString());
            // }

            // state.output.message("-------------->Mutating individual");
            // state.output.println("", log);

            // i.printTrees(state, treesBeforeLog);

            // KozaFitness fitnessBefore = (KozaFitness)i.fitness;
            Strategy strategy = strategies.getStrategy(i.trees[0].child);
            // strategy.setLogger(state.output::message);
            // strategy.set(sstats);
            // i.trees[0].child.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
            // state.output.message("");
            stats.numTotal += 1;
            try {
                GPNode before = (GPNode)i.trees[0].child.clone();
                KozaFitness fitnessBefore = (KozaFitness)i.fitness;                
                StrategyResult res = strategy.apply(i.trees[0].child, state, thread);
                // i.trees[0].child.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
                // state.output.message("");                
                if (res.appliedTransform.size() > 0) {
                    stats.numTransformed += 1;
                }
                // state.output.message(res.appliedTransform.size()+"");
                // if (res.appliedTransform.size() > 0) {

                //     BoolProblem problem = (BoolProblem)state.evaluator.p_problem;                
                //     i.fitness = (KozaFitness)fitnessBefore.clone();
                //     i.evaluated = false;
                //     // synchronized (this) {
                //     problem.evaluate(state, i, subpopulation, thread);
                //     // }
                //     // i.evaluated = false;
                //     // problem.evaluate(state, i, subpopulation, thread);                     
                    
                //     KozaFitness fitessAfter = (KozaFitness)i.fitness;
                //     if (fitnessBefore.standardizedFitness() != fitessAfter.standardizedFitness()) {

                //         state.output.message("\n---------------------");
                //         for (Strategy.TransformApplication a: res.appliedTransform) {
                //             a.printTransform(state, Output.ALL_MESSAGE_LOGS);
                //         }
                //         state.output.message("---------------------");

                //         before.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
                //         state.output.message("\n" + fitnessBefore.fitnessToStringForHumans() + "\n");
    
                //         i.trees[0].child.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
                //         state.output.message("\n" + fitessAfter.fitnessToStringForHumans() + "\n");

                //         // i.evaluated = false;
                //         // problem.evaluate(state, i, subpopulation, thread); 
                //         // fitessAfter = (KozaFitness)i.fitness;                       

                //         // if (fitnessBefore.standardizedFitness() != fitessAfter.standardizedFitness()) {
                //         state.output.fatal("\nNot a neutral transform\n");
                //         // }
                //     }
                // }
            } catch (ReplaceFailed e) {
                state.output.fatal("Replacement failed due to metadata missmatch");
            }

            // i.printTrees(state, treesAfterLog);

            // state.output.message("-------------->Done ind");                    
        }
        return n;
    }    
    
    @Override
    public void finishProducing(final EvolutionState state,
        final int subpopulation,
        final int thread)
        {
            if (this.showTransformStats) {
                state.output.message(String.format("-- %d/%d rewriten/seen", stats.numTransformed, stats.numTotal));
                sstats.stats.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue())).forEach(e -> {
                    state.output.message(String.format("\t[%s] %d", e.getKey(), e.getValue()));
                });
            }            
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

