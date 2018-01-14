package br.edu.ufrgs.inf.bpm.bpmparser.textToProcess;

import br.edu.ufrgs.inf.bpm.bpmparser.util.CsvReader;
import br.edu.ufrgs.inf.bpm.bpmparser.util.Paths;

import java.util.ArrayList;
import java.util.List;

public class SignalWordsManager {

    private static List<String> andSignalWords;
    private static List<String> xorSignalWords;
    private static List<String> allSignalWords;
    private static CsvReader csvReader = new CsvReader();

    public static List<String> getANDSignalWords() {
        if (andSignalWords == null) {
            andSignalWords = getSignalWordsfromPath(Paths.andSignalWordsPath);
        }
        return andSignalWords;
    }

    public static List<String> getXORSignalWords() {
        if (xorSignalWords == null) {
            xorSignalWords = getSignalWordsfromPath(Paths.xorSignalWordsPath);
        }
        return xorSignalWords;
    }

    public static List<String> getSignalWords() {
        if (allSignalWords == null) {
            allSignalWords = new ArrayList<>();
            allSignalWords.addAll(getANDSignalWords());
            allSignalWords.addAll(getXORSignalWords());
        }
        return allSignalWords;
    }

    private static List<String> getSignalWordsfromPath(String signalWordsPath) {
        List<String> signalWords = new ArrayList<>();
        List<List<String>> CSVWords = csvReader.readCsv(signalWordsPath);

        for (List<String> CSVWord : CSVWords) {
            signalWords.add(CSVWord.get(0));
        }

        return signalWords;
    }

}