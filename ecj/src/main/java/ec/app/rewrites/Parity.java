package ec.app.rewrites;
import ec.util.*;

import java.util.BitSet;

import ec.*;
import ec.domain.bool.BoolData;
import ec.domain.bool.BoolProblem;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class Parity extends BoolProblem {
    private static final long serialVersionUID = 1;    

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state,base);        

        for (int i = 0; i < this.testSetSize(); i++) {
            boolean oddNumberOfBits = (Integer.bitCount(i) & 1) == 1;  // now tb is 1 if we're odd, 0 if we're even    
            expectedOut.set(i, oddNumberOfBits);
        }

        expectedOut.finalize();
    }

}
