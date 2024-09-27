package synth.core;
import synth.cfg.CFG;
import synth.cfg.NonTerminal;
import synth.cfg.Production;
import synth.cfg.Symbol;

import java.util.*;

public class TopDownEnumSynthesizer implements ISynthesizer {

    /**
     * Synthesize a program f(x, y, z) based on a context-free grammar and examples
     *
     * @param cfg      the context-free grammar
     * @param examples a list of examples
     * @return the program or null to indicate synthesis failure
     */
    @Override
    public Program synthesize(CFG cfg, List<Example> examples) {

        Queue<ASTNode> workList = new LinkedList<ASTNode>();
        ASTNode FirstAST = new ASTNode(cfg.getStartSymbol(), Collections.emptyList());
        workList.add(FirstAST);
        int i = 0;

        while (!workList.isEmpty()) {
            ASTNode AST = workList.remove();
            // check if AST program statisfies examples
            Program prog = new Program(AST);

            // if the program is complete and is correct for the examples, return the program
            if (isASTComplete(AST)) {
                if (checkProgram(prog, examples)) {
                    return prog;
                }
            }
            // if AST is not complete, expand AST and add new trees to workList
            else {
                workList.addAll(expand(AST,cfg));
            }
        }
        // return null if no program found when loop terminates
        return null;
    }

    //clones ast with the first non-terminal replaced with ASTNode expansion
    public ASTNode copyAndExpand(ASTNode ast, ASTNode expansion) {
        if (ast == null) {
            return null;
        }
        List<ASTNode> children = new ArrayList<>();
        for (ASTNode child : ast.getChildren()) {
            children.add(copyAndExpand(child, expansion));
        }
        // when chosen node to be replaced is found, replace it with the expansion
        if (ast.getIsChosen()) {
            return expansion;
        }
        return new ASTNode(ast.getSymbol(), children);
    }

    public List<ASTNode> expand(ASTNode ast, CFG cfg) {
        Stack<ASTNode> s = new Stack<ASTNode>();
        List<ASTNode> newTrees = new ArrayList<>();
        ASTNode curr;
        s.push(ast);

        while (!s.isEmpty()) {
            curr = s.pop();
            // check if curr is non-terminal symbol
            if (curr.getSymbol().isNonTerminal()) {
                // set node to chosen so it can be replaced in copyAndExpand
                curr.setIsChosen(true);
                // get all productions of the non terminal symbol
                List<Production> prods = cfg.getProductions((NonTerminal)curr.getSymbol());
                for (Production prod : prods) {
                    List<ASTNode> args = new ArrayList<>();
                    for (Symbol arg : prod.getArgumentSymbols()) {
                        args.add(new ASTNode(arg,Collections.emptyList()));
                    }
                    ASTNode newAST = new ASTNode(prod.getOperator(), args);
                    newTrees.add(copyAndExpand(ast,newAST));
                }
                return newTrees;
            }

            for (ASTNode child : curr.getChildren()) {
                s.push(child);
            }
        }
        // returns null if ast is complete (no non-terminal nodes)
        return null;
    }

    // Checks if an AST is complete
    public Boolean isASTComplete(ASTNode ast) {
        // Traverse AST, if non-terminal found return false, otherwise return true
        Stack<ASTNode> s = new Stack<ASTNode>();
        ASTNode curr;
        s.push(ast);

        while (!s.isEmpty()) {
            curr = s.pop();
            // check if curr is non-terminal symbol
            if (curr.getSymbol().isNonTerminal()) {
                return false;
            }

            for (ASTNode child : curr.getChildren()) {
                s.push(child);
            }
        }
        // returns true if no non-terminal found
        return true;
    }

    // Checks if a program is correct on a list of examples
    public Boolean checkProgram(Program prog, List<Example> examples) {
        int val;
        for (Example ex : examples) {
            // evaluate program on ex input
            val = Interpreter.evaluate(prog,ex.getInput());
            // break loop if program is wrong on any example
            if (val != ex.getOutput()) {
                return false;
            }
        }
        return true;
    }
}

