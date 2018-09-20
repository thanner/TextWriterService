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

    public static final ArrayList<String> PARALLEL_CONNECTIVES = new ArrayList<String>() {{
        add("in the meantime");
        add("at the same time");
    }};

    public static final String ORDINAL_CONNECTIVE = "in the @ordinal procedure";

    public static final String SENTENCE_AGGREGATION = "and then";
    public static final String AND_CONDITION = "AND";
    public static final String OR_CONDITION = "OR";

    public static String sequenceSentence = "AND"; // "and, in sequence,"
    public static String andCondition = "AND"; // "and, in parallel,"
    public static String orCondition = "OR";
    public static String SEQUENCEFINAL_CONNECTIVE = "finally";
    public static String startEvent = "the process starts with";

}
