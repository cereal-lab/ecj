package ec.domain.bool;

import ec.*;
import ec.gp.*;

public class And extends GPNode {
    public String toString() { return "and"; }

    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
        BoolData data = (BoolData)input;

        children[0].eval(state,thread,input,stack,individual,problem);

        if (!data.out.isEmpty()) { //check for shortcut of  And
            BitSet firstArg = (BitSet)data.out.clone(); //no shortcut on case when all bits are 0
            children[1].eval(state,thread,input,stack,individual,problem);
            firstArg.and(data.out);
            data.out = firstArg;
        }
    }


}