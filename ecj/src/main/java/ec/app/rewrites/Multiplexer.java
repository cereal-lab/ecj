package ec.app.rewrites;
import ec.util.*;

import java.util.BitSet;

import ec.*;
import ec.domain.bool.BoolData;
import ec.domain.bool.BoolProblem;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

public class Multiplexer extends BoolProblem {
    private static final long serialVersionUID = 1;

    public void setup(final EvolutionState state, final Parameter base) {
        super.setup(state,base);

        int controlBits = 0;
        for (;controlBits < 5; controlBits++) {
            if (controlBits + (1 << controlBits) == busSize()) break;
        }
        if (controlBits == 0) 
            state.output.fatal(String.format("Not all data bits are addressable. Bus size %d", busSize()));

        int dataBits = busSize() - controlBits;

        for (int i = 0; i < this.testSetSize(); i++) {
            int address = i >>> dataBits;
            expectedOut.set(i, ((i >>> address) & 1) == 1);
        }

        expectedOut.finalize();

    }

}
