package br.edu.ufrgs.inf.bpm.changes.templates;

import java.util.ArrayList;

public class Lexemes {

    public static String startEvent = "the process begins when";
    public static String sequenceSentence = "AND"; // "and, in sequence,"
    public static String andCondition = "AND"; // "and, in parallel,"
    public static String orCondition = "OR";

    public static final ArrayList<String> SEQ_CONNECTIVES = new ArrayList<String>() {{
        add("then");
        add("afterwards");
        add("subsequently");
    }};
}
