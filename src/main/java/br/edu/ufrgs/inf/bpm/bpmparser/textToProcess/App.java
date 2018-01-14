package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import java.util.List;

public class App {

    public static void main(String[] args) {

        // Teste 1: Recuperar as regras
        List<Rule> rules = RulesManager.getRules();
        System.out.println("Test 1");
        rules.forEach(System.out::println);

        // Teste 2: Verificar as regras de uma sentença
        String sentenceTest = "the employee must buy a in case box of cake";
        SentenceAnalyzer sentenceAnalyzer = new SentenceAnalyzer();
        // ...

        // Teste 3: Verificar se SignalWord aparece
        Boolean isAppear = sentenceAnalyzer.containSignalWord(sentenceTest, SignalWordsManager.getXORSignalWords());
        System.out.println(isAppear);

        /**
         List<Rule> rulesAppear = sentenceAnalyzer.getRulesAppear(sentenceTest);
         System.out.println("Test 2");
         for (Rule r : rulesAppear) {
         System.out.println(r);
         }**/
    }

}
