package ec.domain.bool;

import java.util.ArrayList;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.gp.koza.KozaFitness;
import ec.util.Output;
import ec.util.Parameter;

public class BoolProblem extends GPProblem {
    private final static String P_SIZE = "busSize";
    protected int size;
    public ec.domain.bool.BitSet trueV;
    public ec.domain.bool.BitSet falseV;
    public ec.domain.bool.BitSet expectedOut; 

    public int busSize() {
        return size;
    }

    public int testSetSize() {
        return 1 << this.busSize(); //by default test set size is whole true table
    }

    @Override
    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state,base);

        this.size = state.parameters.getInt(base.push(P_SIZE), defaultBase().push(P_SIZE), 1);
        if (this.size == 0)
            state.output.fatal("The number of bits for Multiplexer must be between 1 and 3 inclusive");

        this.trueV = new ec.domain.bool.BitSet(this.testSetSize());
        this.trueV.set(0, this.trueV.realSize());
        this.trueV.finalize();
        this.falseV = new ec.domain.bool.BitSet(this.testSetSize());
        this.falseV.finalize();

        if (!(input instanceof BoolData))
            state.output.fatal("GPData class must subclass from " + BoolData.class,
                base.push(P_DATA), null);

        BoolData data = (BoolData)this.input;
        
        data.initTestCases(this.busSize());

        expectedOut = new ec.domain.bool.BitSet(this.testSetSize()); //should be set in the child classes
        
    }

    @Override
    public void evaluate(final EvolutionState state, 
        final Individual ind, 
        final int subpopulation,
        final int threadnum) {
        if (!ind.evaluated) {
            BoolData input = (BoolData)(this.input);

            // ((GPIndividual)ind).trees[0].child.printRootedTreeForHumans(state, Output.ALL_MESSAGE_LOGS, 0, 0);
            // state.output.message("");
                
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);
                
            BitSet out = (BitSet)input.out.clone();
            out.xor(expectedOut);
            out.flip(0, out.realSize());
            out.and(trueV); //as mask
            int hits = out.cardinality();
                
            // the fitness better be KozaFitness!
            KozaFitness f = ((KozaFitness)ind.fitness);
            f.setStandardizedFitness(state, (testSetSize() - hits));
            f.hits = hits;
            ind.evaluated = true;
        }
    }    
    
}
