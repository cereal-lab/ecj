package ec.domain.bool;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BitSet extends java.util.BitSet {

    private int realSize;
    private boolean finalized; 

    public BitSet(int sz) {
        super(sz);
        this.realSize = sz;
    }

    public void finalize() {
        this.finalized = true;
    }

    public int realSize() {
        return realSize;
    }

    @Override
    public void and(java.util.BitSet set) {
        if (this.finalized) throw new IllegalStateException();
        super.and(set);
    }

    @Override
    public void andNot(java.util.BitSet set) {
        if (this.finalized) throw new IllegalStateException();
        super.andNot(set);
    }

    @Override    
    public void clear() {
        if (this.finalized) throw new IllegalStateException();
        super.clear();
    }

    @Override
    public void clear(int bitIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.clear(bitIndex);
    }

    @Override
    public void clear(int fromIndex, int toIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.clear(fromIndex, toIndex);
    }

    @Override
    public void flip(int bitIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.flip(bitIndex);
    }

    @Override
    public void flip(int fromIndex, int toIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.flip(fromIndex, toIndex);
    }

    @Override
    public void or(java.util.BitSet set) {
        if (this.finalized) throw new IllegalStateException();
        super.or(set);
    }

    @Override
    public void set(int bitIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.set(bitIndex);
    }

    @Override
    public void set(int bitIndex, boolean value) {
        if (this.finalized) throw new IllegalStateException();
        super.set(bitIndex, value);
    }

    @Override
    public void set(int fromIndex, int toIndex) {
        if (this.finalized) throw new IllegalStateException();
        super.set(fromIndex, toIndex);
    }

    @Override
    public void set(int fromIndex, int toIndex, boolean value) {
        if (this.finalized) throw new IllegalStateException();
        super.set(fromIndex, toIndex, value);
    }

    @Override
    public void xor(java.util.BitSet set) {
        if (this.finalized) throw new IllegalStateException();
        super.xor(set);
    }

    public String toBin() {
        return IntStream.range(0, realSize).mapToObj(i -> this.get(i) ? "1" : "0").collect(Collectors.joining());
    }

    @Override
    public Object clone() {
        BitSet newObj = (BitSet)super.clone();
        newObj.finalized = false; 
        return newObj;
    }

}
