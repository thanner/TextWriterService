package br.edu.ufrgs.inf.bpm.changes.templates;

import java.util.ArrayList;

public class Lexemes {

    public static final String START_EVENT = "the process begins when";
    public static final String SENTENCE_AGGREGATION = "AND"; // "and, in sequence,"
    public static final String AND_CONDITION = "AND"; // "and, in parallel,"
    public static final String OR_CONDITION = "OR";
    public static final String SEQUENCEFINAL_CONNECTIVE = "finally";

    public static final ArrayList<String> SEQUENCE_CONNECTIVES = new ArrayList<String>() {{
        add("then");
        add("afterwards");
        add("subsequently");
    }};
}
