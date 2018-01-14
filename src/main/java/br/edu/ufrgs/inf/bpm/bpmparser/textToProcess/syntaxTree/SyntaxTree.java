package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree;

import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.POSType;
import edu.stanford.nlp.trees.SimpleTree;

import java.util.List;

public class SyntaxTree extends SimpleTree {

    public static boolean hasRuleTree(SyntaxNode sentenceTree, SyntaxNode ruleTree) {
        SyntaxNode subTree = findRuleTreeInSentenceTree(sentenceTree, ruleTree);
        return subTree != null;
    }

    public static SyntaxNode findRuleTreeInSentenceTree(SyntaxNode sentenceTree, SyntaxNode ruleTree) {
        if (isPOSTypeEquals(sentenceTree.getUniquePOSTypeNode(), ruleTree.getPOSTypeNode())) {
            if (matchChildren(sentenceTree, ruleTree)) {
                return sentenceTree;
            }
        }

        SyntaxNode result = null;
        for (SyntaxNode child : sentenceTree.getChildren()) {
            result = findRuleTreeInSentenceTree(child, ruleTree);

            if (result != null) {
                if (matchChildren(sentenceTree, result)) {
                    return result;
                }
            }
        }

        return result;
    }

    private static boolean matchChildren(SyntaxNode sentenceTree, SyntaxNode ruleTree) {
        if (!isPOSTypeEquals(sentenceTree.getUniquePOSTypeNode(), ruleTree.getPOSTypeNode())) {
            return false;
        }

        if (sentenceTree.getChildren().size() < ruleTree.getChildren().size()) {
            return false;
        }

        boolean result = true;
        int treeChildrenIndex = 0;

        for (int searchChildrenIndex = 0; searchChildrenIndex < ruleTree.getChildren().size(); searchChildrenIndex++) {

            // Skip non-matching children in the tree.
            while (treeChildrenIndex < sentenceTree.getChildren().size()
                    && !(result = matchChildren(sentenceTree.getChildren().get(treeChildrenIndex),
                    ruleTree.getChildren().get(searchChildrenIndex)))) {
                treeChildrenIndex++;
            }

            if (!result) {
                return result;
            }
        }
        return result;
    }

    private static boolean isPOSTypeEquals(POSType sentencePOSType, List<POSType> rulePOSTypes) {
        return rulePOSTypes.stream().anyMatch(r -> r.equals(POSType.None) || r.equals(sentencePOSType));
    }
}
