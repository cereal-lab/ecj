package ec.app.comparator;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.util.Output;
import ec.util.Parameter;

public class Comparator extends GPProblem {
    //compares top and bottom parts of the word 

    public int bits; 
    public int dataPart; 

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.bits = state.parameters.getIntWithMax(base.push("bits"), defaultBase().push("bits"), 2, 10);
    }
    
    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        if (ind.evaluated) return;
        final int upperBound = 1 << (this.bits << 1);
        final int lowMask = (1 << this.bits) - 1;

        ComparatorData input = (ComparatorData)(this.input);

        int total = 0; 
        int hits = 0;
        // ((GPIndividual)ind).trees[0].child.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
        for (dataPart = 0; dataPart < upperBound; dataPart++) {
            int upperPart = (dataPart >>> this.bits);
            int lowerPart = (dataPart & lowMask);
            Boolean lowerIsGreater = lowerPart > upperPart; 
            // Boolean lowerIsLower = lowerPart < upperPart; 
            ((GPIndividual)ind).trees[0].child.eval(state, threadnum, input, stack, (GPIndividual)ind, this);
            // if ((lowerIsLower && ((input.x & 1) == 0)) || (lowerIsGreater && ((input.x & 1) == 1))) {
            //     hits++;
            // }
            if (lowerIsGreater == ((input.x & 1) == 1)) {
                hits++;
            }
            total++;
        }

        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state, (total - hits));
        f.hits = hits;
        ind.evaluated = true;
    }

    @Override
    public void finishEvaluating(final EvolutionState state, final int threadnum) {
        // state.output.message("-----------------------");
    }
    
}
