package ec.domain.bool;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import ec.*;
import ec.gp.*;
import ec.util.*;

public class BusBit extends ERC {

    public int lineNo;

    public static BusBit rand(EvolutionState state, int thread) {
        BusBit bit = new BusBit();
        bit.setup(state, bit.defaultBase());
        bit.resetNode(state, thread);
        return bit;
    }

    public static Optional<ERC> testPresense(EvolutionState state, int thread, Set<ERC> present) {
        Set<Integer> allLines = 
            new HashSet<>(IntStream.range(0, ((BoolProblem)state.evaluator.p_problem).busSize())
                .mapToObj(Integer::valueOf).collect(Collectors.toSet()));
        Set<Integer> usedLines = 
            present.stream().filter(e -> e instanceof BusBit)
                .map(e -> Integer.valueOf(((BusBit)e).lineNo)).collect(Collectors.toSet());
        allLines.removeAll(usedLines);
        if (allLines.size() == 0) return Optional.empty();
        int lineNo = allLines.stream().collect(Collectors.toList()).get(state.random[thread].nextInt(allLines.size()));
        BusBit bit = new BusBit();
        bit.children = new GPNode[] {};
        // bit.setup(state, bit.defaultBase());
        bit.lineNo = lineNo;
        return Optional.of(bit);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BusBit && ((BusBit)obj).lineNo == this.lineNo;
    }

    @Override
    public int hashCode() {
        return this.lineNo;
    }

    @Override
    public String name() {
        return "d";
    }

    @Override
    public String toStringForHumans() {
        return String.format("%s%d", this.name(), this.lineNo);
    }

    @Override
    public void resetNode(EvolutionState state, int thread) {
        int max = ((BoolProblem)state.evaluator.p_problem).busSize();
        this.lineNo = state.random[thread].nextInt(max);
    }

    @Override
    public boolean nodeEquals(GPNode node) {
        return (node instanceof BusBit) && (lineNo == ((BusBit)node).lineNo);
    }

    @Override
    public String encode() {
        return Code.encode(lineNo);
    }

    @Override
    public boolean decode(DecodeReturn dret) {
        int pos = dret.pos;
        String data = dret.data;
        Code.decode(dret);
        if (dret.type != DecodeReturn.T_INT) { 
            dret.data = data; 
            dret.pos = pos; 
            return false; 
        }
        lineNo = (int)dret.l;
        return true;
    }    

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual,
            Problem problem) {
        ((BoolData)input).out = ((BoolData)input).lines.get(this.lineNo); //NOTE: we do not clone here - ops should never attempt to modify bit set but create new one        
    }
}



