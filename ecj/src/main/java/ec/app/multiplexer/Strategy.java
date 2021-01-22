package ec.app.multiplexer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import ec.gp.GPNode;
import ec.util.MersenneTwisterFast;

public abstract class Strategy {

    public class ReplaceFailed extends Exception {
        private static final long serialVersionUID = 1L;        
    }

    public static class StrategyResult {
        final public boolean wasModified; 
        final public GPNode newNode;
        public StrategyResult(boolean wasModified, GPNode newNode) {
            this.wasModified = wasModified;
            this.newNode = newNode;
        }
    }
    //defines a way how to apply transforms 
    public abstract StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws ReplaceFailed;

    private static class All extends Strategy {

        private Strategy[] strategies;

        public All(Strategy... strategies) {
            this.strategies = strategies;            
        }
    
        @Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws ReplaceFailed{
            StrategyResult res = new StrategyResult(false, i);
            for (Strategy s: strategies) {
                res = s.apply(i, rand);
                i = res.newNode;
            }    
            return res;
        }    
    }    

    public static Strategy All(Strategy... strategies) {
        return new All(strategies);
    }

    private static class First extends Strategy {

        private Strategy[] strategies;

        public First(Strategy... strategies) {
            this.strategies = strategies;            
        }
    
        @Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws ReplaceFailed {
            StrategyResult res = new StrategyResult(false, i);
            for (Strategy s: strategies) {
                res = s.apply(i, rand);
                if (res.wasModified) break;
            }    
            return res;
        }    
    }  

    public static Strategy First(Strategy... strategies) {
        return new First(strategies);
    }    
    
    private static class Prob extends Strategy {

        private Strategy strategy;
        private double prob;

        public Prob(double prob, Strategy strategy) {
			this.strategy = strategy;
			this.prob = prob;
        }
    
        @Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws ReplaceFailed {
            if (rand.nextDouble() < prob)
                return strategy.apply(i, rand);
            return new StrategyResult(false, i);
        }    
    } 

    public static Strategy Prob(double prob, Strategy strategy) {
        return new Prob(prob, strategy);
    }       
    
    private static class If extends Strategy {

        private Strategy strategy;
        private Predicate<GPNode> pred;

        public If(Predicate<GPNode> pred, Strategy strategy) {
            this.pred = pred;
            this.strategy = strategy;
        }
    
        @Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws ReplaceFailed {
            if (pred.test(i))
                return strategy.apply(i, rand);
            return new StrategyResult(false, i);
        }    
    }
    
    public static Strategy If(Predicate<GPNode> pred, Strategy strategy) {
        return new If(pred, strategy);
    }     
        
    private static class Fixpoint extends Strategy {

        private Strategy[] strategies;

        public Fixpoint(Strategy... strategies) {
            this.strategies = strategies;
		}

		@Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {    
            StrategyResult res = new StrategyResult(false, i);
            boolean wasModified = false;
            while (true) {
                wasModified = false;
                for (Strategy s: strategies) {
                    res = s.apply(i, rand);
                    i = res.newNode;
                    wasModified = wasModified || res.wasModified;
                }
                if (!wasModified) break;
            }
            return res;
        }    
    } 

    public static Strategy Fixpoint(Strategy... other) {
        return new Fixpoint(other);
    }
    
    private static class Any extends Strategy {

        private Strategy[] strategies;

        public Any(Strategy... strategies) {
            this.strategies = strategies;
        }    
        
		@Override
        public StrategyResult apply(GPNode i, MersenneTwisterFast rand) throws Strategy.ReplaceFailed {    
            Strategy str = strategies[rand.nextInt(strategies.length)];
            return str.apply(i, rand);
        }
    }

    public static Strategy Any(Strategy... other) {
        return new Any(other);
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

}
