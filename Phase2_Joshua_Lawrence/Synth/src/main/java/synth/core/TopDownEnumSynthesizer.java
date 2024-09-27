package synth.core;
import com.google.common.collect.Sets;
import com.microsoft.z3.AST;
import synth.cfg.*;
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
        // ASTs to evaluate
        List<ASTNode> workList = new ArrayList<ASTNode>();
        // ASTs already evaluated and used as components
        List<ASTNode> components = new ArrayList<>();
        // add all terminals with no arguments to workList
        List<Terminal> terms = cfg.getTerminals();
        // Get all operators
        List<Terminal> ops = cfg.getTerminalOperators();

        for (Terminal t : terms) {
            workList.add(new ASTNode(t,Collections.emptyList()));
        }
        int programSize = 1;
        while(programSize < 20) {
            for (ASTNode ast : workList) {
                Program program = new Program(ast);
                // if ast returns a boolean, don't evaluate it
                if (cfg.getReturnSymbolOfTerminal((Terminal)ast.getSymbol()).equals(cfg.getStartSymbol())) {
                    // if correct program found, return it
                    if (checkProgram(program,examples)) {
                        return program;
                    }
                }
            }
            // if correct program not found, grow the worklist
            components.addAll(workList);
            components = elimEq(components,cfg,examples);
            workList.clear();
            // grow worklist operator by operator
            for (Terminal op : ops) {
                workList.addAll(grow(components,op,cfg,programSize));
                workList = elimEq(workList,cfg,examples);
            }
            programSize++;
        }
        return null;
    }

    // returns programs in the worklist that return the given non-terminal expression
    public List<ASTNode> getProgramsReturningNonTerminal(List<ASTNode> worklist, CFG cfg, NonTerminal nonterm) {
        List<ASTNode> EPrograms = new ArrayList<ASTNode>();
        for (ASTNode program : worklist) {
            if (cfg.getReturnSymbolOfTerminal((Terminal)program.getSymbol()).equals(nonterm)) {
                EPrograms.add(program);
            }
        }
        return EPrograms;
    }

    public List<ASTNode> enumerateOperator(Terminal operator, CFG cfg ,Map<NonTerminal, List<ASTNode>> argPrograms, int size) {
        List<ASTNode> newPrograms = new ArrayList<>();
        List<Set<ASTNode>> listOfSets = new ArrayList<>();
        int argSize;
        for (NonTerminal arg : cfg.getArgs(operator)) {
            listOfSets.add(Sets.newHashSet(argPrograms.get(arg)));
        }
        // Get cartesian product for all combinations of arguments
        Set<List<ASTNode>> cartesianProduct = Sets.cartesianProduct(listOfSets);

        for (List<ASTNode> argCombination : cartesianProduct) {
            // only add new programs that have the specified size
            argSize = 0;
            for (ASTNode ast : argCombination) {
                argSize += getProgramSize(ast);
            }
            if (argSize+1 == size) {
                newPrograms.add(new ASTNode(operator, argCombination));
            }
        }
        return newPrograms;
    }

    public List<ASTNode> grow(List<ASTNode> components, Terminal op, CFG cfg, int size) {
        //List<Terminal> ops = cfg.getTerminalOperators();
        List<NonTerminal> nonTerminals = cfg.getNonTerminals();
        List<ASTNode> newPrograms = new ArrayList<>();
        Map<NonTerminal,List<ASTNode>> programArgs = new HashMap<>();
        for (NonTerminal nonTerminal: nonTerminals) {
            programArgs.put(nonTerminal, getProgramsReturningNonTerminal(components,cfg,nonTerminal));
        }
        newPrograms.addAll(enumerateOperator(op,cfg,programArgs,size));
        return newPrograms;
    }

    // Eliminates observationally equivalent programs
    public List<ASTNode> elimEq(List<ASTNode> worklist, CFG cfg, List<Example> examples) {
        // Create a new ArrayList
        ArrayList<ASTNode> newList = new ArrayList<ASTNode>();
        List<List<Integer>> all_outputs = new ArrayList<>();
        List<List<Boolean>> all_B_outputs = new ArrayList<>();

        // Traverse original worklist
        for (ASTNode element : worklist) {
            // for expressions that return E
            if (cfg.getReturnSymbolOfTerminal((Terminal)element.getSymbol()).equals(cfg.getStartSymbol())) {
                List<Integer> outputs = new ArrayList<>();
                for (Example ex : examples) {
                    // evaluate program on all inputs and store the outputs
                    outputs.add(Interpreter.evaluate(new Program(element),ex.getInput()));
                }
                if (!all_outputs.contains(outputs)) {
                    all_outputs.add(outputs);
                    newList.add(element);
                }
            }
            // for expressions that return B
            else {
                List<Boolean> Boutputs = new ArrayList<>();
                for (Example ex : examples) {
                    // evaluate program on all inputs and store the outputs
                    Boutputs.add(Interpreter.evaluatePred(element,ex.getInput()));
                }
                if (!all_B_outputs.contains(Boutputs)) {
                    all_B_outputs.add(Boutputs);
                    newList.add(element);
                }
            }
        }
        // return the new list
        return newList;
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

    public int getProgramSize(ASTNode ast) {
        int size = 0;
        Stack<ASTNode> s = new Stack<ASTNode>();
        ASTNode curr;
        s.push(ast);

        while (!s.isEmpty()) {
            curr = s.pop();
            size++;
            for (ASTNode child : curr.getChildren()) {
                s.push(child);
            }
        }
        return size;
    }

    // Checks that all elements in the first list is less than the corresponding (same index) element in the second list
    public Boolean checkLessThan(List<Integer> list1, List<Integer> list2) {
        for (int i = 0 ; i < list1.size(); i++) {
            if (list1.get(i) > list2.get(i)) {
                return false;
            }
        }
        return true;
    }

    public List<Integer> getOutputs(List<Example> examples) {
        List<Integer> outputs = new ArrayList<>();
        for (Example ex : examples) {
            outputs.add(ex.getOutput());
        }
        return outputs;
    }
}

