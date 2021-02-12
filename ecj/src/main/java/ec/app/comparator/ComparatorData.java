package ec.app.comparator;

import ec.gp.GPData;

public class ComparatorData extends GPData {
    public int x;

    public void copyTo(final GPData gpd) 
        { ((ComparatorData)gpd).x = x; }
    
}
