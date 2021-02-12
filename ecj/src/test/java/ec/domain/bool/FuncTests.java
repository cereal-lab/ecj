package ec.domain.bool;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ec.EvolutionState;
import ec.Evolve;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPTree;
import ec.util.MersenneTwisterFast;
import ec.util.Parameter;
import ec.util.ParameterDatabase;

public class FuncTests {

    private final static Parameter BASE = new Parameter("base");
    private ParameterDatabase params;
    private EvolutionState state;
    // private BoolData input;

    
    @Before
    public void setUp()
    {
        params = new ParameterDatabase();
        // params.set(new Parameter(Initializer.P_POP).push(Population.P_SUBPOP).push("0").push(Subpopulation.P_SPECIES).push(BBOBenchmarks.P_GENOME_SIZE), "10");
        // params.set(BASE.push(BBOBenchmarks.P_NOISE), "none");
        // params.set(BASE.push(BBOBenchmarks.P_REEVALUATE_NOISY_PROBLEMS), "true");
        state = new EvolutionState();
        state.parameters = params;
        state.output = Evolve.buildOutput();
        state.output.setThrowsErrors(true);
        state.random = new MersenneTwisterFast[] { new MersenneTwisterFast() };
    }

    @Test 
    public void AndTest() {
        BoolData data = new BoolData().initTestCases(2);
        GPNode andNode = new And();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        andNode.children = new GPNode[] { bit0, bit1 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(4);
        expectedOut.set(3);
        assertTrue(data.out.equals(expectedOut));
    }

    @Test 
    public void OrTest() {
        BoolData data = new BoolData().initTestCases(2);
        GPNode andNode = new Or();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        andNode.children = new GPNode[] { bit0, bit1 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(4);
        expectedOut.set(1,4);
        assertTrue(data.out.equals(expectedOut));
    }

    @Test 
    public void XorTest() {
        BoolData data = new BoolData().initTestCases(2);
        GPNode andNode = new Xor();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        andNode.children = new GPNode[] { bit0, bit1 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(4);
        expectedOut.set(1,3);
        assertTrue(data.out.equals(expectedOut));
    }

    @Test 
    public void NandTest() {
        BoolData data = new BoolData().initTestCases(2);
        GPNode andNode = new Nand();    
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        andNode.children = new GPNode[] { bit0, bit1 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(4);
        expectedOut.set(0, 3);
        assertTrue(data.out.equals(expectedOut));
    }    

    @Test 
    public void NorTest() {
        BoolData data = new BoolData().initTestCases(2);
        GPNode andNode = new Nor();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        andNode.children = new GPNode[] { bit0, bit1 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(4);
        expectedOut.set(0);
        assertTrue(data.out.equals(expectedOut));
    }     
    
    @Test 
    public void NotTest() {
        BoolData data = new BoolData().initTestCases(1);
        GPNode andNode = new Not();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        andNode.children = new GPNode[] { bit0 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(2);
        expectedOut.set(0);
        assertTrue(data.out.equals(expectedOut));
    }     

    @Test 
    public void IfTest() {
        BoolData data = new BoolData().initTestCases(3);
        GPNode andNode = new If();
        GPIndividual ind = new GPIndividual();
        // ind.setup(state, base);
        ind.trees = new GPTree[] { new GPTree() };
        ind.trees[0].child = andNode;
        BusBit bit0 = new BusBit();
        bit0.lineNo = 0;
        BusBit bit1 = new BusBit();
        bit1.lineNo = 1;
        BusBit bit2 = new BusBit();
        bit2.lineNo = 2;
        andNode.children = new GPNode[] { bit0, bit1, bit2 };
        andNode.eval(state, 0, data, null, ind, new BoolProblem());
        BitSet expectedOut = new BitSet(8);
        expectedOut.set(3,5);
        expectedOut.set(6, 8);
        assertTrue(data.out.equals(expectedOut));
    }         
    
}
