package ec.app.majorityBits;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.util.Parameter;

public class Majority extends GPProblem {

    public int bits; 
    public int dataPart; 

    @Override
    public void setup(EvolutionState state, Parameter base) {
        super.setup(state, base);
        this.bits = state.parameters.getIntWithMax(base.push("bits"), defaultBase().push("bits"), 3, 31);
    }

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {
        // TODO Auto-generated method stub
        if (ind.evaluated) return;
        final int upperBound = 1 << this.bits; 

        MajorityData input = (MajorityData)(this.input);
        
        int hits = 0;
        int total = 0;
        for (dataPart = 0; dataPart < upperBound; dataPart++) {
            int ones = Integer.bitCount(dataPart);
            ((GPIndividual)ind).trees[0].child.eval(state, threadnum, input, stack, (GPIndividual)ind, this);
            
            Boolean moreZeroes = ones + ones < this.bits;
            Boolean moreOnes = ones + ones > this.bits;
            if ((moreZeroes && ((input.x & 1) == 0)) || (moreOnes && ((input.x & 1) == 1))) {
                hits++;
            }
            if (moreZeroes || moreOnes) total++;
        }

        KozaFitness f = ((KozaFitness)ind.fitness);
        f.setStandardizedFitness(state, (total - hits));
        f.hits = hits;
        ind.evaluated = true;

    }

    
}
