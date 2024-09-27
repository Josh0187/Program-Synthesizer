package synth.cfg;

import synth.core.ASTNode;

import java.util.*;

public class CFG {
    /**
     * map from non-terminal (return) symbols to all their productions
     */
    private final Map<NonTerminal, List<Production>> symbolToProductions;
    /**
     * start symbol of the grammar
     */
    private final NonTerminal startSymbol;

    public CFG(NonTerminal startSymbol, Map<NonTerminal, List<Production>> symbolToProductions) {
        this.startSymbol = startSymbol;
        this.symbolToProductions = symbolToProductions;
    }

    public NonTerminal getStartSymbol() {
        return startSymbol;
    }

    public Map<NonTerminal, List<Production>> getSymbolToProductions() {
        return this.symbolToProductions;
    }

    public CFG getBcfg() {
        return new CFG(this.getBNonTerminal(), this.getSymbolToProductions());
    }

    public List<Production> getProductions(NonTerminal symbol) {
        return symbolToProductions.get(symbol);
    }

    public List<Production> getAllProductions() {
        Set<NonTerminal> keys = symbolToProductions.keySet();
        List<Production> prods = new ArrayList<>();
        for (NonTerminal n : keys) {
            prods.addAll(symbolToProductions.get(n));
        }
        return prods;
    }

    // Returns all terminal symbols with NO arguments
    public List<Terminal> getTerminals() {
        List<Terminal> terminals = new ArrayList<>();
        List<Production> prods = this.getAllProductions();
        // find terminal symbols without arguments
        for (Production prod : prods) {
            if (prod.getArgumentSymbols().isEmpty()) {
               terminals.add(prod.getOperator());
            }
        }
        return terminals;
    }

    public List<NonTerminal> getNonTerminals() {
        List<NonTerminal> nonterminals = new ArrayList<>();
        List<Production> prods = this.getAllProductions();
        // find terminal symbols without arguments
        for (Production prod : prods) {
            if (!nonterminals.contains(prod.getReturnSymbol())) {
                nonterminals.add(prod.getReturnSymbol());
            }
        }
        return nonterminals;
    }

    public NonTerminal getBNonTerminal() {
        List<Production> prods = this.getAllProductions();
        // find terminal symbols without arguments
        for (Production prod : prods) {
            if (prod.getReturnSymbol().getName() == "B") {
                return prod.getReturnSymbol();
            }
        }
        return null;
    }

    // Returns all terminal symbols with >1 arguments
    public List<Terminal> getTerminalOperators() {
        List<Terminal> terminals = new ArrayList<>();
        List<Production> prods = this.getAllProductions();
        // find terminal symbols without arguments
        for (Production prod : prods) {
            if (!prod.getArgumentSymbols().isEmpty()) {
                terminals.add(prod.getOperator());
            }
        }
        return terminals;
    }

    // Returns Non terminal symbol that a given an ASTnode corresponds to
    public NonTerminal getReturnSymbolOfTerminal(Terminal root) {
        List<Production> prods = getAllProductions();
        for (Production prod : prods) {
            if (root == prod.getOperator()) {
                return prod.getReturnSymbol();
            }
        }
        // return null if not found
        return null;
    }

    // Returns list of non-terminal args for a given operator
    public List<NonTerminal> getArgs(Terminal operator) {
        List<Production> prods = getAllProductions();
        for (Production prod : prods) {
            if (operator == prod.getOperator()) {
                return prod.getNonTerminalArgumentSymbols();
            }
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Start symbol: ").append(startSymbol).append(System.lineSeparator());
        builder.append("Productions:").append(System.lineSeparator());
        for (NonTerminal retSymbol : symbolToProductions.keySet()) {
            builder.append(symbolToProductions.get(retSymbol)).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
