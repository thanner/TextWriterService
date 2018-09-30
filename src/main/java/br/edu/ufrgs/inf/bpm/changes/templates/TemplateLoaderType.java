package br.edu.ufrgs.inf.bpm.changes.templates;

public enum TemplateLoaderType {

    AND_SPLIT("ANDSplit.xml"),
    AND_JOIN("ANDJoin.xml"),
    AND_JOIN_SIMPLE("ANDJoinSimple.xml"),
    SKIP("Skip.xml"),
    LOOP_SPLIT("LoopSplit.xml"),
    LOOP_JOIN("LoopJoin.xml"),
    LOOP_SKIP("LoopSkip.xml"),
    XOR("XOR.xml"),
    OR("OR.xml"),
    RIGID("Rigid.xml"),
    RIGID_MAIN("RigidMain.xml"),
    RIGID_DEV("RigidDeviations.xml"),
    STARTDECISION("StartDecision.xml"),
    EMPTYSEQUENCEFLOW("EmptySequenceFLow.xml");

    //RIGIDSTARTACTIVITY("RigidStartActivity.xml";
    //RIGIDENDACTIVITY("RigidEndActivity.xml";
    //ISOLATEDRIGIDACTIVITY("IsolatedRigidActivity.xml";

    private final String templateLoader;

    TemplateLoaderType(String templateLoader) {
        this.templateLoader = templateLoader;
    }

    public String getTemplateLoader() {
        return templateLoader;
    }

}
