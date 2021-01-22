package ec.app.multiplexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.util.MersenneTwisterFast;

public abstract class Transform extends Strategy {

    public class Match {
        public final GPNode matched; 
        public final Map<GPNode, GPNode> bindings; 
        public Match(final GPNode matched, final Map<GPNode, GPNode> bindings) {
            this.matched = matched; 
            this.bindings = bindings; 
        }
        public Transform transform() {
            return Transform.this;
        }
    }

    // public final String name;
    public final GPNode pattern; 
    public final GPNode rewrite;
    public Transform(final GPNode from, final GPNode to) {
        // this.name = name;
        this.pattern = from;
        this.rewrite = to; 
    } 
    //NOTE: GPNode has nodeEquivalentTo - but we need other definition 
    public boolean nodesEquivalent(GPNode left, GPNode right) {
        return left.getClass().equals(right.getClass()) && left.children.length == right.children.length; //we ignore constraints
    }
    public boolean treesEquivalent(GPNode left, GPNode right, Map<GPNode, GPNode> bindings) {
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
            m.add(new Match(ast, bindings)); 
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
    public Match firstMatch(GPNode ast, GPNode ptn) {
        Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
        if (treesEquivalent(ptn, ast, bindings)) {
            return new Match(ast, bindings);
        }
        for (GPNode child: ast.children) {
            Match res = firstMatch(child, ptn);
            if (res != null)
                return res; 
        }
        return null;
    }
    public GPNode replace(GPNode target, Map<GPNode, GPNode> bindings, GPNodeParent parent, int argpos) {
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
    // public void fixArgposForChildren(GPNode node) {
    //     for (int i = 0; i < node.children.length; i++) {
    //         node.children[i].argposition = (byte)i;
    //         fixArgposForChildren()
    //     }
    // }
    public boolean validate(GPNode node) {
        if (node.getClass().equals(Nodes.Any.class)) {
            return false; 
        }
        for (GPNode child: node.children)
            if (!validate(child))
                return false; 
        return true; 
    }
    public GPNode replace(Match match) throws Strategy.ReplaceFailed {
        GPNode value = (GPNode)this.rewrite.clone();
        value = replace(value, match.bindings, match.matched.parent, match.matched.argposition);
        // value.parent = match.matched.parent;
        if (!validate(value))
            throw new Strategy.ReplaceFailed();
        if (match.matched.parent instanceof GPNode) {
            ((GPNode)match.matched.parent).children[match.matched.argposition] = value; 
            return value; 
        } else if (match.matched.parent instanceof GPTree) {
            ((GPTree)match.matched.parent).child = value;             
            return value; 
        }
        throw new Strategy.ReplaceFailed();
    }

    // public boolean perform(GPNode ast, MersenneTwisterFast rand) throws ReplaceFailed {
    //     List<Match> m = matches(ast);
    //     if (m.size() > 0) {
    //         Match randM = m.get(rand.nextInt(m.size()));
    //         replace(randM);
    //         return true;
    //     }
    //     return false;
    // }

    private static class AnyMatch extends Transform {

        public AnyMatch(GPNode from, GPNode to) {
            super(from, to);
        }

        // @Override
        // public String toString() {
        //     this.pattern.print
        // }

        @Override
        public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
            //default implementation applies transform to random match 
            List<Transform.Match> m = this.matches(i); //, matches);
            if (m.size() == 0) return new StrategyResult(false, i);
            Transform.Match selectedRewrite = m.get(rand.nextInt(m.size()));
            i = selectedRewrite.transform().replace(selectedRewrite);
            return new StrategyResult(true, i);
        }
    }

    public static Transform AnyMatch(GPNode from, GPNode to) {
        return new AnyMatch(from, to);
    }

    private static class AnyOfMatches extends Strategy {

        private Transform[] transforms;

        public AnyOfMatches(Transform... transforms) {
            this.transforms = transforms;
        }

        @Override
        public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
            //default implementation applies transform to random match 
            List<Transform.Match> matches = new ArrayList<>();
            for (Transform t: transforms) {
                t.matches(i, matches);
            }
            if (matches.size() == 0)
                return new StrategyResult(false, i);
            Transform.Match selectedRewrite = matches.get(rand.nextInt(matches.size()));
            i = selectedRewrite.transform().replace(selectedRewrite);
            return new StrategyResult(true, i);
        }        
    }
    
    public static Strategy AnyOfMatches(Transform... transforms) {
        return new AnyOfMatches(transforms);
    }

    private static class FirstMatch extends Transform {

        public FirstMatch(GPNode from, GPNode to) {
            super(from, to);
        }

        @Override
        public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
            Transform.Match firstMatch = this.firstMatch(i, this.pattern);
            if (firstMatch == null)
                return new StrategyResult(false, i);
            i = firstMatch.transform().replace(firstMatch);
            return new StrategyResult(true, i);    
        } 
    }

    public static Transform FirstMatch(GPNode from, GPNode to) {
        return new FirstMatch(from, to);
    }

    private static class IfAbsent extends Transform {

        private GPNode presentPattern;
        private Transform transform;

        public IfAbsent(GPNode presentPattern, GPNode from, GPNode to) {
            super(from, to);
            this.presentPattern = presentPattern;
            this.transform = Transform.AnyMatch(from, to);
        }

        @Override
        public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
            //default implementation applies transform to random match 
            Transform.Match m = this.firstMatch(i, this.presentPattern);
            if (m == null) {
                return transform.apply(i, rand);
            }
            return new StrategyResult(false, i);
        }        
    }

    public static Transform IfAbsent(GPNode presentPattern, GPNode from, GPNode to) {
        return new IfAbsent(presentPattern, from, to);
    }

    private static class TwoWayRand extends Strategy {
        Transform backward;
        Transform forward;
        private double prob;

        public TwoWayRand(double prob, GPNode from, GPNode to) {
            this.prob = prob;
            this.forward = Transform.AnyMatch(from, to);
            this.backward = Transform.AnyMatch(to, from);            
		}

		@Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {    
            if (rand.nextDouble() < prob) 
                return forward.apply(i, rand);
            else 
                return backward.apply(i, rand);
        }    
    }

    public static Strategy TwoWayRand(double prob, GPNode left, GPNode right) {
        return new TwoWayRand(prob, left, right);
    }      

}
