package ec.domain.bool;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class Not extends GPNode {
    public String toString() { return "not"; }

    public int expectedChildren() { return 1; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
        BoolData data = (BoolData)input;
        children[0].eval(state,thread,input,stack,individual,problem);

        data.out = (BitSet)data.out.clone();
        data.out.flip(0, data.out.realSize());
    }
}



