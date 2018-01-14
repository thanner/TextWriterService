package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree.SyntaxNode;
import br.edu.ufrgs.inf.bpm.bpmparser.textToProcess.syntaxTree.SyntaxTree;
import edu.stanford.nlp.trees.Tree;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SentenceAnalyzer {

    private List<SyntaxNode> possibleSentencePOSTypes;

    public SentenceAnalyzer() {
        possibleSentencePOSTypes = new ArrayList<SyntaxNode>();
    }

    /**
     * Método que retorna os elementos de processo identificados em uma sentença
     *
     * @param sentence
     * @return
     */
    public List<ProcessElementType> getProcessElements(String sentence) {
        return getRulesAppear(sentence).map(Rule::getProcessElementType).collect(Collectors.toList()); // R:Se a sentença bateu, pega o tipo de elemento da regra e coleta numa lista?
    }

    /**
     * Método que retorna a lista de regras que aparece em uma sentença
     *
     * @param sentence
     * @return
     */
    public List<Rule> getListRulesAppear(String sentence) {
        return getRulesAppear(sentence).collect(Collectors.toList());
    }

    /**
     * Método que retorna todas as regras que aparecem em uma sentença
     *
     * @param sentence
     * @return
     */
    private Stream<Rule> getRulesAppear(String sentence) {
        List<Rule> rulesAppear = new ArrayList<>();

        //LexicalizedParser lp = new LexicalizedParser(Paths.resourcesPath + Paths.englishModelStanfordPath);
        //Tree parseTree = (Tree) lp.apply(splitWords(sentence));
        Tree parseTree = null;

        // Teste
        parseTree.pennPrint();

        SyntaxNode sentenceNode = convertToSyntaxTree(parseTree);
        List<Rule> activityAndEventsRules = getActivityAndEventsRules();

        // Tratamento sem XOR/AND
        if (!containSignalWord(sentence, SignalWordsManager.getSignalWords())) {
            rulesAppear = activityAndEventsRules.stream().filter(r -> SyntaxTree.hasRuleTree(sentenceNode, r.getPOSTree())).collect(Collectors.toList());
            // Tratamento com XOR e AND
        } else {
            // Faz o tratamento com atividades e eventos
            List<Rule> XORAndANDRules = getANDAndXORRules();
            // Faz o tratamento com xor e and
        }

        return rulesAppear.stream();
    }

    // TODO: Ver regex

    /**
     * Método que separa as palavras dentro de uma string
     *
     * @param str
     * @return
     */
    private List<String> splitWords(String str) {
        return Arrays.asList(str.split(",| |-|!"));
    }

    // TODO: Fazer conversão

    /**
     * Método que transforma uma stanford parse tree em uma árvore sintática nossa e retorna a SyntaxNode do nó raíz
     *
     * @param parseTree
     * @return
     */
    private SyntaxNode convertToSyntaxTree(Tree parseTree) {
        return new SyntaxNode(POSType.Undefined);
    }

    /**
     * Método que verifica se uma sentença possui uma signal word dentro de uma lista de signal words
     *
     * @param sentence
     * @param signalWords
     * @return
     */
    public boolean containSignalWord(String sentence, List<String> signalWords) {
        return signalWords.stream().anyMatch(signalWord -> sentence.toLowerCase().contains(signalWord.toLowerCase()));
    }

    /**
     * Método que retorna todas as regras de Atividades e Eventos
     *
     * @return
     */
    private List<Rule> getActivityAndEventsRules() {
        Set<ProcessElementType> processElementTypeSet = new HashSet<>();
        processElementTypeSet.add(ProcessElementType.Activity);
        processElementTypeSet.add(ProcessElementType.Event);
        return RulesManager.getRules(processElementTypeSet);
    }

    /**
     * Método que retorna as regras AND e XOR
     *
     * @return
     */
    private List<Rule> getANDAndXORRules() {
        Set<ProcessElementType> processElementTypeSet = new HashSet<>();
        processElementTypeSet.add(ProcessElementType.AND);
        processElementTypeSet.add(ProcessElementType.XOR);
        return RulesManager.getRules(processElementTypeSet);
    }

    /**
     // Retorna a lista de regras que aparece em uma sentença
     private Stream<Rule> getRulesAppear(String sentence) {
     List<Rule> rules = new ArrayList<>();

     // Recuperar os POS Type da sentença
     // Como saber o POS da WORD? Stanford tagger
     // https://nlp.stanford.edu/software/tagger.shtml
     // https://nlp.stanford.edu/nlp/javadoc/javanlp/edu/stanford/nlp/tagger/maxent/MaxentTagger.html
     // List<POSType> sentencePOSType = convertePOSType(recuperaDoStanfordParser(sentence));

     //LexicalizedParser lp = LexicalizedParser.loadModel(Paths.resourcesPath + Paths.englishModelStanfordPath);
     //Tree parse = lp.apply(splitWords(sentence));

     //List<POSType> sentencePOSType = taggedWordToPOSType(taggedWords);

     //for (Rule rule : RulesManager.getRules()) {
     //    if (isRuleAppear(sentencePOSType, rule)) {
     //        rules.add(rule);
     //    }
     //}

     if (containSignalWord(sentence, SignalWordsManager.getSignalWords())) {   //Is it a XOR/AND?
     //verifyACTEVTsimplified;
     } else {
     //verifyACTEVTcomplete;
     //verifyXORAND;
     }
     return rules.stream();
     }

     // TODO: Sei lá
     private List<POSType> taggedWordToPOSType(SentenceUtils taggedWords) {
     return null;
     }

     private boolean isRuleAppear(List<POSType> sentencePOSType, Rule rule) {
     int indexSentencePOS = 0;
     int sizeSentencePOS = sentencePOSType.size();

     // Não sei
     int indexPOSStructure = 0;
     int currentPOS = 0;

     POSType currentSentencePOS = sentencePOSType.get(indexPOSStructure);
     for (POSType currentRulePOS : rule.getPOSList()) {
     if (currentRulePOS == currentSentencePOS) {
     indexSentencePOS++;
     if (indexSentencePOS == sizeSentencePOS) {
     return true;
     } else {
     currentPOS = indexPOSStructure;
     }
     }
     }
     return false;
     }

     private boolean hasPOSAppear(String sentence, POSType POSType) {
     if (isANDSignalPOS(POSType)) {
     return containSignalWord(sentence, SignalWordsManager.getANDSignalWords());
     } else if (isXORSignalPOS(POSType)) {
     return containSignalWord(sentence, SignalWordsManager.getXORSignalWords());
     } else {
     return hasNormalPOSAppear(sentence, POSType);
     }
     }

     private boolean isANDSignalPOS(POSType POSType) {
     return POSType.equals(br.edu.ufrgs.bpmparser.textToProcess.POSType.ANDSignalWord);
     }

     private boolean isXORSignalPOS(POSType POSType) {
     return POSType.equals(br.edu.ufrgs.bpmparser.textToProcess.POSType.XORSignalWord);
     }

     private boolean hasNormalPOSAppear(String word, POSType POSType) {
     return true;
     }
     **/

}
