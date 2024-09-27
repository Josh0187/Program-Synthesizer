package synth.core;

import synth.cfg.Symbol;

import java.util.List;

public class ASTNode {
    private final Symbol symbol;
    private final List<ASTNode> children;
    private Boolean isChosen;

    public ASTNode(Symbol symbol, List<ASTNode> children) {
        this.symbol = symbol;
        this.children = children;
        this.isChosen = false;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public List<ASTNode> getChildren() {
        return children;
    }

    public ASTNode getChild(int index) {
        return children.get(index);
    }

    public void setIsChosen(Boolean val) {
        this.isChosen = val;
    }

    public Boolean getIsChosen() {
        return this.isChosen;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(symbol);
        String separator = "";
        if (!children.isEmpty()) {
            builder.append("(");
            for (ASTNode child : children) {
                builder.append(separator);
                separator = ", ";
                builder.append(child);
            }
            builder.append(")");
        }
        return builder.toString();
    }
}
