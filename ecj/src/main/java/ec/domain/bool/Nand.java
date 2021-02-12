package ec.domain.bool;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class Nand extends GPNode {

    private GPNode notGate; 
    private GPNode andGate; 

    public String toString() { return "nand"; }

    public int expectedChildren() { return 2; }

    public Nand() {
        notGate = new Not();
        andGate = new And();
        notGate.children = new GPNode[] { andGate };
        andGate.parent = notGate;
        andGate.argposition = 0;
    }    

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
            
            andGate.children = this.children;
            for (GPNode ch: this.children) {
                ch.parent = andGate;
            }
    
            notGate.parent = this.parent;
            notGate.argposition = this.argposition;

            notGate.eval(state, thread, input, stack, individual, problem); 
    }
}