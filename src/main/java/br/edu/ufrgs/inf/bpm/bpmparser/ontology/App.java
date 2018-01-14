package br.edu.ufrgs.inf.bpm.bpmparser.ontology;

import br.edu.ufrgs.inf.bpm.bpmparser.communication.ElementType;
import br.edu.ufrgs.inf.bpm.bpmparser.communication.ProcessElement;

import java.util.List;

public class App {

    public static void main(String[] args) {

        // Ontology Test
        OntologyWrapper.init();

        // Creating Individual
        ProcessElement individual = new ProcessElement(ElementType.STARTEVENT);
        individual.addDataProperty("name", "ValorDataPropertyName");

        // Add and get individual
        OntologyWrapper.addCompleteIndividual(individual);
        List<ProcessElement> individuals = OntologyWrapper.getCompleteIndividuals();
        individuals.forEach(System.out::println);

        // WordNet Test
        /**
         try {
         JWNL.initialize(new FileInputStream(Paths.resourcesPath + Paths.wordNetPath));
         final Dictionary dictionary = Dictionary.getInstance();
         IndexWord indexWord = dictionary.getIndexWord(POS.NOUN, "blue");
         Synset[] senses = indexWord.getSenses();
         for (Synset set : senses) {
         System.out.println(indexWord + ": " + set.getGloss());
         }
         } catch(Exception e) {
         e.printStackTrace();
         }
         **/

    }
}