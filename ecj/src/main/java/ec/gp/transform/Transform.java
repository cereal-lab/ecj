package ec.gp.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.ERC;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.util.MersenneTwisterFast;

public class Transform {

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
    
    private static List<List<GPNode>> permute(List<GPNode> args) {
        List<List<GPNode>> ret = new ArrayList<List<GPNode>>();
        if (0 == args.size()) {
            ret.add(new ArrayList<GPNode>());
        } else {
            GPNode headArg = args.remove(0);
            List<List<GPNode>> prevRes = permute(args);
            for (List<GPNode> perm : prevRes) {
                for (int index=0; index <= perm.size(); index++) {
                    List<GPNode> temp = new ArrayList<>(perm);
                    temp.add(index, headArg);
                    ret.add(temp);
                  }
            }
        }
        return ret;
    }

    private static List<List<GPNode>> shuffle(List<List<GPNode>> permutations, MersenneTwisterFast rand) {
        permutations.sort(Comparator.comparing(t -> rand.nextDouble()));
        return permutations;
    }

    public static boolean unify(GPNode left, GPNode right, Set<Class<?>> commutative, 
                                Map<GPNode, GPNode> bindings,
                                MersenneTwisterFast rand) {
        if (nodesEquivalent(left, right)) {
            if (commutative != null && commutative.contains(left.getClass())) {
                for (List<GPNode> perm: shuffle(permute(new ArrayList<>(List.of(right.children))), rand)) {
                    boolean continueOuter = false;
                    Map<GPNode, GPNode> localBinds = new HashMap<GPNode, GPNode>(bindings);
                    for (int i = 0; i < left.children.length; i++) {
                        if (!unify(left.children[i], perm.get(i), commutative, localBinds, rand)) {
                            continueOuter = true; 
                            break;
                        }
                    }
                    if (continueOuter) {
                        continueOuter = false; 
                        continue;
                    }
                    bindings.putAll(localBinds);
                    return true;
                }
            } else {
                for (int i = 0; i < left.children.length; i++) {
                    if (!unify(left.children[i], right.children[i], commutative, bindings, rand))
                        return false; 
                }
                return true; 
            }            
        }
        else if (left.getClass().equals(Nodes.Any.class)) {
            //NOTE: we DO NOT implement full unification here - important
            if (bindings.containsKey(left)) {
                Map<GPNode, GPNode> emptyB = new HashMap<>();
                if (!unify(bindings.get(left), right, commutative, emptyB, rand))
                    return false;
            } else {
                bindings.put(left, right);
            }
            return true; 
        } else if (right.getClass().equals(Nodes.Any.class)) {
            if (bindings.containsKey(left)) {
                Map<GPNode, GPNode> emptyB = new HashMap<>();
                if (!unify(bindings.get(right), left, commutative, emptyB, rand))
                    return false;
            } else {
                bindings.put(right, left);
            }
            return  true; 
        }
        return false;
    }
}
