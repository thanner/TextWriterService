package br.edu.ufrgs.inf.bpm.changes.templates;

import java.util.ArrayList;

public class Lexemes {

    public static final ArrayList<String> SEQUENCE_CONNECTIVES = new ArrayList<String>() {{
        add("then");
        add("next");
        add("afterwards");
        add("after that");
        add("subsequently");
    }};
    public static final String SENTENCE_AGGREGATION = "AND";
    public static final String AND_CONDITION = "AND";
    public static final String OR_CONDITION = "OR";

    public static String sequenceSentence = "AND"; // "and, in sequence,"
    public static String andCondition = "AND"; // "and, in parallel,"
    public static String orCondition = "OR";
    public static String SEQUENCEFINAL_CONNECTIVE = "finally";
    public static String startEvent = "the process starts with";

}
