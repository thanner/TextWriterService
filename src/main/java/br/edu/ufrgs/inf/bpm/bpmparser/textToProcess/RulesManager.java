package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.util.CsvReader;
import br.edu.ufrgs.inf.bpm.bpmparser.util.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class RulesManager {

    private static List<Rule> rules;
    private static CsvReader csvReader = new CsvReader();

    public static List<Rule> getRules() {
        if (rules == null) {
            rules = new ArrayList<>();
            List<List<String>> CSVRules = csvReader.readCsv(Paths.rulesPath);

            for (List<String> CSVRule : CSVRules) {
                try {
                    rules.add(new Rule(CSVRule.get(0), CSVRule.get(1), CSVRule.get(2)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        return rules;
    }

    public static List<Rule> getRules(Set<ProcessElementType> elementTypes) {
        return getRules().stream().filter(rule -> elementTypes.contains(rule.getProcessElementType())).collect(Collectors.toList());
    }

}
