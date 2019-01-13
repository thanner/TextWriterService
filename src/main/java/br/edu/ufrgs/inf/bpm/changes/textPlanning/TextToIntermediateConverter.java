package br.edu.ufrgs.inf.bpm.changes.textPlanning;

import br.edu.ufrgs.inf.bpm.builder.FragmentGenerator;
import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.changes.templates.Phrases;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoader;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoaderType;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import net.didion.jwnl.JWNLException;
import processToText.contentDetermination.extraction.GatewayExtractor;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.AbstractFragment;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;
import processToText.dataModel.process.*;
import processToText.textPlanning.recordClasses.ConverterRecord;
import processToText.textPlanning.recordClasses.ModifierRecord;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextToIntermediateConverter {

    private RPST<ControlFlow, Node> rpst;
    private ProcessModel process;
    private EnglishLabelHelper lHelper;
    private boolean imperative;
    private String imperativeRole;
    private TemplateLoader templateLoader;

    public TextToIntermediateConverter(RPST<ControlFlow, Node> rpst,
                                       ProcessModel process, EnglishLabelHelper lHelper,
                                       String imperativeRole, boolean imperative)
            throws FileNotFoundException, JWNLException {
        this.rpst = rpst;
        this.process = process;
        this.lHelper = lHelper;
        this.imperative = imperative;
        this.imperativeRole = imperativeRole;
        this.templateLoader = new TemplateLoader();
    }

    // *********************************************************************************************
    // OR - SPLIT
    // *********************************************************************************************

    // The following optional parallel paths are available.

    public ConverterRecord convertORSimple(RPSTNode<ControlFlow, Node> node, GatewayExtractor gwExtractor, boolean labeled, int amountProcedures) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.OR, modificationMap);

        ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
        modRecord2.addAttribute("adv-type", "sentential");
        eFrag.addMod(templateLoader.getAddition(), modRecord2);
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;
        eFrag.verb_IsPassive = true;
        eFrag.add_hasArticle = false;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
        preStatements.add(new DSynTMainSentence(eFrag));

        // JOIN
        ConditionFragment post = FragmentGenerator.generateConditionFragment(TemplateLoaderType.OR, modificationMap, ConditionFragment.TYPE_ONCE);
        post.verb_isPast = true;
        post.verb_IsPassive = true;
        post.bo_isSubject = true;
        post.bo_isPlural = false;
        post.bo_hasArticle = false;
        post.setFragmentType(AbstractFragment.TYPE_JOIN);
        post.addAssociation(Integer.valueOf(node.getEntry().getId()));

        return new ConverterRecord(null, post, preStatements, null);
    }

    // *********************************************************************************************
    // XOR - SPLIT
    // *********************************************************************************************

    public ArrayList<DSynTSentence> convertXORSimpleYesNo(RPSTNode<ControlFlow, Node> node, GatewayExtractor gwExtractor) {

        ExecutableFragment eFragYes = null;
        ExecutableFragment eFragNo = null;
        String role = "";

        ArrayList<RPSTNode<ControlFlow, Node>> pNodeList = new ArrayList<RPSTNode<ControlFlow, Node>>();
        pNodeList.addAll(rpst.getChildren(node));
        for (RPSTNode<ControlFlow, Node> pNode : pNodeList) {
            for (RPSTNode<ControlFlow, Node> tNode : rpst.getChildren(pNode)) {
                if (tNode.getEntry() == node.getEntry()) {
                    for (Arc arc : process.getArcs().values()) {
                        if (arc.getSource().getId() == Integer.valueOf(tNode.getEntry().getId()) &&
                                arc.getTarget().getId() == Integer.valueOf(tNode.getExit().getId())) {
                            if (arc.getLabel().toLowerCase().equals("yes")) {
                                Activity a = process.getActivity(Integer.valueOf(tNode.getExit().getId()));
                                Annotation anno = a.getAnnotations().get(0);
                                String action = anno.getActions().get(0);
                                String bo = anno.getBusinessObjects().get(0);
                                role = a.getLane().getName();
                                // role = getRole(tNode);
                                String addition = anno.getAddition();

                                eFragYes = new ExecutableFragment(action, bo, role, addition);
                                eFragYes.addAssociation(Integer.valueOf(node.getExit().getId()));
                            }
                            if (arc.getLabel().toLowerCase().equals("no")) {
                                Activity a = process.getActivity(Integer.valueOf(tNode.getExit().getId()));
                                Annotation anno = a.getAnnotations().get(0);
                                String action = anno.getActions().get(0);
                                String bo = anno.getBusinessObjects().get(0);
                                role = a.getLane().getName();
                                // role = getRole(tNode);
                                String addition = anno.getAddition();

                                eFragNo = new ExecutableFragment(action, bo, role, addition);
                                ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
                                modRecord.addAttribute("adv-type", "sentential");
                                eFragNo.addMod("otherwise", modRecord);
                                eFragNo.sen_hasConnective = true;
                                eFragNo.addAssociation(Integer.valueOf(node.getExit().getId()));
                            }
                        }
                    }
                }
            }
        }

        ConditionFragment cFrag = new ConditionFragment(gwExtractor.getVerb(),
                gwExtractor.getObject(), "", "", ConditionFragment.TYPE_IF,
                gwExtractor.getModList());
        cFrag.bo_replaceWithPronoun = true;
        cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

        // If imperative mode
        if (imperative && imperativeRole.equals(role)) {
            eFragNo.setRole("");
            eFragNo.verb_isImperative = true;
            eFragYes.setRole("");
            eFragYes.verb_isImperative = true;
        }

        DSynTConditionSentence dsyntSentence1 = new DSynTConditionSentence(eFragYes, cFrag);
        DSynTMainSentence dsyntSentence2 = new DSynTMainSentence(eFragNo);
        ArrayList<DSynTSentence> sentences = new ArrayList<DSynTSentence>();
        sentences.add(dsyntSentence1);
        sentences.add(dsyntSentence2);

        return sentences;
    }

    public DSynTMainSentence convertXORSimple(RPSTNode<ControlFlow, Node> node, Map<Integer, String> bpmnIdMap) {

        List<DSynTMainSentence> dSynTSentenceList = new ArrayList<>();
        String role = "";
        boolean isFirst = true;

        ArrayList<RPSTNode<ControlFlow, Node>> pNodeList = new ArrayList<>(rpst.getChildren(node));
        for (RPSTNode<ControlFlow, Node> pNode : pNodeList) {
            for (RPSTNode<ControlFlow, Node> tNode : rpst.getChildren(pNode)) {
                if (tNode.getEntry() == node.getEntry()) {
                    for (Arc arc : process.getArcs().values()) {
                        if (arc.getSource().getId() == Integer.valueOf(tNode.getEntry().getId()) && arc.getTarget().getId() == Integer.valueOf(tNode.getExit().getId())) {
                            Activity activity = process.getActivity(Integer.valueOf(tNode.getExit().getId()));
                            Annotation anno = activity.getAnnotations().get(0);
                            String action = anno.getActions().get(0);
                            String bo = anno.getBusinessObjects().get(0);
                            role = activity.getLane().getName();
                            String addition = anno.getAddition();

                            if (isFirst) {
                                addition = "can either";
                            }

                            ExecutableFragment eFrag = new ExecutableFragment(action, bo, role, addition);

                            if (isFirst) {
                                eFrag.add_hasArticle = false;
                                isFirst = false;
                            }

                            eFrag.addAssociation(Integer.valueOf(node.getExit().getId()));

                            if (imperative && imperativeRole.equals(role)) {
                                eFrag.setRole("");
                                eFrag.verb_isImperative = true;
                            }

                            DSynTMainSentence dSynTMainSentence = new DSynTMainSentence(eFrag);
                            String processElementId = bpmnIdMap.getOrDefault(activity.getId(), "none");
                            dSynTMainSentence.addProcessElementDocument(processElementId, ProcessElementType.ACTIVITY);

                            dSynTSentenceList.add(dSynTMainSentence);
                        }
                    }
                }
            }
        }

        DSynTMainSentence dSynTMainSentence = dSynTSentenceList.get(0);

        dSynTMainSentence.addCoordSentences(dSynTSentenceList.get(1), Lexemes.SIMPLE_XOR_AGGREGATION_CONNECTIVE, false);

        return dSynTMainSentence;
    }

    public ConverterRecord convertXORGeneral(RPSTNode<ControlFlow, Node> node, int amountProcedures) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.XOR, modificationMap);
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;
        eFrag.verb_IsPassive = true;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
        preStatements.add(new DSynTMainSentence(eFrag));

        // Statement about negative case (process is finished)
        // JOIN
        ConditionFragment post = FragmentGenerator.generateConditionFragment(TemplateLoaderType.XOR, modificationMap, ConditionFragment.TYPE_ONCE);
        post.verb_isPast = true;
        post.verb_IsPassive = true;
        post.bo_isSubject = true;
        post.bo_isPlural = false;
        post.bo_hasArticle = false;
        post.setFragmentType(AbstractFragment.TYPE_JOIN);
        post.addAssociation(Integer.valueOf(node.getEntry().getId()));

        return new ConverterRecord(null, post, preStatements, null, null);
    }

    // *********************************************************************************************
    // LOOP - SPLIT
    // *********************************************************************************************

    /**
     * Converts a loop construct with labeled entry condition into two
     * sentences.
     */
    public ConverterRecord convertLoop(RPSTNode<ControlFlow, Node> node, RPSTNode<ControlFlow, Node> firstActivity) {
        if (node.getExit().getName().equals("") == false) {
            return convertLoopLabeledCase(node, firstActivity);
        } else {
            return convertLoopUnlabeledCase(node, firstActivity);
        }
    }

    private ConverterRecord convertLoopLabeledCase(RPSTNode<ControlFlow, Node> node, RPSTNode<ControlFlow, Node> firstActivity) {
        // Derive information from the gateway
        GatewayExtractor gwExtractor = new GatewayExtractor(node.getExit(), lHelper);

        // Generate general statement about loop
        templateLoader.loadTemplate(TemplateLoaderType.LOOP_SPLIT);
        String role = getRole(node);
        ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(), templateLoader.getObject(), role, "");
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
        eFrag.addMod(templateLoader.getAddition(), modRecord);
        eFrag.bo_isPlural = true;

        ExecutableFragment eFrag2 = new ExecutableFragment("continue", "", "", "");
        eFrag2.addAssociation(Integer.valueOf(node.getEntry().getId()));
        eFrag.addSentence(eFrag2);
        if (role.equals("")) {
            eFrag.verb_IsPassive = true;
            eFrag.bo_isSubject = true;
            eFrag2.verb_IsPassive = true;
            eFrag2.setBo("it");
            eFrag2.bo_isSubject = true;
            eFrag2.bo_hasArticle = false;
        }

        role = "";
        ConditionFragment cFrag = null;
        Activity a = process.getActivity(Integer.valueOf(firstActivity.getExit().getId()));
        Event e = process.getEvent(Integer.valueOf(firstActivity.getExit().getId()));
        Gateway g = process.getGateway(Integer.valueOf(firstActivity.getExit().getId()));
        if (a != null) {
            role = a.getLane().getName();
            if (role.equals("")) {
                role = a.getPool().getName();
            }
            ExecutableFragment eFrag3 = new ExecutableFragment(a
                    .getAnnotations().get(0).getActions().get(0), a
                    .getAnnotations().get(0).getBusinessObjects().get(0),
                    "", "");
            eFrag3.addAssociation(a.getId());
            eFrag3.sen_isCoord = false;
            eFrag3.verb_isParticiple = true;
            ModifierRecord modRecord2 = new ModifierRecord(
                    ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
            modRecord2.addAttribute("adv-type", "sentential");
            eFrag3.addMod("with", modRecord2);
            eFrag2.addSentence(eFrag3);

            cFrag = new ConditionFragment(gwExtractor.getVerb(),
                    gwExtractor.getObject(), "", "",
                    ConditionFragment.TYPE_AS_LONG_AS,
                    new HashMap<String, ModifierRecord>(
                            gwExtractor.getModList()));
            cFrag.verb_IsPassive = true;
            cFrag.bo_isSubject = true;
            cFrag.sen_headPosition = true;
            cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        } else if (e != null) {
            role = e.getLane().getName();
            if (role.equals("")) {
                role = e.getPool().getName();
            }

            ExecutableFragment eFrag3 = new ExecutableFragment("continue", "loop", "", "");
            eFrag3.addAssociation(e.getId());
            eFrag3.sen_isCoord = false;
            eFrag3.verb_isParticiple = true;
            ModifierRecord modRecord2 = new ModifierRecord(
                    ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
            modRecord2.addAttribute("adv-type", "sentential");
            eFrag3.addMod("with", modRecord2);
            eFrag2.addSentence(eFrag3);

            cFrag = new ConditionFragment(gwExtractor.getVerb(),
                    gwExtractor.getObject(), "", "",
                    ConditionFragment.TYPE_AS_LONG_AS,
                    new HashMap<String, ModifierRecord>(
                            gwExtractor.getModList()));
            cFrag.verb_IsPassive = true;
            cFrag.bo_isSubject = true;
            cFrag.sen_headPosition = true;
            cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        } else {
            // Gateway

            role = g.getLane().getName();
            if (role.equals("")) {
                role = g.getPool().getName();
            }

            ExecutableFragment eFrag3 = new ExecutableFragment("repeat", "loop", "", "");
            eFrag3.addAssociation(g.getId());
            eFrag3.sen_isCoord = false;
            eFrag3.verb_isParticiple = true;
            eFrag2.addSentence(eFrag3);
            cFrag = new ConditionFragment(gwExtractor.getVerb(),
                    gwExtractor.getObject(), "", "",
                    ConditionFragment.TYPE_AS_LONG_AS,
                    new HashMap<String, ModifierRecord>(
                            gwExtractor.getModList()));
            cFrag.verb_IsPassive = true;
            cFrag.bo_isSubject = true;
            cFrag.sen_headPosition = true;
            cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        }

        // Determine postcondition
        gwExtractor.negateGatewayLabel();
        ConditionFragment post = new ConditionFragment(
                gwExtractor.getVerb(), gwExtractor.getObject(), "", "",
                ConditionFragment.TYPE_ONCE, gwExtractor.getModList());
        post.verb_IsPassive = true;
        post.bo_isSubject = true;
        post.setFragmentType(AbstractFragment.TYPE_JOIN);
        post.addAssociation(Integer.valueOf(node.getEntry().getId()));

        // If imperative mode
        if (imperative == true && imperativeRole.equals(role) == true) {
            eFrag.setRole("");
            eFrag.verb_isImperative = true;
            eFrag2.verb_isImperative = true;
        }

        ArrayList<DSynTSentence> postStatements = new ArrayList<DSynTSentence>();
        postStatements.add(new DSynTConditionSentence(eFrag, cFrag));
        return new ConverterRecord(null, post, null, postStatements);
    }

    private ConverterRecord convertLoopUnlabeledCase(RPSTNode<ControlFlow, Node> node, RPSTNode<ControlFlow, Node> firstActivity) {

        templateLoader.loadTemplate(TemplateLoaderType.LOOP_SPLIT);
        ExecutableFragment eFrag = new ExecutableFragment(templateLoader.getAction(), templateLoader.getObject(), "", "");
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
        eFrag.addMod(templateLoader.getAddition(), modRecord);
        eFrag.bo_isPlural = true;
        eFrag.bo_isSubject = true;
        eFrag.verb_IsPassive = true;

        ConditionFragment cFrag = new ConditionFragment("require", "dummy", "", "", ConditionFragment.TYPE_IF, new HashMap<>());
        cFrag.bo_replaceWithPronoun = true;
        cFrag.verb_IsPassive = true;
        cFrag.bo_isSubject = true;
        cFrag.sen_headPosition = true;

        // In that case the Resource 1 continues with do the activity 2.
        // Determine repetition
        ExecutableFragment eFrag2 = new ExecutableFragment("continue", "", "", "");
        String role = "";
        Activity activity = process.getActivity(Integer.valueOf(firstActivity.getExit().getId()));
        if (activity != null) {
            role = activity.getLane().getName();
            if (role.equals("")) {
                role = activity.getPool().getName();
            }
            eFrag2.setRole(role);
            ModifierRecord modRecord3 = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
            modRecord3.addAttribute("adv-type", "sentential");
            eFrag2.addMod("in that case", modRecord3);
            eFrag2.sen_hasConnective = true;

            Annotation annotation = activity.getAnnotations().get(0);
            ExecutableFragment eFrag3 = new ExecutableFragment(annotation.getActions().get(0), annotation.getBusinessObjects().get(0), "", annotation.getAddition());
            eFrag3.sen_isCoord = false;
            eFrag3.verb_isParticiple = true;

            ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
            modRecord2.addAttribute("adv-type", "sentential");
            eFrag3.addMod("with", modRecord2);
            eFrag2.addSentence(eFrag3);
        } else {
            eFrag2 = null;
        }

        // Determine postcondition
        templateLoader.loadTemplate(TemplateLoaderType.LOOP_JOIN);
        ConditionFragment post = new ConditionFragment(templateLoader.getAction(), templateLoader.getObject(), "", "", ConditionFragment.TYPE_ONCE, new HashMap<>());
        post.verb_IsPassive = true;
        post.bo_isSubject = true;
        post.setFragmentType(AbstractFragment.TYPE_JOIN);

        // If imperative mode
        if (imperative && imperativeRole.equals(role)) {
            eFrag.setRole("");
            eFrag.verb_isImperative = true;
            if (eFrag2 != null) {
                eFrag2.verb_isImperative = true;
            }
        }

        ArrayList<DSynTSentence> postStatements = new ArrayList<>();
        postStatements.add(new DSynTConditionSentence(eFrag, cFrag));
        if (eFrag2 != null) {
            postStatements.add(new DSynTMainSentence(eFrag2));
        }

        return new ConverterRecord(null, post, null, postStatements);
    }

    // *********************************************************************************************
    // SKIP - SPLIT
    // *********************************************************************************************

    public ConverterRecord convertSkipGeneralUnlabeled(RPSTNode<ControlFlow, Node> node) {
        Map<String, String> modificationMap = new HashMap<>();
        ConditionFragment pre = FragmentGenerator.generateConditionFragment(TemplateLoaderType.SKIP, modificationMap, ConditionFragment.TYPE_IF);

        ModifierRecord mod = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        pre.addMod(templateLoader.getAddition(), mod);
        //pre.bo_replaceWithPronoun = true;
        //pre.sen_headPosition = true;
        //pre.sen_isCoord = true;
        //pre.sen_hasComma = true;
        //pre.verb_IsPassive = true;

        pre.add_hasArticle = false;
        pre.sen_hasComma = true;
        pre.skip = true;

        pre.addAssociation(Integer.valueOf(node.getEntry().getId()));

        return new ConverterRecord(pre, null, null, null);

    }

    public ConverterRecord convertSkipGeneralUnlabeled(RPSTNode<ControlFlow, Node> node, int amountProcedures, boolean isXorSkip) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ConditionFragment pre = FragmentGenerator.generateConditionFragment(TemplateLoaderType.SKIP, modificationMap, ConditionFragment.TYPE_IF);

        ModifierRecord mod = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        pre.addMod(templateLoader.getAddition(), mod);

        pre.add_hasArticle = false;
        pre.sen_hasComma = true;
        pre.skip = true;

        pre.addAssociation(Integer.valueOf(node.getEntry().getId()));

        ExecutableFragment eFrag;
        eFrag = isXorSkip ? getXorEFrag(node, amountProcedures) : getOrEFrag(node, amountProcedures);

        ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
        preStatements.add(new DSynTConditionSentence(eFrag, pre));

        return new ConverterRecord(null, null, preStatements, null);

    }

    public ExecutableFragment getXorEFrag(RPSTNode<ControlFlow, Node> node, int amountProcedures) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.XOR, modificationMap);
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;
        eFrag.verb_IsPassive = true;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        return eFrag;
    }

    public ExecutableFragment getOrEFrag(RPSTNode<ControlFlow, Node> node, int amountProcedures) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_ADJ, ModifierRecord.TARGET_BO);
        modRecord2.addAttribute("adv-type", "sentential");

        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.OR, modificationMap);
        eFrag.addMod(templateLoader.getAddition(), modRecord2);
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;
        eFrag.verb_IsPassive = true;
        eFrag.add_hasArticle = false;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        return eFrag;
    }

    /**
     * Converts a standard skip construct with labeled condition gateway into
     * two sentences.
     */
    public ConverterRecord convertSkipGeneral(RPSTNode<ControlFlow, Node> node) {
        // Derive information from the gateway
        GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);

        // Generate general statement about upcoming decision
        ConditionFragment pre = new ConditionFragment(gwExtractor.getVerb(),
                gwExtractor.getObject(), "", "",
                ConditionFragment.TYPE_IN_CASE, gwExtractor.getModList());
        pre.verb_IsPassive = gwExtractor.hasVerb != false;
        pre.bo_isSubject = true;
        pre.sen_headPosition = true;
        pre.bo_isPlural = gwExtractor.bo_isPlural;
        pre.bo_hasArticle = gwExtractor.bo_hasArticle;
        pre.addAssociation(Integer.valueOf(node.getEntry().getId()));
        return new ConverterRecord(pre, null, null, null);
    }

    /**
     * Converts a standard skip construct with labeled condition gateway,
     * leading to the end of the process, into two sentences.
     */
    public ConverterRecord convertSkipToEnd(RPSTNode<ControlFlow, Node> node) {

        // Derive information from the gateway
        GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);
        String role = getRole(node);

        // Generate general statement about upcoming decision
        ExecutableFragment eFrag = new ExecutableFragment("decide", "", role, "");
        ConditionFragment cFrag = new ConditionFragment(gwExtractor.getVerb(),
                gwExtractor.getObject(), "", "",
                ConditionFragment.TYPE_WHETHER, gwExtractor.getModList());
        cFrag.verb_IsPassive = true;
        cFrag.bo_isSubject = true;
        cFrag.sen_headPosition = false;
        cFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));

        if (role.equals("")) {
            eFrag.verb_IsPassive = true;
            eFrag.setBo("it");
            eFrag.bo_hasArticle = false;
            eFrag.bo_isSubject = true;

            cFrag.verb_IsPassive = true;
            cFrag.setBo("it");
            cFrag.bo_hasArticle = false;
            cFrag.bo_isSubject = true;
        }

        // Statement about negative case (process is finished)
        ExecutableFragment eFrag2 = new ExecutableFragment("end", "process instance", "", "");
        //eFrag2.verb_IsPassive = true;
        eFrag2.bo_isSubject = true;
        ConditionFragment cFrag2 = new ConditionFragment("be", "case", "this", "", ConditionFragment.TYPE_IF, new HashMap<>());
        cFrag2.verb_isNegated = true;

        // Determine precondition
        ConditionFragment pre = new ConditionFragment(gwExtractor.getVerb(), gwExtractor.getObject(), "", "", ConditionFragment.TYPE_IF, new HashMap<>());
        pre.verb_IsPassive = true;
        pre.sen_headPosition = true;
        pre.bo_isSubject = true;
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_PREP,
                ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");
        pre.addMod("otherwise", modRecord);
        pre.sen_hasConnective = true;

        // If imperative mode
        if (imperative == true && imperativeRole.equals(role) == true) {
            eFrag.setRole("");
            eFrag.verb_isImperative = true;
        }

        ArrayList<DSynTSentence> preStatements = new ArrayList<DSynTSentence>();
        preStatements.add(new DSynTConditionSentence(eFrag, cFrag));
        preStatements.add(new DSynTConditionSentence(eFrag2, cFrag2));

        return new ConverterRecord(pre, null, preStatements, null);
    }

    // *********************************************************************************************
    // AND - SPLIT
    // *********************************************************************************************

    public ConverterRecord convertANDGeneral(RPSTNode<ControlFlow, Node> node, int amountProcedures) {
        Map<String, String> modificationMap = new HashMap<>();
        modificationMap.put("@number", Integer.toString(amountProcedures));

        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.AND_SPLIT, modificationMap);
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;
        eFrag.bo_isPlural = true;
        eFrag.verb_IsPassive = true;
        eFrag.add_hasArticle = false;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        ArrayList<DSynTSentence> preStatements = new ArrayList<>();
        preStatements.add(new DSynTMainSentence(eFrag));

        // Statement about negative case (process is finished)
        // JOIN
        ConditionFragment post = FragmentGenerator.generateConditionFragment(TemplateLoaderType.AND_JOIN, modificationMap, ConditionFragment.TYPE_AFTER);
        post.bo_isSubject = true;
        post.bo_isPlural = true;
        post.bo_hasArticle = false;
        post.sen_hasComma = true;
        post.addAssociation(Integer.valueOf(node.getEntry().getId()));
        post.setFragmentType(AbstractFragment.TYPE_JOIN);

        return new ConverterRecord(null, post, preStatements, null, null);
    }

    public ConverterRecord convertANDSimple(RPSTNode<ControlFlow, Node> node, int activities, ArrayList<Node> conditionNodes) {

        // get last element of both branches and combine them to a post condition
        // if one of them is a gateway, include gateway post condition in the and post condition

        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");

        if (activities == 1) {
            modRecord.setLemma(Phrases.AND_SIMPLE_SINGLE);
        } else {
            modRecord.setLemma(Phrases.AND_SIMPLE_MULT.replace("@number", Integer.toString(activities)));
        }

        // Determine postcondition
        ConditionFragment post = null;
        String role = "";

        // Check whether postcondition should be passed
        int arcs = 0;
        for (Arc arc : process.getArcs().values()) {
            if (arc.getTarget().getId() == Integer.valueOf(node.getExit()
                    .getId())) {
                arcs++;
            }
        }

        // Only if no other arc flows into join gateway, join condition is passed
        if (arcs == 2) {
            templateLoader.loadTemplate(TemplateLoaderType.AND_JOIN_SIMPLE);
            if (conditionNodes.size() == 1) {
                Activity a = process.getActivity(Integer.valueOf(conditionNodes.get(0).getId()));
                String verb = a.getAnnotations().get(0).getActions().get(0);
                role = getRole(node);
                post = new ConditionFragment(templateLoader.getAction(), lHelper.getNoun(verb),
                        role, "", ConditionFragment.TYPE_ONCE,
                        new HashMap<String, ModifierRecord>());
                post.sen_headPosition = true;
                post.verb_isPast = true;
                post.setFragmentType(AbstractFragment.TYPE_JOIN);
                post.addAssociation(Integer.valueOf(node.getEntry().getId()));
            } else {
                post = new ConditionFragment(templateLoader.getAction(), templateLoader.getObject(), "", "",
                        ConditionFragment.TYPE_ONCE,
                        new HashMap<String, ModifierRecord>());
                post.bo_isPlural = true;
                post.sen_headPosition = true;
                post.bo_hasArticle = false;
                post.bo_isSubject = true;
                post.verb_isPast = true;
                post.verb_IsPassive = true;
                post.setFragmentType(AbstractFragment.TYPE_JOIN);
                post.addAssociation(Integer.valueOf(node.getEntry().getId()));
            }
        }

        // If imperative mode
        if (imperative == true && imperativeRole.equals(role) == true) {
            post.role_isImperative = true;
        }

        return new ConverterRecord(null, post, null, null, modRecord);
    }

    // ***************************************************************
    // EVENTS
    // ***************************************************************

    public ConverterRecord convertEvent(Event event) {

        String role = event.getLane().getName();
        if (role.equals("")) {
            role = event.getPool().getName();
        }

        switch (event.getType()) {
            case EventType.INTM_ERROR:
                return handleIntermediateError(event);
            case EventType.INTM_TIMER:
                return handleIntermediateTimer(event, role);
            case EventType.INTM_MSG_CAT:
                return handleIntermediateMessageCatch(event, role);
            case EventType.INTM_ESCALATION_CAT:
                return handleIntermediateEscalationCatch(event);

            case EventType.START_EVENT:
                return handleStartEvent();
            case EventType.START_MSG:
                return handleStartEventMessage();
            case EventType.START_TIMER:
                return handleStartEventTimer();
            case EventType.END_EVENT:
                return handleEndEvent(event);
            case EventType.END_MSG:
                return handleEndEventMessage();
            case EventType.END_ERROR:
                return handleEndEventError(event);
            case EventType.END_SIGNAL:
                return handleEndEventSignal();

            case EventType.INTM_MSG_THR:
                return handleIntermediateMessageThrow(event);
            case EventType.INTM_ESCALATION_THR:
                return handleIntermediateEscalationThrow(event);
            case EventType.INTM_LINK_THR:
                return handleIntermediateLinkThrow(event);
            case EventType.INTM_MULTIPLE_THR:
                return handleIntermediateMultipleThrow(event);
            case EventType.INTM_SIGNAL_THR:
                return handleIntermediateSignalThrow(event);

            default:
                System.out.println("NON-COVERED EVENT " + event.getType());
                return null;
        }
    }

    private ConverterRecord handleIntermediateError(Event event) {
        String error = event.getLabel();
        ExecutableFragment eFrag = null;
        ConditionFragment cFrag = null;

        if (error.equals("")) {
            cFrag = new ConditionFragment("occur", "error", "", "", ConditionFragment.TYPE_IF, new HashMap<>());
            cFrag.bo_hasIndefArticle = true;
        } else {
            cFrag = new ConditionFragment("occur", "error '" + error + "'", "", "", ConditionFragment.TYPE_IF, new HashMap<>());
            cFrag.bo_hasArticle = true;
        }
        cFrag.bo_isSubject = true;
        if (event.isAttached()) {
            cFrag.setAddition("while latter task is executed,");
        }

        return handleCatchEvent(event, eFrag, cFrag);
    }

    private ConverterRecord handleIntermediateTimer(Event event, String role) {
        String limit = event.getLabel();
        ExecutableFragment eFrag = null;
        ConditionFragment cFrag = null;

        if (limit.equals("")) {
            eFrag = new ExecutableFragment("wait", "up to a certain time", role, "", new HashMap<>());
            eFrag.bo_hasArticle = false;
        } else {
            eFrag = new ExecutableFragment("wait", "up to the time limit of " + limit, role, "", new HashMap<>());
            eFrag.bo_hasArticle = false;
        }
        if (event.isAttached()) {
            cFrag = new ConditionFragment("reach", "time limit", "", "", ConditionFragment.TYPE_IN_CASE, new HashMap<>());
            cFrag.bo_hasArticle = true;
            cFrag.bo_isSubject = true;
            cFrag.setAddition("while latter task is executed,");
        }

        return handleCatchEvent(event, eFrag, cFrag);
    }

    private ConverterRecord handleIntermediateMessageCatch(Event event, String role) {
        ExecutableFragment eFrag = new ExecutableFragment("receive", "a message", role, "", new HashMap<>());
        eFrag.bo_hasArticle = false;

        ConditionFragment cFrag = null;

        return handleCatchEvent(event, eFrag, cFrag);
    }

    private ConverterRecord handleIntermediateEscalationCatch(Event event) {
        ExecutableFragment eFrag = null;

        ConditionFragment cFrag = new ConditionFragment("", "of an escalation", "", "", ConditionFragment.TYPE_IN_CASE, new HashMap<>());
        cFrag.bo_hasArticle = false;
        cFrag.bo_isSubject = true;

        return handleCatchEvent(event, eFrag, cFrag);
    }

    private ConverterRecord handleCatchEvent(Event event, ExecutableFragment eFrag, ConditionFragment cFrag) {
        ArrayList<DSynTSentence> preSentences = new ArrayList<>();

        // Attached Event
        if (event.isAttached()) {
            preSentences.add(getAttachedEventSentence(event, cFrag));
            return new ConverterRecord(null, null, preSentences, null);

            // Non-attached Event
        } else {
            preSentences = new ArrayList<>();
            if (cFrag != null) {
                preSentences.add(getIntermediateEventSentence(cFrag));
            } else {
                preSentences.add(new DSynTMainSentence(eFrag));
            }
            return new ConverterRecord(null, null, preSentences, null);
        }
    }

    private ConverterRecord handleStartEvent() {
        ExecutableFragment eFrag = new ExecutableFragment("contain", "subprocess", "", "the following steps");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;
        eFrag.add_hasArticle = false;
        eFrag.sen_hasBullet = true;
        eFrag.isIndividualSentence = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleStartEventMessage() {
        ConditionFragment cFrag = new ConditionFragment("receive", "message", "", "", ConditionFragment.TYPE_ONCE);
        cFrag.bo_isSubject = true;
        cFrag.verb_IsPassive = true;
        cFrag.bo_hasArticle = true;
        cFrag.bo_hasIndefArticle = true;

        ExecutableFragment eFrag = new ExecutableFragment("start", "process", "", "");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;

        return getEventSentence(eFrag, cFrag);
    }

    private ConverterRecord handleStartEventTimer() {
        ConditionFragment cFrag = new ConditionFragment("fulfill", "time condition", "", "", ConditionFragment.TYPE_ONCE);
        cFrag.bo_isSubject = true;
        cFrag.verb_IsPassive = true;
        cFrag.bo_hasArticle = true;
        cFrag.bo_hasIndefArticle = true;

        ExecutableFragment eFrag = new ExecutableFragment("start", "process", "", "");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;

        return getEventSentence(eFrag, cFrag);
    }

    private ConverterRecord handleEndEvent(Event event) {
        ExecutableFragment eFrag;
        if (event.getSubProcessID() > 0) {
            eFrag = new ExecutableFragment("end", "subprocess", "", "");
        } else {
            eFrag = new ExecutableFragment("end", "process", "", "");
        }
        //eFrag.verb_IsPassive = true;
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleEndEventMessage() {
        ExecutableFragment eFrag = new ExecutableFragment("end", "process", "", "with a message");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;
        eFrag.add_hasArticle = false;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleEndEventError(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("end", "process", "", "with an error");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;
        eFrag.add_hasArticle = false;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleEndEventSignal() {
        ExecutableFragment eFrag = new ExecutableFragment("end", "process", "", "with a signal.");
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = true;
        eFrag.add_hasArticle = false;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleIntermediateMessageThrow(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("send", "message", event.getLane().getName(), "");
        eFrag.bo_hasIndefArticle = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleIntermediateEscalationThrow(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("trigger", "escalation", event.getLane().getName(), "");
        eFrag.bo_hasIndefArticle = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleIntermediateLinkThrow(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("send", "signal", event.getLane().getName(), "");
        eFrag.bo_hasIndefArticle = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleIntermediateMultipleThrow(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("cause", "multiple trigger", event.getLane().getName(), "");
        eFrag.bo_hasArticle = false;
        eFrag.bo_isPlural = true;
        return getEventSentence(eFrag);
    }

    private ConverterRecord handleIntermediateSignalThrow(Event event) {
        ExecutableFragment eFrag = new ExecutableFragment("send", "signal", event.getLane().getName(), "");
        eFrag.bo_hasArticle = true;
        eFrag.bo_hasIndefArticle = true;
        eFrag.bo_isPlural = true;
        return getEventSentence(eFrag);
    }

    // *********************************************************************************************
    // Attached Event
    // *********************************************************************************************

    /**
     * Returns Sentence for attached Event.
     */

    private DSynTConditionSentence getAttachedEventSentence(Event event, ConditionFragment cFrag) {
        ExecutableFragment eFrag = new ExecutableFragment("cancel", "it", "", "");
        eFrag.verb_IsPassive = true;
        eFrag.bo_isSubject = true;
        eFrag.bo_hasArticle = false;

        // if (event.isLeadsToEnd() == false) {
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV,
                ModifierRecord.TARGET_VERB);
        ExecutableFragment eFrag2 = new ExecutableFragment("continue",
                "process", "", "");
        modRecord.addAttribute("adv-type", "sent-final");
        modRecord.addAttribute("rheme", "+");
        eFrag2.addMod("as follows", modRecord);

        eFrag2.bo_isSubject = true;
        eFrag.addSentence(eFrag2);
        DSynTConditionSentence sen = new DSynTConditionSentence(eFrag, cFrag);
        return sen;
    }

    public DSynTConditionSentence getAttachedEventPostStatement(Event event) {
        switch (event.getType()) {
            case EventType.INTM_TIMER:
                return handleIntermediateTimerAttached();
            case EventType.INTM_ERROR:
                return handleIntermediateErrorAttached();
            case EventType.INTM_ESCALATION_CAT:
                return handleIntermediateEscalationCatchAttached();
            default:
                System.out.println("NON-COVERED EVENT " + event.getType());
                return null;
        }
    }

    private DSynTConditionSentence handleIntermediateTimerAttached() {
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
        eFrag.bo_isSubject = true;
        modRecord.addAttribute("adv-type", "sent-final");
        modRecord.addAttribute("rheme", "+");
        eFrag.addMod("normally", modRecord);

        ConditionFragment cFrag = new ConditionFragment("complete", "the task", "", "within the time limit", ConditionFragment.TYPE_IF, new HashMap<>());
        cFrag.sen_hasConnective = true;
        cFrag.add_hasArticle = false;
        ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP, ModifierRecord.TARGET_VERB);
        modRecord2.addAttribute("adv-type", "sentential");
        cFrag.addMod("otherwise", modRecord2);
        configureFragment(cFrag);

        return new DSynTConditionSentence(eFrag, cFrag);
    }

    private DSynTConditionSentence handleIntermediateErrorAttached() {
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
        eFrag.bo_isSubject = true;
        modRecord.addAttribute("adv-type", "sent-final");
        modRecord.addAttribute("rheme", "+");
        eFrag.addMod("normally", modRecord);

        ConditionFragment cFrag = new ConditionFragment("complete", "the task", "", "without error", ConditionFragment.TYPE_IF, new HashMap<>());
        cFrag.sen_hasConnective = true;
        cFrag.add_hasArticle = false;
        ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP, ModifierRecord.TARGET_VERB);
        modRecord2.addAttribute("adv-type", "sentential");
        cFrag.addMod("otherwise", modRecord2);
        configureFragment(cFrag);

        return new DSynTConditionSentence(eFrag, cFrag);
    }

    private DSynTConditionSentence handleIntermediateEscalationCatchAttached() {
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
        eFrag.bo_isSubject = true;
        modRecord.addAttribute("adv-type", "sent-final");
        modRecord.addAttribute("rheme", "+");
        eFrag.addMod("normally", modRecord);

        ConditionFragment cFrag = new ConditionFragment("complete", "the task", "", "without escalation", ConditionFragment.TYPE_IF, new HashMap<>());
        cFrag.sen_hasConnective = true;
        cFrag.add_hasArticle = false;
        ModifierRecord modRecord2 = new ModifierRecord(ModifierRecord.TYPE_PREP, ModifierRecord.TARGET_VERB);
        modRecord2.addAttribute("adv-type", "sentential");
        cFrag.addMod("otherwise", modRecord2);
        configureFragment(cFrag);

        return new DSynTConditionSentence(eFrag, cFrag);
    }


    // *********************************************************************************************
    // Event Sentence
    // *********************************************************************************************

    /**
     * Returns record with sentence for throwing events.
     */
    private ConverterRecord getEventSentence(ExecutableFragment eFrag) {
        DSynTMainSentence msen = new DSynTMainSentence(eFrag);
        ArrayList<DSynTSentence> preSentences = new ArrayList<>();
        preSentences.add(msen);
        return new ConverterRecord(null, null, preSentences, null);
    }

    private ConverterRecord getEventSentence(ExecutableFragment eFrag, ConditionFragment cFrag) {
        DSynTConditionSentence msen = new DSynTConditionSentence(eFrag, cFrag);
        ArrayList<DSynTSentence> preSentences = new ArrayList<>();
        preSentences.add(msen);
        return new ConverterRecord(null, null, preSentences, null);
    }

    /**
     * Returns sentence for intermediate events.
     */
    private DSynTConditionSentence getIntermediateEventSentence(ConditionFragment cFrag) {
        ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
        eFrag.bo_isSubject = true;
        DSynTConditionSentence dSynTConditionSentence = new DSynTConditionSentence(eFrag, cFrag);
        return dSynTConditionSentence;
    }

    // *********************************************************************************************
    // Others
    // *********************************************************************************************

    /**
     * Configures condition fragment in a standard fashion.
     */
    private void configureFragment(ConditionFragment cFrag) {
        cFrag.verb_IsPassive = true;
        cFrag.bo_isSubject = true;
        cFrag.bo_hasArticle = false;
    }

    /**
     * Returns role executing current RPST node.
     */
    private String getRole(RPSTNode<ControlFlow, Node> node) {
        String role = process.getGateways()
                .get(Integer.valueOf(node.getExit().getId())).getLane()
                .getName();
        if (role.equals("")) {
            role = process.getGateways()
                    .get(Integer.valueOf(node.getExit().getId())).getPool()
                    .getName();
        }
        return role;
    }

}
