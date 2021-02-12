package ec.gp.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;

public class Transform {

    public static class ReplaceFailed extends Exception {
        private static final long serialVersionUID = 1L;        
    }

    public static class Match {
        public final GPNode matched; 
        public final Map<GPNode, GPNode> bindings; 
        public final Transform transform;
        public Match(final Transform t, final GPNode matched, final Map<GPNode, GPNode> bindings) {
            this.matched = matched; 
            this.bindings = bindings; 
            this.transform = t; 
        }
    }

    // public final String name;
    public final GPNode pattern; 
    public final GPNode rewrite;
    public final String name;
    public Transform(String name, GPNode from, GPNode to) {
        // this.name = name;
        this.name = name;
        this.pattern = from;
        this.rewrite = to; 
    } 

    public static Transform of(String name, GPNode from, GPNode to) {
        return new Transform(name, from, to);
    }

    public static int getDepthOfRewrite(GPNode root, Map<GPNode, Integer> depthes) {
        if (depthes.containsKey(root)) {
            return depthes.get(root);
        } else {
            return 1 + Arrays.stream(root.children).map(ch -> getDepthOfRewrite(ch, depthes)).max(Comparator.comparingInt(d -> d)).orElse(0);
        }
    }

    //NOTE: GPNode has nodeEquivalentTo - but we need other definition 
    public static boolean nodesEquivalent(GPNode left, GPNode right) {
        if (left.getClass().equals(right.getClass())) {
            if (left instanceof ERC)
                return ((ERC)left).nodeEquals(right);
            return (left.children.length == right.children.length); //we ignore constraints)
        }
        return false;
    }
    public static boolean treesEquivalent(GPNode left, GPNode right, Map<GPNode, GPNode> bindings) {
        if (nodesEquivalent(left, right)) {
            for (int i = 0; i < left.children.length; i++) {
                if (!treesEquivalent(left.children[i], right.children[i], bindings))
                    return false; 
            }
            return true; 
        }
        else if (left.getClass().equals(Nodes.Any.class)) {
            //NOTE: we DO NOT implement full unification here - important
            if (bindings.containsKey(left)) {
                Map<GPNode, GPNode> emptyB = new HashMap<>();
                if (!treesEquivalent(bindings.get(left), right, emptyB))
                    return false;
            } else {
                bindings.put(left, right);
            }
            return true; 
        } else if (right.getClass().equals(Nodes.Any.class)) {
            if (bindings.containsKey(left)) {
                Map<GPNode, GPNode> emptyB = new HashMap<>();
                if (!treesEquivalent(bindings.get(right), left, emptyB))
                    return false;
            } else {
                bindings.put(right, left);
            }
            return  true; 
        }
        return false;
    }
    public void matches(GPNode ast, List<Match> m) {
        //returns binding of Any nodes to some subnodes on match 
        Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
        if (treesEquivalent(this.pattern, ast, bindings)) {
            m.add(new Match(this, ast, bindings)); 
        }
        for (GPNode child: ast.children) {
            matches(child, m);
        }
    }
    public List<Match> matches(GPNode ast) {
        List<Match> m = new ArrayList<Match>();
        matches(ast, m);
        return m;
    }
    public Match firstMatch(GPNode ast) {
        Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
        if (treesEquivalent(this.pattern, ast, bindings)) {
            return new Match(this, ast, bindings);
        }
        for (GPNode child: ast.children) {
            Match res = firstMatch(child);
            if (res != null)
                return res; 
        }
        return null;
    }
    private GPNode replace(GPNode target, Map<GPNode, GPNode> bindings, GPNodeParent parent, int argpos) {        
        if (bindings.containsKey(target)) {
            GPNode res = (GPNode)bindings.get(target).clone();
            res.parent = parent;
            res.argposition = (byte)argpos;
            return res;
        }
        for (int i = 0; i < target.children.length; i++) {            
            GPNode res = replace(target.children[i], bindings, target, i);
            target.children[i] = res; 
            res.parent = target;
        }
        target.argposition = (byte)argpos;
        target.parent = parent;
        return target;
    }

    public static boolean validate(GPNode node) {
        if (node instanceof Nodes.MetaNamed) {
            return false; 
        }
        for (GPNode child: node.children)
            if (!validate(child))
                return false; 
        return true; 
    }

    public GPNode gen(EvolutionState state, int thread, GPNode rewrite, Map<GPNode, GPNode> bindings, GPNodeParent parent, int argpos) {
        if (rewrite instanceof Nodes.Gen) {
            GPNode res = ((Nodes.Gen)rewrite).create(state, thread);            
            res.parent = parent;
            res.argposition = (byte)argpos;
            bindings.put(Nodes.Any(rewrite.toString()), res);
            return res;
        }
        for (int i = 0; i < rewrite.children.length; i++) {            
            GPNode res = gen(state, thread, rewrite.children[i], bindings, rewrite, i);
            rewrite.children[i] = res; 
            res.parent = rewrite;
        }
        rewrite.argposition = (byte)argpos;
        rewrite.parent = parent;
        return rewrite;

    }

    public GPNode replace(EvolutionState state, int thread, Match match, Map<GPNode, GPNode> external) throws ReplaceFailed {
        GPNode value = (GPNode)this.rewrite.clone();
        Map<GPNode, GPNode> bindings = match.bindings;
        if (external != null && external.size() > 0) {
            bindings.putAll(external);
        }
        value = gen(state, thread, value, bindings, null, 0); //should remove all Gen
        value = replace(value, bindings, match.matched.parent, match.matched.argposition);
        // value.parent = match.matched.parent;
        if (!validate(value))
            throw new ReplaceFailed();
        if (match.matched.parent instanceof GPNode) {
            ((GPNode)match.matched.parent).children[match.matched.argposition] = value; 
            return value; 
        } else if (match.matched.parent instanceof GPTree) {
            ((GPTree)match.matched.parent).child = value;             
            return value; 
        }
        throw new ReplaceFailed();
    }

}
