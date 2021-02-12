package ec.app.rewrites;
import ec.util.*;

import java.util.BitSet;

import ec.*;
import ec.domain.bool.BoolData;
import ec.domain.bool.BoolProblem;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class Majority extends BoolProblem {
    private static final long serialVersionUID = 1;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state,base);

        for (int i = 0; i < this.testSetSize(); i++) {
            int ones = Integer.bitCount(i);            
            Boolean moreOnes = ones + ones > this.busSize();
            expectedOut.set(i, moreOnes);
        }

        expectedOut.finalize();
    }

}
