package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree.SyntaxNode;

public class Rule {

    private final String elementSplit = "-";
    private String ruleName;
    private ProcessElementType processElementType;
    private SyntaxNode POSTree;

    public Rule() {
    }

    public Rule(String ruleName, String processElementType, String POSTreeString) {
        this.ruleName = ruleName;
        setProcessElementType(processElementType);
        setPOSTree(POSTreeString);
    }

    public String getRuleName() {
        return ruleName;
    }

    public ProcessElementType getProcessElementType() {
        return processElementType;
    }

    public void setProcessElementType(String elementType) {
        this.processElementType = convertToProcessElementType(elementType);
    }

    public SyntaxNode getPOSTree() {
        return POSTree;
    }

    public void setPOSTree(String POSTreeString) {
        this.POSTree = convertToPOSTree(POSTreeString);
    }

    @Override
    public String toString() {
        return "Rule name: " + ruleName + ", Element type: " + processElementType + ", POS Structure: " + POSTree;
    }

    private ProcessElementType convertToProcessElementType(String element) {
        try {
            return ProcessElementType.valueOf(element);
        } catch (IllegalArgumentException e) {
            return ProcessElementType.Undefined;
        }
    }

    // TODO: Fazer conversão

    /**
     * Método que recupera uma string representando uma tree, converte numa árvore sintática nossa e retorna o SyntaxNode da raíz
     *
     * @param POSTreeString
     * @return
     */
    private SyntaxNode convertToPOSTree(String POSTreeString) {
        return new SyntaxNode(POSType.Undefined);
        /**
         List<POSType> POSList = new ArrayList<>();
         List<String> list = Arrays.asList(structure.replace("[", "").replace("]", "").split(elementSplit));
         for (String string : list) {
         try {
         POSList.add(POSType.valueOf(string));
         } catch (IllegalArgumentException e) {
         POSList.add(POSType.Undefined);
         }
         }
         return POSList;**/
    }

}
