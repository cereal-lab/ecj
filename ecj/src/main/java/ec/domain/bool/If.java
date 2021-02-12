package ec.domain.bool;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class If extends GPNode {
    public String toString() { return "if"; }

    public int expectedChildren() { return 3; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
            BoolData data = (BoolData)input;
            children[0].eval(state,thread,input,stack,individual,problem);
            BitSet ifBits = data.out;
            // BitSet res = new BitSet();
            // BitSet arg1 = null;
            // BitSet arg2 = null; 
            //(x and y) or (not x and z)
            if (ifBits.isEmpty()) {
                children[2].eval(state,thread,input,stack,individual,problem);
            } else if (ifBits.cardinality() == ifBits.realSize()) {
                children[1].eval(state,thread,input,stack,individual,problem);
            } else {
                ifBits = (BitSet)ifBits.clone();
                children[1].eval(state,thread,input,stack,individual,problem);
                BitSet arg1 = (BitSet)data.out.clone(); 
                children[2].eval(state,thread,input,stack,individual,problem);
                BitSet arg2 = (BitSet)data.out.clone();
                arg1.and(ifBits);
                arg2.andNot(ifBits);
                arg1.or(arg2);
                data.out = arg1;
            }
    }
}



