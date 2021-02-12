package ec.domain.bool;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class True extends GPNode {

    public String toString() { return "1"; }

    public int expectedChildren() { return 0; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
            BoolData data = (BoolData)input;

            data.out = ((BoolProblem)problem).trueV;
    }
}