package ec.gp.transform;

import java.util.Map;

import ec.gp.GPNode;

public class Match {
    public final GPNode matched; 
    public final Map<GPNode, GPNode> bindings; 
    public final Transform transform;
    public Match(final Transform t, final GPNode matched, final Map<GPNode, GPNode> bindings) {
        this.matched = matched; 
        this.bindings = bindings; 
        this.transform = t; 
    }
}