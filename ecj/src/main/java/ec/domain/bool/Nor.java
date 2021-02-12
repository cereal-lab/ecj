package ec.domain.bool;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class Nor extends GPNode {

    private GPNode notGate; 
    private GPNode orGate; 

    public Nor() {
        notGate = new Not();
        orGate = new Or();
        notGate.children = new GPNode[] { orGate };
        orGate.parent = notGate;
        orGate.argposition = 0;
    }
    
    public String toString() { return "nor"; }

    public int expectedChildren() { return 2; }

    public void eval(final EvolutionState state,
        final int thread,
        final GPData input,
        final ADFStack stack,
        final GPIndividual individual,
        final Problem problem) {
            
            orGate.children = this.children;
            for (GPNode ch: this.children) {
                ch.parent = orGate;
            }
    
            notGate.parent = this.parent;
            notGate.argposition = this.argposition;

            notGate.eval(state, thread, input, stack, individual, problem); 
    }
}