package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree.SyntaxNode;
import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree.SyntaxTree;

public class AppTree {

    public static void main(String[] args) {
        // Cria árvores
        SyntaxNode sentenceTree = createSentenceTree();
        SyntaxNode ruleTree = createRuleTree();
        SyntaxNode ruleTree2 = createRuleTree2();

        System.out.println("Árvore da sentença");
        sentenceTree.print();

        // Deve gerar subárvore
        System.out.println("\nÁrvore da regra 1");
        ruleTree.print();

        System.out.println("\nSubárvore da regra 1");
        SyntaxNode subTree = SyntaxTree.findRuleTreeInSentenceTree(sentenceTree, ruleTree);
        if (subTree != null) {
            subTree.print();
        }

        // Deve gerar vazio
        System.out.println("\nÁrvore da regra 2");
        ruleTree2.print();

        System.out.println("\nSubárvore da regra 2");
        SyntaxNode subTree2 = SyntaxTree.findRuleTreeInSentenceTree(sentenceTree, ruleTree2);
        if (subTree2 != null) {
            subTree2.print();
        }
    }

    private static SyntaxNode createSentenceTree() {
        SyntaxNode root = new SyntaxNode(POSType.POS);
        SyntaxNode node1 = new SyntaxNode(POSType.ANDSignalWord);
        SyntaxNode node2 = new SyntaxNode(POSType.TO);
        SyntaxNode node3 = new SyntaxNode(POSType.DT);
        SyntaxNode node4 = new SyntaxNode(POSType.FW);
        SyntaxNode node5 = new SyntaxNode(POSType.CD);

        root.addChildren(node1);
        node1.addChildren(node2);
        node1.addChildren(node3);
        node2.addChildren(node4);
        node2.addChildren(node5);

        return root;
    }

    private static SyntaxNode createRuleTree() {
        SyntaxNode root = new SyntaxNode(POSType.ANDSignalWord);
        SyntaxNode node1 = new SyntaxNode(POSType.TO);
        SyntaxNode node2 = new SyntaxNode(POSType.FW);
        SyntaxNode node3 = new SyntaxNode(POSType.CD);

        root.addChildren(node1);
        node1.addChildren(node2);
        node1.addChildren(node3);

        return root;
    }

    private static SyntaxNode createRuleTree2() {
        SyntaxNode root = new SyntaxNode(POSType.ANDSignalWord);
        SyntaxNode node1 = new SyntaxNode(POSType.TO);
        SyntaxNode node2 = new SyntaxNode(POSType.FW);
        SyntaxNode node3 = new SyntaxNode(POSType.CD);

        root.addChildren(node1);
        node1.addChildren(node3);
        node1.addChildren(node2);

        return root;
    }

}
