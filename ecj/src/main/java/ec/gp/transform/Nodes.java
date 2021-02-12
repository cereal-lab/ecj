package ec.gp.transform;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Nodes {

    public static abstract class MetaNamed extends GPNode {
        protected String metaName;

        public MetaNamed(String metaName) {
            this.metaName = metaName;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof MetaNamed && ((MetaNamed)other).metaName.equals(this.metaName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.metaName);
        }

        @Override 
        public Object clone() {
            MetaNamed cl = (MetaNamed)super.clone();
            cl.metaName = this.metaName;
            return cl;
        }  
        
        @Override
        public String toString() {        
            return this.metaName == null ? "_" : this.metaName;
        }        

    }

    public static class Any extends MetaNamed {

        private static final long serialVersionUID = 1L;
        private Predicate<GPNode> filter;

        public Any(String metaName, Predicate<GPNode> filter, GPNode... children) {
            super(metaName);
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

        @Override
        public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
                Problem problem) {
            //NOTE: this method should never be called, Any node plays role only in meta-analysis of AST of ind 
            state.output.fatal("Transform.Any node should not be used in evaluation process"); 
        }

    }

    public static class Gen extends MetaNamed { //delayed node

        private static final long serialVersionUID = 1L;
        private BiFunction<EvolutionState, Integer, GPNode> nodeCreator;

        public Gen(String metaName, BiFunction<EvolutionState, Integer, GPNode> nodeCreator) {
            super(metaName);
            this.nodeCreator = nodeCreator;
        }

        public GPNode create(EvolutionState state, int thread) {
            return nodeCreator.apply(state, thread);
        }

        @Override
        public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
                Problem problem) {
            state.output.fatal("Transform.Gen node should not be used in evaluation process"); 
        }

    }

    
    public static GPNode Any(String name, GPNode... children) {
        return new Any(name, children);
    }

    public static GPNode Any(String name, Predicate<GPNode> filter, GPNode... children) {
        return new Any(name, filter, children);
    }    

    public static GPNode Gen(String name, BiFunction<EvolutionState, Integer, GPNode> nodeCreator) {
        return new Gen(name, nodeCreator);
    }
    
}
