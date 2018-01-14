package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree;

import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.POSType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SyntaxNode {

    private List<POSType> posTypeNode;
    private Vector<SyntaxNode> children;

    public SyntaxNode(POSType posType) {
        posTypeNode = new ArrayList<>(Arrays.asList(posType));
        children = new Vector<>();
    }

    public SyntaxNode(List<POSType> posType) {
        posTypeNode = posType;
        children = new Vector<>();
    }

    public List<POSType> getPOSTypeNode() {
        return posTypeNode;
    }

    public POSType getUniquePOSTypeNode() {
        return posTypeNode.get(0);
    }

    public void addChildren(SyntaxNode node) {
        this.children.add(node);
    }

    public Vector<SyntaxNode> getChildren() {
        return children;
    }

    public void print() {
        print("", true);
    }

    private void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + posTypeNode);
        for (int i = 0; i < children.size() - 1; i++) {
            children.get(i).print(prefix + (isTail ? "    " : "│   "), false);
        }
        if (children.size() > 0) {
            children.get(children.size() - 1)
                    .print(prefix + (isTail ? "    " : "│   "), true);
        }
    }

}
