package br.edu.ufrgs.inf.bpm.builder;

import java.util.ArrayList;
import java.util.List;

public class IndexDocumentGenerator {
    private List<Integer> indexList;

    public String getIndex(int level, int lastLevel) {
        if (indexList == null) {
            generateFirstIndex();
        } else {
            incrementNumberIndex(level);
            if (lastLevel > level) {
                resetPositionsIndex(level);
            }
        }
        return generateIndexDocumentString();
    }

    private void generateFirstIndex() {
        indexList = new ArrayList<>();
        indexList.add(1);
    }

    private void resetPositionsIndex(int level) {
        indexList = indexList.subList(0, level + 1);
    }

    private void incrementNumberIndex(int level) {
        while (level + 1 > indexList.size()) {
            indexList.add(0);
        }

        int currentIndexLevel = indexList.get(level);
        indexList.set(level, currentIndexLevel + 1);
    }

    private String generateIndexDocumentString() {
        StringBuilder indexString = new StringBuilder();

        for (int index = 0; index < indexList.size() - 1; index++) {
            indexString.append(indexList.get(index)).append(".");
        }
        indexString.append(indexList.get(indexList.size() - 1));

        return indexString.toString();
    }
}
