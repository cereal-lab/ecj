package ec.app.multiplexer;

import java.util.Objects;
import java.util.function.Predicate;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Nodes {
    public static class Any extends GPNode {

        private static final long serialVersionUID = 1L;
        private String metaName;
        private Predicate<GPNode> filter;

        public Any(String metaName, Predicate<GPNode> filter, GPNode... children) {
            this.metaName = metaName;
            this.filter = filter;
            this.children = children; // new GPNode[0];
            for (int i = 0; i < children.length; i++) 
                children[i].argposition = (byte)i;
        }

        public Any(String metaName, GPNode... children) {
            this(metaName, node -> true, children);
        }

        public boolean matches(GPNode node) {
            return filter.test(node);
        }

        // public Any() {
        //     this(null);
        // }
        @Override 
        public Object clone() {
            Any cl = (Any)super.clone();
            cl.metaName = this.metaName;
            return cl;
        }

        @Override
        public String toString() {        
            return this.metaName == null ? "_" : this.metaName;
        }

        @Override
        public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
                Problem problem) {
            //NOTE: this method should never be called, Any node plays role only in meta-analysis of AST of ind 
            state.output.fatal("Transform.Any node should not be used in evaluation process"); 
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Any && ((Any)other).metaName.equals(this.metaName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.metaName);
        }

    }
    
    public static GPNode Any(String name, GPNode... children) {
        return new Any(name, children);
    }

    public static GPNode Any(String name, Predicate<GPNode> filter, GPNode... children) {
        return new Any(name, filter, children);
    }    
    
}
