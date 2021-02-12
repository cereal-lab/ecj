package ec.app.majorityBits;

import ec.gp.GPData;

public class MajorityData extends GPData {
    public int x;

    public void copyTo(final GPData gpd) 
        { ((MajorityData)gpd).x = x; }
    
}
