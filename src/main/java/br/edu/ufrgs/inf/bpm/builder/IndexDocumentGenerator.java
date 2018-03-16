package br.edu.ufrgs.inf.bpm.builder;

import java.util.ArrayList;
import java.util.List;

public class IndexDocumentGenerator {
    private List<IndexTuple> indexList;

    public String getIndex(int level, int lastLevel, boolean isParallel) {
        if (indexList == null) {
            generateFirstIndex();
        } else if (isInNewModelLevel(level, lastLevel)) {
            createNecessaryIndex(level);
        } else if (isInSequence(level, lastLevel) && !isParallel) {
            incrementSequentialIndex(level);
        } else if (isInParallel(level, lastLevel) && isParallel) {
            incrementParallelIndex(level);
        } else if (isOutModelLevel(level, lastLevel)) {
            incrementSequentialIndex(level);
            resetPositionsIndex(level);
        }

        return generateIndexDocumentString();
    }

    private boolean isInNewModelLevel(int level, int lastLevel) {
        return level > lastLevel;
    }

    private boolean isInSequence(int level, int lastLevel) {
        return level == lastLevel;
    }

    private boolean isInParallel(int level, int lastLevel) {
        return level == lastLevel;
    }

    private boolean isOutModelLevel(int level, int lastLevel) {
        return lastLevel > level;
    }

    private void generateFirstIndex() {
        indexList = new ArrayList<>();
        indexList.add(new IndexTuple());
    }

    private void createNecessaryIndex(int level) {
        while (level + 1 > indexList.size()) {
            indexList.add(new IndexTuple());
        }
    }

    private void incrementSequentialIndex(int level) {
        int currentIndexLevel = indexList.get(level).getIndexSequence();
        indexList.get(level).setIndexSequence(currentIndexLevel + 1);
    }

    private void incrementParallelIndex(int level) {
        int currentIndexLevel = indexList.get(level).getIndexParallel();
        indexList.get(level).setIndexSequence(1);
        indexList.get(level).setIndexParallel(currentIndexLevel + 1);
    }

    private void resetPositionsIndex(int level) {
        indexList = indexList.subList(0, level + 1);
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
