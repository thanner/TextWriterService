package br.edu.ufrgs.inf.bpm.changes.templates;

import java.util.ArrayList;

public class Lexemes {

    public static final ArrayList<String> SEQUENCE_CONNECTIVES = new ArrayList<String>() {{
        //add("then"); // Used in aggregation
        add("next");
        //add("afterwards"); // Used in OR Join
        add("subsequently");
        add("after that");
    }};

    public static final ArrayList<String> PARALLEL_CONNECTIVES = new ArrayList<String>() {{
        add("in the meantime");
        add("at the same time");
    }};

    public static final String ORDINAL_CONNECTIVE = "in the @ordinal procedure";

    public static final String IF_CONNECTIVE = "if \"@condition\"";

    public static final String START_EVENT_CONNECTIVE = "the process starts when";
    public static final String START_EVENT_WHEN_CONNECTIVE = "when the process starts,";
    public static final String SEQUENCE_FINAL_CONNECTIVE = "finally";

    public static final String SENTENCE_AGGREGATION = "and then";
    public static final String SIMPLE_XOR_START_CONNECTIVE = "must either";
    public static final String SIMPLE_XOR_AGGREGATION_CONNECTIVE = "or";


    public static final String AND_CONDITION = "AND";
    public static final String OR_CONDITION = "OR";

    public static final String AND_JOIN_CONNECTIVE = "after each case"; //after each of these procedures
    public static final String XOR_JOIN_CONNECTIVE = "in any case"; //after one of these procedures
    public static final String OR_JOIN_CONNECTIVE = "afterwards";

    // public static String sequenceSentence = "AND"; // "and, in sequence,"
    // public static String andCondition = "AND"; // "and, in parallel,"
    // public static String orCondition = "OR";

}
