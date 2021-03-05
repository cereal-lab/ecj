package ec.gp.transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ec.EvolutionState;
import ec.display.EvolutionStateEvent;
import ec.gp.ERC;
import ec.gp.GPNode;
import ec.gp.GPNodeParent;
import ec.gp.GPTree;
import ec.util.MersenneTwisterFast;
import ec.util.Output;

public abstract class Strategy {

    public static class ReplaceFailed extends Exception {
        private static final long serialVersionUID = 1L;        
    }    

    protected Consumer<String> logger = msg -> {};
    protected StrategyStats stats = null;
    protected Map<GPNode, GPNode> externalContext = null;
    protected Set<Class<?>> commutativeFuncs;

    public static class TransformApplication {
        final public Transform transform;
        final public GPNode orig;
        final public GPNode replacement;

        public TransformApplication(Transform transform, GPNode orig, GPNode replacement) {
            this.transform = transform;
            this.orig = orig;
            this.replacement = replacement;
        }

        public void printTransform(EvolutionState state, int log) {
            state.output.println(this.transform.name + ":", log, true);
            orig.printRootedTreeForHumans(state, log, 0, 0);
            state.output.println("\n\t->", log, true);
            replacement.printRootedTreeForHumans(state, log, 0, 0);
            state.output.println("", log, true);
        }
    }

    public static class StrategyResult {
        public List<TransformApplication> appliedTransform;
        public GPNode gpNode;

        public StrategyResult(GPNode gpNode) { // original node on which strategy is applied
            this.appliedTransform = new ArrayList<TransformApplication>();
            this.gpNode = gpNode;
        }
    }

    public static class StrategyStats {
        public Map<String, Integer> stats = new HashMap<>();

        void add(String transform) {
            stats.put(transform, stats.getOrDefault(transform, 0) + 1);
        }
    }

    public void set(Consumer<String> logger) {
        this.logger = logger;
    }

    public void set(StrategyStats stats) {
        this.stats = stats;
    }

    public void set(Map<GPNode, GPNode> externalContext) {
        this.externalContext = externalContext;
    }

    public void set(Set<Class<?>> commutativeFuncs) {
        this.commutativeFuncs = commutativeFuncs;

    }

    public void set(Consumer<String> logger, StrategyStats stats, Map<GPNode, GPNode> externalContext,
        Set<Class<?>> commutativeFuncs) {
        set(logger); set(stats); set(externalContext); set(commutativeFuncs);
    }          
    //defines a way how to apply transforms 
    public StrategyResult apply(GPNode i, EvolutionState state, int thread) throws ReplaceFailed {
        StrategyResult res = new StrategyResult(i);
        apply(state, thread, res);
        return res;
    }
    public abstract void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed;
    // public StrategyResult apply(GPNode i, MersenneTwisterFast rand, Consumer<String> logger) throws ReplaceFailed {
    //     StrategyResult res = this.apply(i, rand);
    //     // logger.accept();
    // }

    private static abstract class StrategyCollection extends Strategy {
        protected Strategy[] strategies;

        public StrategyCollection(Strategy... strategies) {
            this.strategies = strategies;
        }

        @Override
        public void set(StrategyStats stats) {
            super.set(stats);
            Arrays.stream(strategies).forEach(s -> s.set(stats));
        }     
        
        @Override
        public void set(Consumer<String> logger) {
            super.set(logger);
            Arrays.stream(strategies).forEach(s -> s.set(logger));
        }          

        @Override
        public void set(Map<GPNode, GPNode> context) {
            super.set(context);
            Arrays.stream(strategies).forEach(s -> s.set(context));
        }

        @Override 
        public void set(Set<Class<?>> commutativeFuncs) {
            super.set(commutativeFuncs);
            Arrays.stream(strategies).forEach(s -> s.set(commutativeFuncs));
        }

    }

    public static Strategy All(Strategy... strategies) {
        return new StrategyCollection(strategies) {
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed{
                for (Strategy s: strategies) {
                    s.apply(state, thread, res);
                }
            }    
        };
    }

    public static Strategy First(Strategy... strategies) {
        return new StrategyCollection(strategies) {
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
                for (Strategy s: strategies) {
                    s.apply(state, thread, res);
                    if (res.appliedTransform.size() > 0) break;
                }    
            }   
        };
    }    
    
    // private static class Prob extends Strategy {

    //     private Strategy strategy;
    //     private double prob;

    //     public Prob(double prob, Strategy strategy) {
	// 		this.strategy = strategy;
	// 		this.prob = prob;
    //     }
    
    //     @Override
    //     public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
    //         if (state.random[thread].nextDouble() < prob)
    //             strategy.apply(state, thread, res);
    //     }    
    // } 

    // public static Strategy Prob(double prob, Strategy strategy) {
    //     return new Prob(prob, strategy);
    // }       
    
    // private static class If extends Strategy {

    //     private Strategy strategy;
    //     private Predicate<GPNode> pred;

    //     public If(Predicate<GPNode> pred, Strategy strategy) {
    //         this.pred = pred;
    //         this.strategy = strategy;
    //     }
    
    //     @Override
    //     public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
    //         if (pred.test(res.gpNode))
    //             strategy.apply(state, thread, res);
    //     }    
    // }
    
    // public static Strategy If(Predicate<GPNode> pred, Strategy strategy) {
    //     return new If(pred, strategy);
    // }             

    public static Strategy Fixpoint(Strategy... other) {
        return new StrategyCollection(other){
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {    
                StrategyResult innerRes = new StrategyResult(res.gpNode);
                while (true) {                
                    innerRes.appliedTransform.clear();
                    for (Strategy s: strategies) {
                        s.apply(state, thread, innerRes);
                    }
                    if (innerRes.appliedTransform.size() == 0) break;
                    res.appliedTransform.addAll(innerRes.appliedTransform);
                }
            }            
        };
    }

    public static Strategy Any(Strategy... other) {
        return new StrategyCollection(other) {
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {    
                List<Strategy> awailable = new ArrayList<>(List.of(strategies));
                while (awailable.size() > 0) {
                    Strategy str = awailable.get(state.random[thread].nextInt(awailable.size()));
                    // logger.accept(String.format("strategy [%s] ", args));
                    str.apply(state, thread, res);
                    if (res.appliedTransform.size() > 0)
                        break;
                    else 
                        awailable.remove(str);
                }
            }            
        };
    }

    public static interface CaseTest {
        public boolean test(EvolutionState state, int thread, GPNode node, TreeStats stats);
    }

    public static interface ERCCaseTest {
        public Optional<ERC> test(EvolutionState state, int thread, Set<ERC> stats);
    }    

    public static class Case {
        public final CaseTest condition;
        public final Strategy reaction;
        public boolean noBreak = false;

        public Case(CaseTest condition, Strategy reaction) {
            this.condition = condition;
            this.reaction = reaction;
        }

        public Case(CaseTest condition, Strategy reaction, boolean noBreak) {
            this(condition, reaction);
            this.noBreak = noBreak;
        }
    }

    public static Case Missing(Class<?> nodeClass, Strategy reaction) {
        return new Case((st, t, n, s) -> (s.nodeCounts.getOrDefault(nodeClass, 0) == 0), reaction);
    }

    public static Case MissingERC(ERCCaseTest ercTest, String newName, Strategy reaction) {        
        return new Case((st, t, n, s) -> {
            Optional<ERC> created = ercTest.test(st, t, s.ercs);
            if (created.isPresent()) {
                reaction.set(Map.of(Nodes.Any(newName), created.get()));
            }
            return created.isPresent();
        }, reaction, true);
    }

    private static StrategyCollection Id(Strategy strategy) {
        return new StrategyCollection(strategy){
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {   
                if (strategies[0] != null)
                    strategies[0].apply(state, thread, res);
            }
        };
    }

    public static Case AnyCase(Case... cases) {
        StrategyCollection selectedStrategy = Id(null);
        return new Case((st, t, n, s) -> {
            List<Case> allCases = new ArrayList<>(List.of(cases));
            while (allCases.size() > 0) {
                Case selectedCase = allCases.get(st.random[t].nextInt(allCases.size()));
                if (selectedCase.condition.test(st, t, n, s)) {
                    selectedStrategy.strategies[0] = selectedCase.reaction;
                    return true;
                } else 
                    allCases.remove(selectedCase);
            }  
            return false;  
        }, selectedStrategy);
    }

    public static Case Depth(int depth, Strategy reaction) {
        return new Case((st, t, n, s) -> n.depth() > depth, reaction, true);
    }

    public static Case Default(Strategy defaultStr) {
        return new Case((st, t, n,s) -> true, defaultStr);
    }
    public static class TreeStats {

        Map<Class<?>, Integer> nodeCounts = new HashMap<Class<?>, Integer>(); 
        Set<ERC> ercs = new HashSet<ERC>();
        int size = 0;

        private void collect(GPNode i) {
            size++;
            if (i instanceof ERC) {
                ercs.add((ERC)i);
            } else {
                Class<?> cl = i.getClass();
                Integer count = nodeCounts.getOrDefault(cl, 0);
                nodeCounts.put(cl, count + 1);                
                for (GPNode child: i.children) {
                    collect(child);
                }
            }
        }     
        
        public static TreeStats collectFor(GPNode i) {
            TreeStats stats = new TreeStats();
            stats.collect(i);
            return stats;
        }
    }

    public static Strategy On(Case... cases) { //for now work with stats is hardcoded 
        return new StrategyCollection(Arrays.stream(cases).map(c -> c.reaction).toArray(i -> new Strategy[i])) {
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
                TreeStats stats = TreeStats.collectFor(res.gpNode);
                for (Case oneCase: cases) {
                    if (oneCase.condition.test(state, thread, res.gpNode, stats)) {
                        oneCase.reaction.apply(state, thread, res);
                        if (!oneCase.noBreak)
                            break;
                    }
                }                
            }
        };
    }

    // private static class Fallback extends Strategy {

    //     private Strategy[] strategies;

    //     public Fallback(Strategy... strategies) {
    //         this.strategies = strategies;
    //     }    
        
	// 	@Override
    //     public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {    
    //         Strategy str = strategies[rand.nextInt(strategies.length)];
    //         return str.apply(i, rand);
    //     }
    // }

    // public static Strategy Any(Strategy... other) {
    //     return new Any(other);
    // }   
    
    // private static class TopDownAny extends Strategy {
    //     //goes in topdown fashion and with probability applies transform to any match 

    //     private final Transform[ ] transforms;
    //     private final double prob; 
    //     public TopDownAny(double prob, Transform... ts) {
    //         this.transforms = ts; 
    //         this.prob = prob;
    //     }

    //     public GPNode matchReplace(GPNode ast, MersenneTwisterFast rand, StrategyResult res) throws ReplaceFailed {
    //         //returns binding of Any nodes to some subnodes on match             
    //         if (rand.nextDouble() < this.prob) { //replace 
    //             //treesEquivalent(this.pattern, ast, bindings)
    //             List<Transform.Match> matches = new ArrayList<>();
    //             for (Transform t: this.transforms) {
    //                 Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
    //                 if (Transform.treesEquivalent(t.pattern, ast, bindings)) {
    //                     matches.add(new Transform.Match(t, ast, bindings));
    //                 }
    //             }
    //             if (matches.size() > 0)
    //             {
    //                 Transform.Match m = matches.get(rand.nextInt(matches.size()));
    //                 Transform t = m.transform;
    //                 GPNode value = (GPNode)t.rewrite.clone();
    //                 value = t.replace(value, m.bindings, ast.parent, ast.argposition);
    //                 // value.parent = match.matched.parent;
    //                 if (!Transform.validate(value))
    //                     throw new ReplaceFailed();
    //                 if (ast.parent instanceof GPNode) {
    //                     ((GPNode)ast.parent).children[ast.argposition] = value; 
    //                     // return value; 
    //                 } else if (ast.parent instanceof GPTree) {
    //                     ((GPTree)ast.parent).child = value;             
    //                     // return value; 
    //                 }
    //                 // throw new Strategy.ReplaceFailed();
    //                 res.appliedTransform.add(new TransformApplication(t, m.matched, value));
    //                 ast = value;                                
    //             }
    //         }
    //         for (GPNode child: ast.children) {
    //             matchReplace(child, rand, res);
    //         }
    //         return ast;
    //     }
        
    //     @Override
    //     public void apply(MersenneTwisterFast rand, StrategyResult res) throws ReplaceFailed {
    //         //default implementation applies transform to random match 
    //         this.matchReplace(res.gpNode, rand, res); //, matches);
    //     }
    // }

    // public static Strategy TopDownAny(double prob, Transform... ts) {
    //     return new TopDownAny(prob, ts);
    // }
    
    // private static class AnyMatch extends Strategy {

    //     public AnyMatch(Transform rule) {
    //         super(rule);
    //     }

    //     // @Override
    //     // public String toString() {
    //     //     this.pattern.print
    //     // }

    //     @Override
    //     public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
    //         //default implementation applies transform to random match 
    //         List<Transform.Match> m = this.matches(i); //, matches);
    //         if (m.size() == 0) return new StrategyResult(false, i);
    //         Transform.Match selectedRewrite = m.get(rand.nextInt(m.size()));
    //         i = selectedRewrite.transform().replace(selectedRewrite);
    //         return new StrategyResult(true, i);
    //     }
    // }

    // public static Transform AnyMatch(Transform rule) {
    //     return new AnyMatch(rule);
    // }

    public static abstract class TransformApplier extends Strategy {
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

        private GPNode gen(EvolutionState state, int thread, GPNode rewrite, Map<GPNode, GPNode> bindings, GPNodeParent parent, int argpos) {
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
        
        private boolean validate(GPNode node) {
            if (node instanceof Nodes.MetaNamed) {
                return false; 
            }
            for (GPNode child: node.children)
                if (!validate(child))
                    return false; 
            return true; 
        }

        public GPNode replace(GPNode replacement, EvolutionState state, int thread, Match match, Map<GPNode, GPNode> external) throws ReplaceFailed {
            GPNode value = (GPNode)replacement.clone();
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

        // public abstract List<Match> matchPattern(Transform pattern, GPNode ast);
    }

    private static class AnyMatch extends TransformApplier {

        private Transform[] transforms;

        public AnyMatch(Transform... transforms) {
            this.transforms = transforms;
        }

        @Override
        public void set(StrategyStats stats) {
            super.set(stats);
            for (Transform t: this.transforms) {
                if (!this.stats.stats.containsKey(t.name))
                    this.stats.stats.put(t.name, 0);
            }
        }
        
        private void matches(Transform t, GPNode ast, List<Match> m, MersenneTwisterFast rand) {
            //returns binding of Any nodes to some subnodes on match 
            Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
            if (Transform.unify(t.pattern, ast, this.commutativeFuncs, bindings, rand)) {
                m.add(new Match(t, ast, bindings)); 
            }
            for (GPNode child: ast.children) {
                matches(t, child, m, rand);
            }
        }

        // public List<Match> matchPattern(Transform t, GPNode ast) {
        //     List<Match> m = new ArrayList<Match>();
        //     matches(t, ast, m);
        //     return m;
        // }    

        @Override
        public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
            //default implementation applies transform to random match 
            List<Match> matches = new ArrayList<>();
            for (Transform t: transforms) {
                List<Match> localMatches = new ArrayList<>();
                // logger.accept(String.format("[%s]", t.name));
                matches(t, res.gpNode, localMatches, state.random[thread]);
                matches.addAll(localMatches);  
                if (localMatches.size() > 0) {
                    logger.accept(String.format("[%s] %d matches", t.name, localMatches.size()));           
                }
            }
            while (matches.size() > 0) {
                Match selectedRewrite = matches.get(state.random[thread].nextInt(matches.size()));                 
                Map<GPNode, Integer> depthes = selectedRewrite.bindings.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().depth()));
                int depth = Transform.getDepthOfRewrite(selectedRewrite.transform.rewrite, depthes);
                if (depth <= 12) { //constant here for now - TODO: refactor to selection strategy                 
                    logger.accept(String.format("selected [%s]", selectedRewrite.transform.name)); 
                    GPNode newChild = this.replace(selectedRewrite.transform.rewrite, state, thread, selectedRewrite, this.externalContext);
                    if (res.gpNode.equals(selectedRewrite.matched))
                        res.gpNode = newChild;                    
                    res.appliedTransform.add(new TransformApplication(selectedRewrite.transform, selectedRewrite.matched, newChild));
                    if (this.stats != null) 
                        stats.add(selectedRewrite.transform.name);                    
                    break;
                } else {
                    matches.remove(selectedRewrite);
                }
            }
        }
    }
    
    public static Strategy AnyMatch(Transform... transforms) {
        return new AnyMatch(transforms);
    }

    // private static class TopDown extends Transform {
    //     //goes in topdown fashion and with probability applies transform to any match 

    //     private final double prob; 
    //     public TopDown(RewriteRule rule, double prob) {
    //         super(rule);
    //         this.prob = prob;
    //     }

    //     public GPNode matchReplace(GPNode ast, MersenneTwisterFast rand, StrategyResult res) throws ReplaceFailed {
    //         //returns binding of Any nodes to some subnodes on match 
    //         Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
    //         if ((rand.nextDouble() < this.prob) && treesEquivalent(this.pattern, ast, bindings)) { //replace 
    //             GPNode value = (GPNode)this.rewrite.clone();
    //             value = replace(value, bindings, ast.parent, ast.argposition);
    //             // value.parent = match.matched.parent;
    //             if (!validate(value))
    //                 throw new Strategy.ReplaceFailed();
    //             if (ast.parent instanceof GPNode) {
    //                 ((GPNode)ast.parent).children[ast.argposition] = value; 
    //                 // return value; 
    //             } else if (ast.parent instanceof GPTree) {
    //                 ((GPTree)ast.parent).child = value;             
    //                 // return value; 
    //             }
    //             // throw new Strategy.ReplaceFailed();
    //             ast = value;            
    //             res.wasModified = true;
    //         }
    //         for (GPNode child: ast.children) {
    //             matchReplace(child, rand, res);
    //         }
    //         return ast;
    //     }
    //     // public List<Match> matchReplace(GPNode ast) {
    //     //     List<Match> m = new ArrayList<Match>();
    //     //     matchReplace(ast, m);
    //     //     return m;
    //     // }        

    //     // @Override
    //     // public String toString() {
    //     //     this.pattern.print
    //     // }

    //     @Override
    //     public Strategy.StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {
    //         //default implementation applies transform to random match 
    //         StrategyResult res = new StrategyResult(false, i);
    //         res.newNode = this.matchReplace(i, rand, res); //, matches);
    //         return res;
    //     }
    // }

    // public static Transform TopDown(RewriteRule rule, double prob) {
    //     return new TopDown(rule, prob);
    // }

    public static  Strategy NTimesMax(int max, Strategy s) {
        return new StrategyCollection(s) {
            @Override
            public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
                int times = (max <= 1) ? 0 : state.random[thread].nextInt(max);
                for (int i = 0; i <= times; i++) {
                    strategies[0].apply(state, thread, res);
                }
            }             
        };
    }

    private static class FirstMatch extends TransformApplier {

        private Transform[] rules;

        public FirstMatch(Transform... rules) {
            this.rules = rules;
        }           

        private Match firstTopDownMatch(Transform t, GPNode ast, MersenneTwisterFast rand) {
            Map<GPNode, GPNode> bindings = new HashMap<GPNode, GPNode>();
            if (Transform.unify(t.pattern, ast, this.commutativeFuncs, bindings, rand)) {
                return new Match(t, ast, bindings);
            }
            for (GPNode child: ast.children) {
                Match res = firstTopDownMatch(t, child, rand);
                if (res != null)
                    return res; 
            }
            return null;
        }            

        @Override
        public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {
            for (Transform rule: rules) {
                Match firstMatch = firstTopDownMatch(rule, res.gpNode, state.random[thread]);
                if (firstMatch == null)
                    continue;
                GPNode newChild = this.replace(firstMatch.transform.rewrite, state, thread, firstMatch, this.externalContext);
                if (res.gpNode.equals(firstMatch.matched))
                    res.gpNode = newChild;
                res.appliedTransform.add(new TransformApplication(firstMatch.transform, firstMatch.matched, newChild));                
            }
        } 
    }

    public static Strategy FirstMatch(Transform... rules) {
        return new FirstMatch(rules);
    }

    // private static class TwoWayRand extends Strategy {
    //     AnyMatch backward;
    //     AnyMatch forward;
    //     private double prob;

    //     public TwoWayRand(double prob, Transform... rules) {
    //         this.prob = prob;
    //         this.forward = new AnyMatch(rules);
    //         this.backward = new AnyMatch(Arrays.stream(rules).map(r -> Transform.of(r.name + "-rev", r.rewrite, r.pattern)).toArray(Transform[]::new));           
	// 	}

	// 	@Override
    //     public void apply(EvolutionState state, int thread, StrategyResult res) throws ReplaceFailed {    
    //         if (state.random[thread].nextDouble() < prob) 
    //             forward.apply(state, thread, res);
    //         else 
    //             backward.apply(state, thread, res);
    //     }    
    // }

    // public static Strategy TwoWayRand(double prob, Transform... rules) {
    //     return new TwoWayRand(prob, rules);
    // }          

}
