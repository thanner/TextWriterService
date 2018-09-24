package br.edu.ufrgs.inf.bpm.changes.textPlanning;

import br.edu.ufrgs.inf.bpm.builder.FragmentGenerator;
import br.edu.ufrgs.inf.bpm.changes.templates.Lexemes;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoaderType;
import br.edu.ufrgs.inf.bpm.metatext.ProcessElementType;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import net.didion.jwnl.JWNLException;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TFlowElement;
import org.omg.spec.bpmn._20100524.model.TFlowNode;
import org.omg.spec.bpmn._20100524.model.TGateway;
import org.w3c.dom.Document;
import processToText.contentDetermination.extraction.GatewayExtractor;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTConditionSentence;
import processToText.dataModel.dsynt.DSynTMainSentence;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.intermediate.AbstractFragment;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;
import processToText.dataModel.process.*;
import processToText.preprocessing.FormatConverter;
import processToText.textPlanning.PlanningHelper;
import processToText.textPlanning.recordClasses.ConverterRecord;
import processToText.textPlanning.recordClasses.GatewayPropertyRecord;
import processToText.textPlanning.recordClasses.ModifierRecord;

import java.io.FileNotFoundException;
import java.util.*;

public class TextPlanner {

    private RPST<ControlFlow, Node> rpst;
    private ProcessModel process;
    private static String[] quantifiers = {"a", "the", "all", "any", "more", "most", "none", "some", "such", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
    private EnglishLabelHelper lHelper;
    private TextToIntermediateConverter textToIntermediateConverter;
    private ArrayList<ConditionFragment> passedFragments;
    private ArrayList<DSynTSentence> sentencePlan;
    private EnglishLabelDeriver lDeriver;
    private ArrayList<ModifierRecord> passedMods; // used for Skips
    private String imperativeRole;
    private boolean isImperative;
    private boolean isAlternative;
    private ModifierRecord passedMod = null; // used for AND-Splits
    private boolean isTagWithBullet = false;
    private boolean isStart = true;
    private boolean isEnd = false;
    private Map<Integer, String> bpmnIdMap;
    private String currentStartEventId = "Unknown Start Event id";
    private BpmnWrapper bpmnWrapper;

    public TextPlanner(RPST<ControlFlow, Node> rpst, ProcessModel process, EnglishLabelDeriver lDeriver, EnglishLabelHelper lHelper, String imperativeRole, boolean isImperative, boolean isAlternative, Map<Integer, String> bpmnIdMap, TDefinitions tDefinitions) throws FileNotFoundException, JWNLException {
        this.rpst = rpst;
        this.process = process;
        this.lHelper = lHelper;
        this.lDeriver = lDeriver;
        textToIntermediateConverter = new TextToIntermediateConverter(rpst, process, lHelper, imperativeRole, isImperative);
        passedFragments = new ArrayList<>();
        sentencePlan = new ArrayList<>();
        passedMods = new ArrayList<>();
        this.isImperative = isImperative;
        this.imperativeRole = imperativeRole;
        this.isAlternative = isAlternative;
        this.bpmnIdMap = bpmnIdMap;
        this.bpmnWrapper = new BpmnWrapper(tDefinitions);
        //this.loader = new TemplateLoader();
    }

    // *********************************************************************************************
    // Convert
    // *********************************************************************************************

    public void convertToText(RPSTNode<ControlFlow, Node> root, int level) throws JWNLException, FileNotFoundException {
        // Order nodes of current level with respect to control flow
        ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);
        // For each node of current level
        for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {

            // If we face an isEnd event
            isEnd = (PlanningHelper.isEvent(node.getExit()) && orderedTopNodes.indexOf(node) == orderedTopNodes.size() - 1);
            int depth = PlanningHelper.getDepth(node, rpst);

            // Bond
            if (PlanningHelper.isBond(node)) {
                handleBond(node, orderedTopNodes, level);
                // Rigid
            } else if (PlanningHelper.isRigid(node)) {
                handleRigid(node, level);
                // Activity
            } else if (PlanningHelper.isTask(node.getEntry())) {
                handleActivities(node, level, depth);
                if (PlanningHelper.isEvent(node.getExit())) {
                    handleEndEvent(node, orderedTopNodes, level);
                }
                // Event
            } else if (PlanningHelper.isEvent(node.getEntry())) {
                handleEvent(node, orderedTopNodes, level);
                // Subprocess
            } else {
                if (depth > 0) {
                    convertToText(node, level);
                }
            }

            if (isEnd) {
                handleEndEvent(node, level);
            }

        }
    }

    // *********************************************************************************************
    // HANDLE BOND
    // *********************************************************************************************

    private void handleBond(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) throws FileNotFoundException, JWNLException {
        ConverterRecord convRecord = getConverterRecord(node, orderedTopNodes);

        if (convRecord != null) {
            // Add pre statements
            addBondPreStatements(convRecord, level, node);

            // Pass precondition
            /**
            if (convRecord != null && convRecord.pre != null) {
                if (passedFragments.size() > 0) {
                    if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
                        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.EMPTYSEQUENCEFLOW);
                        eFrag.bo_isSubject = true;
                        DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
                        if (convRecord.post != null) {
                            dsyntSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), convRecord.post.getProcessElementType());
                        }
                        sentencePlan.add(dsyntSentence);
                        passedFragments.clear();
                    }
                }
                passedFragments.add(convRecord.pre);
            }
             **/

            // Convert branches to Text
            convertBondToText(node, level);

            // Add post statement to sentence plan
            addBondPostStatement(convRecord, level, node);

            // Pass post fragment
            if (convRecord.post != null) {
                passedFragments.add(convRecord.post);
            }

            // JOIN
            if (passedFragments.size() > 0) {
                if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
                    if (isNextElementAJoin(convRecord.post)) {
                        ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.EMPTYSEQUENCEFLOW);
                        eFrag.sen_level = level;
                        eFrag.bo_isSubject = true;
                        eFrag.sen_hasConnective = false;
                        eFrag.isJoinSentence = true;

                        DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
                        if (convRecord.post != null) {
                            dsyntSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), convRecord.post.getProcessElementType());
                        }
                        sentencePlan.add(dsyntSentence);
                    }
                    passedFragments.clear();
                }
            }
        }
    }

    // TODO: Próximo elemento é um gateway? (ver método isFollowedByGateway)
    // TODO: 1) int currentPosition = orderedTopNodes.indexOf(node);
    // TODO: 2) PlanningHelper.isBond(orderedTopNodes.get(currentPosition + 1);
    private boolean isNextElementAJoin(ConditionFragment conditionFragment) {
        TFlowElement tFlowElement = bpmnWrapper.getFlowElementById(conditionFragment.getProcessElementId());
        if (tFlowElement instanceof TFlowNode) {
            for (TFlowElement tFlowElementTarget : bpmnWrapper.getFlowNodeTargetList((TFlowNode) tFlowElement)) {
                if (tFlowElementTarget instanceof TGateway) {
                    if (bpmnWrapper.isGatewayJoin((TGateway) tFlowElementTarget)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private ConverterRecord getConverterRecord(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes) {
        ConverterRecord convRecord = null;

        if (PlanningHelper.isLoop(node, rpst)) {
            convRecord = getLoopConverterRecord(node);
            setProcessElementData(convRecord, node, ProcessElementType.XORSPLIT);
        }
        if (PlanningHelper.isSkip(node, rpst)) {
            convRecord = getSkipConverterRecord(orderedTopNodes, node);
            convRecord.pre.setProcessElementType(ProcessElementType.XORSPLIT);
            convRecord.pre.setProcessElementId(getProcessElementId(node.getEntry().getId()));
        }
        if (PlanningHelper.isXORSplit(node, rpst)) {
            convRecord = getXORConverterRecord(node);
            setProcessElementData(convRecord, node, ProcessElementType.XORJOIN);
        }
        if (PlanningHelper.isEventSplit(node, rpst)) {
            convRecord = getXORConverterRecord(node);
            setProcessElementData(convRecord, node, ProcessElementType.GATEWAYBASEDEVENTSPLIT);
        }
        if (PlanningHelper.isORSplit(node, rpst)) {
            convRecord = getORConverterRecord(node);
            setProcessElementData(convRecord, node, ProcessElementType.ORJOIN);
        }
        if (PlanningHelper.isANDSplit(node, rpst)) {
            convRecord = getANDConverterRecord(node);
            setProcessElementData(convRecord, node, ProcessElementType.ANDJOIN);
        }
        return convRecord;
    }

    private ConverterRecord getLoopConverterRecord(RPSTNode<ControlFlow, Node> node) {
        RPSTNode<ControlFlow, Node> firstNodeInLoop = PlanningHelper.getNextNode(node, rpst);
        return textToIntermediateConverter.convertLoop(node, firstNodeInLoop);
    }

    private ConverterRecord getSkipConverterRecord(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
        // Yes-No Case
        if (propRec.isGatewayLabeled() && propRec.hasYNArcs()) {
            // Yes-No Case which is directly leading to the isEnd of the process
            if (isToEndSkip(orderedTopNodes, node)) {
                return textToIntermediateConverter.convertSkipToEnd(node);
                // General Yes/No-Case
            } else {
                return textToIntermediateConverter.convertSkipGeneral(node);
            }
            // General unlabeled Skip
        } else {
            return textToIntermediateConverter.convertSkipGeneralUnlabeled(node);
        }
    }

    // Evaluate whether skip leads to an isEnd
    private boolean isToEndSkip(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
        int currentPosition = orderedTopNodes.indexOf(node);
        if (currentPosition < orderedTopNodes.size() - 1) {
            Node potEndNode = orderedTopNodes.get(currentPosition + 1).getExit();
            return PlanningHelper.isEndEvent(potEndNode, process) == true;
        }
        return false;
    }

    private ConverterRecord getXORConverterRecord(RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord xorPropertyRecord = new GatewayPropertyRecord(node, rpst, process);
        if (xorPropertyRecord.isGatewayLabeled() && xorPropertyRecord.hasYNArcs() && xorPropertyRecord.getMaxPathDepth() == 1) {
            GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);
            // Labeled Case with Yes/No - arcs and Max. Depth of 1
            for (DSynTSentence dSynTSentence : textToIntermediateConverter.convertXORSimple(node, gwExtractor)) {
                dSynTSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), ProcessElementType.XORSPLIT);
                sentencePlan.add(dSynTSentence);
            }
            return null;
            // General case
        } else {
            ArrayList<RPSTNode<ControlFlow, Node>> xorNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            return textToIntermediateConverter.convertXORGeneral(node, xorNodes.size());
        }
    }

    private ConverterRecord getORConverterRecord(RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord orPropertyRecord = new GatewayPropertyRecord(node, rpst, process);
        // Labeled Case
        if (orPropertyRecord.isGatewayLabeled()) {
            return null;
            // Unlabeled case
        } else {
            ArrayList<RPSTNode<ControlFlow, Node>> orNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            return textToIntermediateConverter.convertORSimple(node, null, false, orNodes.size());
        }
    }

    private ConverterRecord getANDConverterRecord(RPSTNode<ControlFlow, Node> node) {
        ArrayList<RPSTNode<ControlFlow, Node>> andNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
        return textToIntermediateConverter.convertANDGeneral(node, andNodes.size());
    }

    private void setProcessElementData(ConverterRecord convRecord, RPSTNode<ControlFlow, Node> node, ProcessElementType processElementType) {
        if (convRecord != null && convRecord.post != null) {
            convRecord.post.setProcessElementType(processElementType);
            convRecord.post.setProcessElementId(getProcessElementId(node.getExit().getId()));
        }
    }

    private void addBondPreStatements(ConverterRecord convRecord, int level, RPSTNode<ControlFlow, Node> node) {
        if (convRecord != null && convRecord.preStatements != null) {
            for (DSynTSentence preStatement : convRecord.preStatements) {
                if (isStart) {
                    isStart = false;

                    DSynTMainSentence dSynTSentence = new DSynTMainSentence(preStatement.getExecutableFragment());
                    dSynTSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), getProcessElementType(node));
                    addStartEventFragment(dSynTSentence, Lexemes.START_EVENT_WHEN_CONNECTIVE);

                    sentencePlan.add(dSynTSentence);
                } else {
                    if (passedFragments.size() > 0) {
                        if (isTagWithBullet) {
                            preStatement.getExecutableFragment().sen_hasBullet = true;
                            preStatement.getExecutableFragment().sen_level = level;
                            isTagWithBullet = false;

                            passedFragments.get(0).sen_hasBullet = true;
                            passedFragments.get(0).sen_level = level;
                        }
                        DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(preStatement.getExecutableFragment(), passedFragments, getProcessElementType(node));
                        passedFragments.clear();
                        sentencePlan.add(dsyntSentence);
                    } else {
                        if (isTagWithBullet) {
                            preStatement.getExecutableFragment().sen_hasBullet = true;
                            preStatement.getExecutableFragment().sen_level = level;
                            isTagWithBullet = false;
                        }
                        preStatement.getExecutableFragment().sen_level = level;
                        if (passedMods.size() > 0) {
                            preStatement.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));
                            preStatement.getExecutableFragment().sen_hasConnective = true;
                            passedMods.clear();
                        }

                        DSynTSentence dSynTSentence2 = new DSynTMainSentence(preStatement.getExecutableFragment());

                        dSynTSentence2.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), getProcessElementType(node));
                        sentencePlan.add(dSynTSentence2);
                    }
                }
            }
        }
    }

    private void convertBondToText(RPSTNode<ControlFlow, Node> node, int level) throws FileNotFoundException, JWNLException {
        if (PlanningHelper.isLoop(node, rpst) || PlanningHelper.isSkip(node, rpst)) {
            convertToText(node, level);
        }

        if (isSplit(node)) {
            ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            for (RPSTNode<ControlFlow, Node> path : paths) {
                isTagWithBullet = true;
                convertToText(path, level + 1);
            }
        }
    }

    private boolean isSplit(RPSTNode<ControlFlow, Node> node) {
        return PlanningHelper.isXORSplit(node, rpst) || PlanningHelper.isORSplit(node, rpst) || PlanningHelper.isANDSplit(node, rpst) || PlanningHelper.isEventSplit(node, rpst);
    }

    private void addBondPostStatement(ConverterRecord convRecord, int level, RPSTNode<ControlFlow, Node> node) {
        if (convRecord != null && convRecord.postStatements != null) {
            for (DSynTSentence postStatement : convRecord.postStatements) {
                postStatement.getExecutableFragment().sen_level = level;
                postStatement.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), convRecord.post.getProcessElementType());
                sentencePlan.add(postStatement);
            }
        }
    }

    private ProcessElementType getProcessElementType(RPSTNode<ControlFlow, Node> node) {
        ProcessElementType processElementType = null;

        if (PlanningHelper.isLoop(node, rpst)) {
            processElementType = ProcessElementType.XORSPLIT;
        } else if (PlanningHelper.isSkip(node, rpst)) {
            processElementType = ProcessElementType.XORSPLIT;
        } else if (PlanningHelper.isXORSplit(node, rpst)) {
            processElementType = ProcessElementType.XORSPLIT;
        } else if (PlanningHelper.isEventSplit(node, rpst)) {
            processElementType = ProcessElementType.GATEWAYBASEDEVENTSPLIT;
        } else if (PlanningHelper.isORSplit(node, rpst)) {
            processElementType = ProcessElementType.ORSPLIT;
        } else if (PlanningHelper.isANDSplit(node, rpst)) {
            processElementType = ProcessElementType.ANDSPLIT;
        }

        if (processElementType != null) {
            return processElementType;
        }

        return ProcessElementType.UNKNOWN;
    }

    // *********************************************************************************************
    // HANDLE RIGID
    // *********************************************************************************************

    private void handleRigid(RPSTNode<ControlFlow, Node> node, int level) {
        ArrayList<Integer> validIDs = new ArrayList<Integer>();
        validIDs.addAll(process.getActivites().keySet());
        validIDs.addAll(process.getEvents().keySet());

        // Transforming RPST subtree to Petri Net
        ArrayList<ArrayList<String>> runSequences = PlanningHelper.getRunSequencesFromRPSTFragment(node, process);
        orderRunSequenceBySize(runSequences, validIDs);

        addRigid(node);
        addRigidMain();

        // processToText.Main run
        ArrayList<String> mainRun = runSequences.get(0);
        boolean first = true;
        for (String id : mainRun) {
            if (validIDs.contains(Integer.valueOf(id))) {
                convertRigidElement(Integer.valueOf(id), level, first);
                first = false;
            }
        }

        addRigidDev(node);

        // Save ID of first rigid element
        String rigidStartID = node.getEntry().getId();

        // Deviation runs
        for (int i = 1; i < runSequences.size(); i++) {

            // Get run
            ArrayList<String> run = runSequences.get(i);
            String currentActivity = "";
            String previousActivity = "";
            String subsequentActivity = "";

            // Determine number of activities in run (if only one the sequence would be unclear to the reader)
            int activityCount = 0;
            for (String id : run) {
                if (validIDs.contains(Integer.valueOf(id))) {
                    currentActivity = id;
                    activityCount++;
                }
            }

            // If activity count = 1, add statement or activity to clarify run
            if (activityCount == 1) {
                // Run is alternative start
                if (run.get(0) == rigidStartID) {
                    convertRigidStartActivity(Integer.valueOf(currentActivity), level);
                    // Run just contains single activity (middle of rigid)
                } else {
                    int startId = mainRun.indexOf(run.get(0));
                    for (int j = startId; j >= 0; j--) {
                        if (j == 0) {
                            System.err.println("No Connection to main run.");
                        }
                        if (validIDs.contains(Integer.valueOf(mainRun.get(j)))) {
                            previousActivity = mainRun.get(j);
                            break;
                        }
                    }
                    convertIsolatedRigidActivity(Integer.valueOf(currentActivity), Integer.valueOf(previousActivity), level);
                }
            } else if (activityCount == 0) {

                boolean foundPrev = false;
                boolean foundSub = false;

                // Try to find previous and subsequent activity in the runs
                for (ArrayList<String> r : runSequences) {

                    // considered run contains predecessor
                    if (r.contains(run.get(0)) && !foundPrev) {
                        int startId = r.indexOf(run.get(0));
                        for (int j = startId; j >= 0; j--) {
                            if (validIDs.contains(Integer.valueOf(r.get(j)))) {
                                previousActivity = r.get(j);
                                foundPrev = true;
                                break;
                            }
                        }
                    }

                    // considered run contains predecessor
                    if (r.contains(run.get(run.size() - 1)) && !foundSub) {
                        int endId = r.indexOf(run.get(run.size() - 1));
                        for (int j = endId; j < r.size(); j++) {
                            if (validIDs.contains(Integer.valueOf(r.get(j)))) {
                                subsequentActivity = r.get(j);
                                foundSub = true;
                                break;
                            }
                        }
                    }
                }
                if (previousActivity.equals("")) {
                    convertRigidStartActivity(Integer.valueOf(subsequentActivity), level);
                } else if (subsequentActivity.equals("")) {
                    convertRigidEndActivity(Integer.valueOf(previousActivity), level);
                } else {
                    convertIsolatedRigidActivity(Integer.valueOf(subsequentActivity), Integer.valueOf(previousActivity), level);
                }


                // Multiple activities: run is clear
            } else {
                first = true;
                for (String id : run) {
                    if (validIDs.contains(Integer.valueOf(id))) {
                        if (first) {
                            convertRigidStartActivity(Integer.valueOf(id), level);
                            first = false;
                        } else {
                            convertRigidElement(Integer.valueOf(id), level, first);
                        }
                    }
                }
            }
        }
    }

    private void orderRunSequenceBySize(ArrayList<ArrayList<String>> runSequences, ArrayList<Integer> activitiesAndEventsIDs) {
        List<Integer> sizeRunSequence = new ArrayList<>();

        for (ArrayList<String> runSequence : runSequences) {
            int amountActivitiesAndEvents = 0;
            for (String elementId : runSequence) {
                if (activitiesAndEventsIDs.contains(Integer.valueOf(elementId))) {
                    amountActivitiesAndEvents++;
                }
            }
            sizeRunSequence.add(amountActivitiesAndEvents);
        }

        // Bubble sort
        for (int i = 0; i < sizeRunSequence.size(); i++) {
            for (int j = 0; j < sizeRunSequence.size() - i - 1; j++) {
                if ((sizeRunSequence.get(j)) < (sizeRunSequence.get(j + 1))) {
                    Collections.swap(sizeRunSequence, j, j + 1);
                    Collections.swap(runSequences, j, j + 1);
                }
            }
        }
    }

    private void addRigid(RPSTNode<ControlFlow, Node> node) {

        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        // loader.loadTemplate(TemplateLoaderType.RIGID);
        // eFrag = new ExecutableFragment(loader.getAction(), loader.getAddition(), loader.getObject(), "");

        eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.RIGID);
        //eFrag.bo_hasIndefArticle = true;
        eFrag.bo_hasArticle = false;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), ProcessElementType.XORSPLIT);
        sentencePlan.add(dSynTSentence);
    }

    private void addRigidMain() {

        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.RIGID_MAIN);
        eFrag.sen_hasConnective = true;
        eFrag.bo_hasArticle = false;
        eFrag.add_hasArticle = false;
        eFrag.bo_isSubject = true;
        eFrag.sen_hasColon = true;
        dSynTSentence = new DSynTMainSentence(eFrag);
        sentencePlan.add(dSynTSentence);
    }

    private void addRigidDev(RPSTNode<ControlFlow, Node> node) {

        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.RIGID_DEV);
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");
        eFrag.addMod("However,", modRecord);
        eFrag.bo_hasArticle = true;
        eFrag.bo_isSubject = true;
        eFrag.sen_hasConnective = true;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), ProcessElementType.XORSPLIT);
        sentencePlan.add(dSynTSentence);
    }

    private void convertRigidElement(int id, int level, boolean first) {
        Activity activity = process.getActivity(id);
        if (activity != null) {
            Annotation anno1 = activity.getAnnotations().get(0);

            ExecutableFragment eFrag = null;
            eFrag = new ExecutableFragment(anno1.getActions().get(0), anno1.getBusinessObjects().get(0), "", anno1.getAddition());
            eFrag.addAssociation(activity.getId());
            String role = getRole(activity, eFrag);
            eFrag.setRole(role);

            if (first) {
                eFrag.sen_hasBullet = true;
            }
            eFrag.sen_level = level + 1;

            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(getProcessElementId(id), ProcessElementType.ACTIVITY);
            sentencePlan.add(dsyntSentence);
        } else {
        }
    }

    // TODO: O MÉTODO ABAIXO INSERE TEXTO DIRETAMENTE
    private void convertRigidStartActivity(int id, int level) {
        Activity activity = process.getActivity(id);
        if (activity != null) {
            Annotation anno1 = activity.getAnnotations().get(0);
            ExecutableFragment eFrag = null;

            //TemplateLoader loader = new TemplateLoader();
            //loader.loadTemplate(TemplateLoader.RIGIDSTARTACTIVITY);
            eFrag = new ExecutableFragment("may", "also begin with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
            eFrag.addAssociation(activity.getId());
            String role = getRole(activity, eFrag);
            eFrag.setRole(role);
            eFrag.bo_hasArticle = false;
            eFrag.sen_hasBullet = true;
            eFrag.sen_level = level + 1;

            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(getProcessElementId(activity.getId()), ProcessElementType.ACTIVITY);
            sentencePlan.add(dsyntSentence);
        }
    }

    // TODO: O MÉTODO ABAIXO INSERE TEXTO DIRETAMENTE
    private void convertRigidEndActivity(int id, int level) {
        Activity activity = process.getActivity(id);
        if (activity != null) {
            Annotation anno1 = activity.getAnnotations().get(0);
            ExecutableFragment eFrag = null;

            //TemplateLoader loader = new TemplateLoader();
            //loader.loadTemplate(TemplateLoader.RIGIDENDACTIVITY);
            eFrag = new ExecutableFragment("may", "also end with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
            eFrag.addAssociation(activity.getId());
            String role = getRole(activity, eFrag);
            eFrag.setRole(role);
            eFrag.bo_hasArticle = false;
            eFrag.sen_hasBullet = true;
            eFrag.sen_level = level + 1;

            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(getProcessElementId(activity.getId()), ProcessElementType.ACTIVITY);
            sentencePlan.add(dsyntSentence);
        }
    }

    // TODO: O MÉTODO ABAIXO INSERE TEXTO DIRETAMENTE
    private void convertIsolatedRigidActivity(int id, int prevId, int level) {
        Activity currActivity = process.getActivity(id);
        Activity prevActivity = process.getActivity(prevId);
        Annotation currAnno = currActivity.getAnnotations().get(0);
        Annotation prevAnno = prevActivity.getAnnotations().get(0);

        ExecutableFragment eFrag = null;
        String modLemma = "after " + prevAnno.getActions().get(0) + "ing " + prevAnno.getBusinessObjects().get(0);
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");

        //TemplateLoader loader = new TemplateLoader();
        //loader.loadTemplate(TemplateLoader.ISOLATEDRIGIDACTIVITY);
        eFrag = new ExecutableFragment("may", "also " + currAnno.getActions().get(0) + " the " + currAnno.getBusinessObjects().get(0), "", "");
        String role = getRole(currActivity, eFrag);
        eFrag.setRole(role);
        eFrag.bo_hasArticle = false;
        eFrag.sen_hasBullet = true;
        eFrag.sen_level = level + 1;
        eFrag.addMod(modLemma, modRecord);

        DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
        dsyntSentence.addProcessElementDocument(getProcessElementId(currActivity.getId()), ProcessElementType.ACTIVITY);
        sentencePlan.add(dsyntSentence);
    }

    // *********************************************************************************************
    // HANDLE ACTIVITIES
    // *********************************************************************************************

    private void handleActivities(RPSTNode<ControlFlow, Node> node, int level, int depth) throws JWNLException, FileNotFoundException {
        Activity activity = process.getActivity(Integer.parseInt(node.getEntry().getId()));
        Annotation anno = activity.getAnnotations().get(0);
        boolean isPlanned = false;

        // Start of the process
        if (isStart && !isAlternative) {
            handleActivityStart(anno, activity);
            isPlanned = true;
        }

        // Standard case
        ExecutableFragment eFrag = handleStandardCase(activity, anno, level);

        if (isImperative && imperativeRole.equals(eFrag.getRole())) {
            correctArticleSettings(eFrag);
            eFrag.verb_isImperative = true;
            eFrag.setRole("");
        }

        if (activity.getSubProcessID() > 0) {
            eFrag.sen_level = level + 1;
        }

        if (isTagWithBullet) {
            eFrag.sen_hasBullet = true;
            isTagWithBullet = false;
        }

        handleIsNotPlanned(eFrag, node, isPlanned);

        // If activity has attached Events
        if (activity.hasAttachedEvents()) {
            handleActivityAttachedEvents(activity, level);
        }

        if (depth > 0) {
            convertToText(node, level);
        }
    }

    private void handleActivityStart(Annotation anno, Activity activity) {
        isStart = false;

        String bo = anno.getBusinessObjects().get(0);
        ExecutableFragment eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition());
        eFrag.addAssociation(activity.getId());

        String role = getRole(activity, eFrag);
        eFrag.setRole(role);

        if (anno.getActions().size() == 2) {
            addExecutableFragment(eFrag, activity, anno);
        }

        if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0, bo.length() - 1))) {
            eFrag.bo_hasArticle = true;
        } else {
            eFrag.bo_hasIndefArticle = true;
        }

        // If isImperative mode
        if (isImperative && imperativeRole.equals(role)) {
            eFrag.verb_isImperative = true;
            eFrag.role_isImperative = true;
        }

        correctArticleSettings(eFrag);

        // Original DSYNT
        DSynTMainSentence dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument(getProcessElementId(activity.getId()), ProcessElementType.ACTIVITY);
        addStartEventFragment(dSynTSentence, Lexemes.START_EVENT_CONNECTIVE);

        // activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
        // XmlFormat.printDocument(startEventDsynt.getDSynT());
        sentencePlan.add(dSynTSentence);
    }

    private void addStartEventFragment(DSynTMainSentence dSynTSentence, String lexeme) {
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("starting_point", "+");
        dSynTSentence.getExecutableFragment().addMod(lexeme, modRecord);
        dSynTSentence.addProcessElementDocument(currentStartEventId, ProcessElementType.STARTEVENT, "", lexeme);
        dSynTSentence.createDSynTRepresentation();
    }

    private void addExecutableFragment(ExecutableFragment eFrag, Activity activity, Annotation anno) {
        ExecutableFragment eFrag2;
        if (anno.getBusinessObjects().size() == 2) {
            eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
        } else {
            eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
        }
        eFrag2.addAssociation(activity.getId());
        correctArticleSettings(eFrag2);
        eFrag.addSentence(eFrag2);
    }

    private ExecutableFragment handleStandardCase(Activity activity, Annotation anno, int level) {
        ExecutableFragment eFrag = new ExecutableFragment(anno.getActions().get(0), anno.getBusinessObjects().get(0), "", anno.getAddition());
        eFrag.addAssociation(activity.getId());
        String role = getRole(activity, eFrag);
        eFrag.setRole(role);
        if (anno.getActions().size() == 2) {
            ExecutableFragment eFrag2 = null;
            if (anno.getBusinessObjects().size() == 2) {
                eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
                if (eFrag.verb_IsPassive) {
                    if (anno.getBusinessObjects().get(0).equals("")) {
                        eFrag2.verb_IsPassive = true;
                        eFrag.setBo(eFrag2.getBo());
                        eFrag2.setBo("");
                        eFrag.bo_hasArticle = true;
                    } else {
                        eFrag2.verb_IsPassive = true;
                        eFrag2.bo_isSubject = true;
                    }

                }
            } else {
                eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
                if (eFrag.verb_IsPassive) {
                    eFrag2.verb_IsPassive = true;
                }
            }

            correctArticleSettings(eFrag2);
            eFrag2.addAssociation(activity.getId());
            eFrag.addSentence(eFrag2);
        }
        eFrag.sen_level = level;
        return eFrag;
    }

    private void handleIsNotPlanned(ExecutableFragment eFrag, RPSTNode<ControlFlow, Node> node, boolean isPlanned) {
        if (!isPlanned) {
            // In case of passed modifications (NOT AND - Split)
            if (passedMods.size() > 0) {
                correctArticleSettings(eFrag);
                eFrag.addMod(passedMods.get(0).getLemma(), passedMods.get(0));
                eFrag.sen_hasConnective = true;
                passedMods.clear();
            }

            // In case of passed modifications (e.g. AND - Split)
            if (passedMod != null) {
                correctArticleSettings(eFrag);
                eFrag.addMod(passedMod.getLemma(), passedMod);
                eFrag.sen_hasConnective = true;
                passedMod = null;
            }

            // In case of passed fragments (General handling)
            if (passedFragments.size() > 0) {
                correctArticleSettings(eFrag);
                DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(eFrag, passedFragments, ProcessElementType.ACTIVITY);
                sentencePlan.add(dsyntSentence);
                // activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
                passedFragments.clear();
                isPlanned = true;
            }
        }

        if (!isPlanned) {
            correctArticleSettings(eFrag);
            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(getProcessElementId(node.getEntry().getId()), ProcessElementType.ACTIVITY);
            sentencePlan.add(dsyntSentence);
            // activitiySentenceMap.add(new Pair<>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
        }
    }

    private void handleActivityAttachedEvents(Activity activity, int level) throws FileNotFoundException, JWNLException {
        ArrayList<Integer> attachedEvents = activity.getAttachedEvents();
        HashMap<Integer, ProcessModel> alternativePaths = process.getAlternativePaths();
        for (Integer attEvent : attachedEvents) {
            if (alternativePaths.keySet().contains(attEvent)) {
                // Transform alternative
                ProcessModel alternative = alternativePaths.get(attEvent);
                alternative.annotateModel(0, lDeriver, lHelper);

                // Consider complexity of the process
                if (alternative.getElemAmount() <= 3) {
                    alternative.getEvents().get(attEvent).setLeadsToEnd(true);
                }

                FormatConverter rpstConverter = new FormatConverter();
                Process p = rpstConverter.transformToRPSTFormat(alternative);
                RPST<ControlFlow, Node> rpst = new RPST<ControlFlow, Node>(p);
                TextPlanner converter = new TextPlanner(rpst, alternative, lDeriver, lHelper, imperativeRole, isImperative, true, bpmnIdMap, bpmnWrapper.getDefinitions());
                PlanningHelper.printTree(rpst.getRoot(), 0, rpst);
                converter.convertToText(rpst.getRoot(), level + 1);
                ArrayList<DSynTSentence> subSentencePlan = converter.getSentencePlan();
                for (int i = 0; i < subSentencePlan.size(); i++) {
                    DSynTSentence sen = subSentencePlan.get(i);
                    if (i == 0) {
                        sen.getExecutableFragment().sen_level = level;
                    }
                    if (i == 1) {
                        sen.getExecutableFragment().sen_hasBullet = true;
                    }
                    sen.addProcessElementDocument(getProcessElementId(activity.getId()), ProcessElementType.ACTIVITY);
                    sentencePlan.add(sen);
                }

                // Print sentence for subsequent normal execution
                DSynTSentence dSynTSentence = textToIntermediateConverter.getAttachedEventPostStatement(alternative.getEvents().get(attEvent));
                dSynTSentence.addProcessElementDocument(getProcessElementId(activity.getId()), ProcessElementType.ACTIVITY);
                sentencePlan.add(dSynTSentence);
            }
        }
    }

    // Checks and corrects the article settings.
    private void correctArticleSettings(AbstractFragment frag) {
        String bo = frag.getBo();
        if (bo.endsWith("s") && !bo.endsWith("ss") && frag.bo_hasArticle && lHelper.isNoun(bo.substring(0, bo.length() - 1))) {
            bo = bo.substring(0, bo.length() - 1);
            frag.setBo(bo);
            frag.bo_isPlural = true;
        }
        if (bo.contains("&")) {
            frag.bo_isPlural = true;
        }
        if (frag.bo_hasArticle) {
            String[] boSplit = bo.split(" ");
            if (boSplit.length > 1) {
                if (Arrays.asList(quantifiers).contains(boSplit[0].toLowerCase())) {
                    frag.bo_hasArticle = false;
                }
            }
        }
        if (bo.equals("") && frag.bo_hasArticle) {
            frag.bo_hasArticle = false;
        }
        if (bo.startsWith("their") || bo.startsWith("a ") || bo.startsWith("for")) {
            frag.bo_hasArticle = false;
        }
        String[] splitAdd = frag.getAddition().split(" ");
        frag.add_hasArticle = splitAdd.length <= 3 || !lHelper.isVerb(splitAdd[1]) || splitAdd[0].equals("on") != false;

    }

    // *********************************************************************************************
    // HANDLE EVENT
    // *********************************************************************************************

    private void handleEvent(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) {
        Event event = process.getEvents().get((Integer.valueOf(node.getEntry().getId())));
        int currentPosition = orderedTopNodes.indexOf(node);

        // Start Event
        if (currentPosition == 0) {
            handleStartEvent(event, currentPosition, orderedTopNodes);
            // Intermediate Events
        } else {
            handleIntermediateEvent(event, level);
        }
    }

    private void handleStartEvent(Event event, int currentPosition, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes) {
        // Start event should be printed
        if (isStart && !isAlternative) {

            // Event is followed by gateway --> full sentence
            /*
            if (isFollowedByGateway(event, currentPosition, orderedTopNodes)) {
                isStart = false;

                ExecutableFragment eFrag = FragmentGenerator.generateExecutableFragment(TemplateLoaderType.STARTDECISION);
                eFrag.add_hasArticle = false;
                eFrag.bo_isSubject = true;

                DSynTSentence dSynTSentence = new DSynTMainSentence(eFrag);
                dSynTSentence.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.STARTEVENT);
                sentencePlan.add(dSynTSentence);
            }
            */

            if (event.getType() != EventType.START_EVENT) {
                isStart = false;

                ConverterRecord convRecord = textToIntermediateConverter.convertEvent(event);
                if (convRecord != null && convRecord.hasPreStatements()) {
                    DSynTSentence dSynTSentence = convRecord.preStatements.get(0);
                    dSynTSentence.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.STARTEVENT);
                    sentencePlan.add(dSynTSentence);
                }
            }

            currentStartEventId = getProcessElementId(event.getId());
        }
    }

    private boolean isFollowedByGateway(Event event, int currentPosition, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes) {
        return event.getType() == EventType.START_EVENT && currentPosition < orderedTopNodes.size() - 1 && PlanningHelper.isBond(orderedTopNodes.get(currentPosition + 1));
    }

    private void handleIntermediateEvent(Event event, int level) {
        ConverterRecord convRecord = textToIntermediateConverter.convertEvent(event);

        // Add fragments if applicable
        if (convRecord != null && convRecord.pre != null) {
            passedFragments.add(convRecord.pre);
        }

        // Adjust level and add to sentence plan (first sentence not indented)
        if (convRecord != null && convRecord.hasPreStatements() == true) {
            for (int i = 0; i < convRecord.preStatements.size(); i++) {

                DSynTSentence sen = convRecord.preStatements.get(i);

                // If only one sentence (e.g. "Intermediate" End Event)
                if (convRecord.preStatements.size() == 1) {
                    sen.getExecutableFragment().sen_level = level;
                }

                if (isTagWithBullet == true) {
                    sen.getExecutableFragment().sen_hasBullet = true;
                    sen.getExecutableFragment().sen_level = level;
                    isTagWithBullet = false;
                }

                if (i > 0) {
                    sen.getExecutableFragment().sen_level = level;
                }

                if (event.getSubProcessID() > 0) {
                    sen.getExecutableFragment().sen_level = level + 1;
                }

                if (passedMods.size() > 0) {
                    String mod = passedMods.get(0).getLemma();
                    if (mod.equals("alternatively,") && sen.getExecutableFragment().sen_hasBullet) {
                        passedMods.clear();
                    } else {
                        sen.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));
                        sen.getExecutableFragment().sen_hasConnective = true;
                        passedMods.clear();
                    }
                }

                if (passedFragments.size() > 0) {
                    DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(sen.getExecutableFragment(), passedFragments, ProcessElementType.INTERMEDIATEEVENT);
                    dsyntSentence.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.INTERMEDIATEEVENT);
                    sentencePlan.add(dsyntSentence);
                    passedFragments.clear();
                } else {
                    if (sen.getClass().toString().endsWith("DSynTConditionSentence")) {
                        DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), ((DSynTConditionSentence) sen).getConditionFragment());
                        dsyntSentence.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.INTERMEDIATEEVENT);
                        sentencePlan.add(dsyntSentence);
                    } else {
                        DSynTMainSentence dsyntSentence = new DSynTMainSentence(sen.getExecutableFragment());
                        dsyntSentence.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.INTERMEDIATEEVENT);
                        sentencePlan.add(dsyntSentence);
                    }
                }

            }
        }
    }

    private void handleEndEvent(RPSTNode<ControlFlow, Node> node, int level) {
        Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
        DSynTSentence sen = textToIntermediateConverter.convertEvent(event).preStatements.get(0);
        sen.getExecutableFragment().sen_level = level;
        sen.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.ENDEVENT);
        sentencePlan.add(sen);
    }

    private void handleEndEvent(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) {
        isEnd = false;
        Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
        if (event.getType() == EventType.END_EVENT && orderedTopNodes.indexOf(node) == orderedTopNodes.size() - 1) {
            // Adjust level and add to sentence plan
            DSynTSentence sen = textToIntermediateConverter.convertEvent(event).preStatements.get(0);
            sen.getExecutableFragment().sen_level = level;
            if (event.getSubProcessID() > 0) {
                sen.getExecutableFragment().sen_level = level + 1;
            }
            sen.addProcessElementDocument(getProcessElementId(event.getId()), ProcessElementType.ENDEVENT);
            sentencePlan.add(sen);
        }
    }

    // *********************************************************************************************
    // OTHERS
    // *********************************************************************************************

    private String getRole(Activity a, AbstractFragment frag) {
        if (a.getLane() == null) {
            frag.verb_IsPassive = true;
            frag.bo_isSubject = true;
            if (frag.getBo().equals("")) {
                frag.setBo("it");
                frag.bo_hasArticle = false;
            }
            return "";
        }
        String role = a.getLane().getName();
        if (role.equals("")) {
            role = a.getPool().getName();
        }
        if (role.equals("")) {
            frag.verb_IsPassive = true;
            frag.bo_isSubject = true;
            if (frag.getBo().equals("")) {
                frag.setBo("it");
                frag.bo_hasArticle = false;
            }
        }
        return role;
    }

    private String getProcessElementId(String id) {
        return getProcessElementId(Integer.valueOf(id));
    }

    private String getProcessElementId(int id) {
        return bpmnIdMap.getOrDefault(id, "none");
    }

    public ArrayList<DSynTSentence> getSentencePlan() {
        return sentencePlan;
    }

    // *********************************************************************************************
    // DSynt
    // *********************************************************************************************

    private DSynTConditionSentence getDSyntConditionSentence(ExecutableFragment eFrag, List<ConditionFragment> passedFragments, ProcessElementType processElementTypeMain) {
        DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
        if (passedFragments.size() > 1) {
            for (int i = 1; i < passedFragments.size(); i++) {
                dsyntSentence.addCondition(passedFragments.get(i), true);
                dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
            }
        }

        generateProcessElementList(eFrag, dsyntSentence, processElementTypeMain);
        return dsyntSentence;
    }

    private void generateProcessElementList(ExecutableFragment eFrag, DSynTConditionSentence dSynTSentence, ProcessElementType processElementMain) {
        ProcessElementType processElementType = processElementMain;
        Document document = dSynTSentence.getDocuments().get(0);

        if (!eFrag.getAssociatedActivities().isEmpty()) {
            dSynTSentence.addProcessElementDocument(getProcessElementId(eFrag.getAssociatedActivities().get(0)), processElementType, document);
        }

        for (int i = 0; i < passedFragments.size(); i++) {
            processElementType = passedFragments.get(i).getProcessElementType();
            String processElementId = passedFragments.get(i).getProcessElementId();
            document = dSynTSentence.getDocuments().get(i + 1);
            dSynTSentence.addProcessElementDocument(processElementId, processElementType, "", document);
        }
    }

}