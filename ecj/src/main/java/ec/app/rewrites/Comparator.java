package ec.app.rewrites;
import ec.util.*;

import java.util.BitSet;

import ec.*;
import ec.domain.bool.BoolData;
import ec.domain.bool.BoolProblem;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class Comparator extends BoolProblem {
    private static final long serialVersionUID = 1;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state,base);

        int halfBus = busSize() >>> 1;
        int mask = (1 << halfBus) - 1;
        for (int i = 0; i < this.testSetSize(); i++) {
            int lowerPart = i & mask;
            int upperPart = i >>> halfBus;
            expectedOut.set(i, lowerPart > upperPart);
        }

        expectedOut.finalize();
    }

}
