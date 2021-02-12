package ec.domain.bool;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ec.gp.GPData;

public class BoolData extends GPData {
    private static final long serialVersionUID = 1L;
    public List<BitSet> lines; // one element is BitSet of one line d0, d1, ... dN
    //NOTE: BitSets should be initied at start of evaluation in the experiment according to test cases
    public BitSet out; //output for all test cases     

    @Override
    public Object clone() {
        BoolData newBoolData = new BoolData();
        //lines should be readonly!!!!!
        newBoolData.lines = this.lines;  //this.lines.stream().map(line -> (BitSet)line.clone()).collect(Collectors.toList());
        newBoolData.out = (BitSet)this.out.clone();
        return newBoolData;
    }

    @Override
    public void copyTo(final GPData gpd) {  //shallow copy
        ((BoolData)gpd).lines = this.lines; 
        ((BoolData)gpd).out = this.out; 
    }

    public BoolData initTestCases(int busSize) {
        BoolData data = this;
        int testSetSize = 1 << busSize;
        data.lines = new ArrayList<>();
        data.out = new ec.domain.bool.BitSet(testSetSize);
        for (int i = 0; i < busSize; i++) {
            data.lines.add(new ec.domain.bool.BitSet(testSetSize));
        }
        for (int i = 0; i < testSetSize; i++) {            
            for (int j = 0; j < busSize; j++) {
                data.lines.get(j).set(i, ((i >>> j) & 1) == 1);
            }
        }

        data.lines.forEach(bs -> ((ec.domain.bool.BitSet)bs).finalize());        
        return data;
    }

}
