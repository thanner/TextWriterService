package br.edu.ufrgs.inf.bpm.changes.textPlanning;

import br.edu.ufrgs.inf.bpm.ProcessElementType;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoader;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import net.didion.jwnl.JWNLException;
import org.w3c.dom.Document;
import processToText.contentDetermination.extraction.GatewayExtractor;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.Pair;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TextPlanner {

    static String[] quantifiers = {"a", "the", "all", "any", "more", "most", "none", "some", "such", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};
    private RPST<ControlFlow, Node> rpst;
    private ProcessModel process;
    private TextToIntermediateConverter textToIMConverter;
    private ArrayList<ConditionFragment> passedFragments;
    private ModifierRecord passedMod = null; // used for AND-Splits
    private ArrayList<ModifierRecord> passedMods; // used for Skips
    private boolean tagWithBullet = false;
    private boolean start = true;
    private boolean end = false;
    private boolean isAlternative = false;
    private int isolatedXORCount = 0;
    private ArrayList<DSynTSentence> sentencePlan;
    private ArrayList<Pair<Integer, DSynTSentence>> activitiySentenceMap;
    private EnglishLabelHelper lHelper;
    private EnglishLabelDeriver lDeriver;
    private boolean imperative;
    private String imperativeRole;

    public TextPlanner(RPST<ControlFlow, Node> rpst, ProcessModel process, EnglishLabelDeriver lDeriver, EnglishLabelHelper lHelper, String imperativeRole, boolean imperative, boolean isAlternative) throws FileNotFoundException, JWNLException {
        this.rpst = rpst;
        this.process = process;
        this.lHelper = lHelper;
        this.lDeriver = lDeriver;
        textToIMConverter = new TextToIntermediateConverter(rpst, process, lHelper, imperativeRole, imperative);
        passedFragments = new ArrayList<>();
        sentencePlan = new ArrayList<>();
        activitiySentenceMap = new ArrayList<>();
        passedMods = new ArrayList<>();
        this.imperative = imperative;
        this.imperativeRole = imperativeRole;
        this.isAlternative = isAlternative;
    }

    /**
     * Text Planning processToText.Main
     *
     * @throws FileNotFoundException
     */
    public void convertToText(RPSTNode<ControlFlow, Node> root, int level) throws JWNLException, FileNotFoundException {
        // Order nodes of current level with respect to control flow
        ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper.sortTreeLevel(root, root.getEntry(), rpst);
        // For each node of current level
        for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {

            // If we face an end event
            end = (PlanningHelper.isEvent(node.getExit()) && orderedTopNodes.indexOf(node) == orderedTopNodes.size() - 1);
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
            } else {
                if (depth > 0) {
                    convertToText(node, level);
                }
            }

            if (end) {
                handleEndEvent(node, level);
            }

        }
    }

    /**
     * HANDLE BOND
     */

    private void handleBond(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) throws FileNotFoundException, JWNLException {
        ConverterRecord convRecord = getConverterRecord(node, orderedTopNodes);

        // Add pre statements
        addBondPreStatements(convRecord, level, node);

        // Pass precondition
        if (convRecord != null && convRecord.pre != null) {
            if (passedFragments.size() > 0) {
                if (passedFragments.get(0).getFragmentType() == AbstractFragment.TYPE_JOIN) {
                    ExecutableFragment eFrag = new ExecutableFragment("continue", "process", "", "");
                    eFrag.bo_isSubject = true;
                    DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
                    dsyntSentence.addProcessElementDocument("10");
                    sentencePlan.add(dsyntSentence);
                    passedFragments.clear();
                }
            }
            passedFragments.add(convRecord.pre);
        }

        // Convert to Text
        convertBondToText(node, level);

        // Add post statement to sentence plan
        if (convRecord != null && convRecord.postStatements != null) {
            for (DSynTSentence postStatement : convRecord.postStatements) {
                postStatement.getExecutableFragment().sen_level = level;
                postStatement.addProcessElementDocument("11");
                sentencePlan.add(postStatement);
            }
        }

        // Pass post fragment
        if (convRecord != null && convRecord.post != null) {
            passedFragments.add(convRecord.post);
        }
    }

    private ConverterRecord getConverterRecord(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes) {
        ConverterRecord convRecord = null;

        if (PlanningHelper.isLoop(node, rpst)) {
            convRecord = getLoopConverterRecord(node);
            convRecord.post.setProcessElement(ProcessElementType.LOOP.getValue());
        }
        if (PlanningHelper.isSkip(node, rpst)) {
            convRecord = getSkipConverterRecord(orderedTopNodes, node);
            convRecord.post.setProcessElement(ProcessElementType.SKIP.getValue());
        }
        if (PlanningHelper.isXORSplit(node, rpst)) {
            convRecord = getXORConverterRecord(node);
            convRecord.post.setProcessElement(ProcessElementType.XORJOIN.getValue());
        }
        if (PlanningHelper.isEventSplit(node, rpst)) {
            convRecord = getXORConverterRecord(node);
            convRecord.post.setProcessElement(ProcessElementType.INTERMEDIATEEVENT.getValue());
        }
        if (PlanningHelper.isORSplit(node, rpst)) {
            convRecord = getORConverterRecord(node);
            convRecord.post.setProcessElement(ProcessElementType.ORJOIN.getValue());
        }
        if (PlanningHelper.isANDSplit(node, rpst)) {
            convRecord = getANDConverterRecord(node);
            convRecord.post.setProcessElement(ProcessElementType.ANDJOIN.getValue());
        }

        return convRecord;
    }

    private ConverterRecord getLoopConverterRecord(RPSTNode<ControlFlow, Node> node) {
        RPSTNode<ControlFlow, Node> firstNodeInLoop = PlanningHelper.getNextNode(node, rpst);
        return textToIMConverter.convertLoop(node, firstNodeInLoop);
    }

    private ConverterRecord getSkipConverterRecord(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
        // Yes-No Case
        if (propRec.isGatewayLabeled() == true && propRec.hasYNArcs() == true) {
            // Yes-No Case which is directly leading to the end of the process
            if (isToEndSkip(orderedTopNodes, node) == true) {
                return textToIMConverter.convertSkipToEnd(node);
                // General Yes/No-Case
            } else {
                return textToIMConverter.convertSkipGeneral(node);
            }
            // General unlabeled Skip
        } else {
            return textToIMConverter.convertSkipGeneralUnlabeled(node);
        }
    }

    // Evaluate whether skip leads to an end
    private boolean isToEndSkip(ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, RPSTNode<ControlFlow, Node> node) {
        int currentPosition = orderedTopNodes.indexOf(node);
        if (currentPosition < orderedTopNodes.size() - 1) {
            Node potEndNode = orderedTopNodes.get(currentPosition + 1).getExit();
            return PlanningHelper.isEndEvent(potEndNode, process) == true;
        }
        return false;
    }

    private ConverterRecord getXORConverterRecord(RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord propRec = new GatewayPropertyRecord(node, rpst, process);
        // Labeled Case with Yes/No - arcs and Max. Depth of 1
        if (propRec.isGatewayLabeled() == true && propRec.hasYNArcs() == true && propRec.getMaxPathDepth() == 1) {
            GatewayExtractor gwExtractor = new GatewayExtractor(node.getEntry(), lHelper);
            // Add sentence
            for (DSynTSentence s : textToIMConverter.convertXORSimple(node, gwExtractor)) {
                s.addProcessElementDocument("9");
                sentencePlan.add(s);
            }
            return null;
            // General case
        } else {
            return textToIMConverter.convertXORGeneral(node);
        }
    }

    private ConverterRecord getORConverterRecord(RPSTNode<ControlFlow, Node> node) {
        GatewayPropertyRecord orPropRec = new GatewayPropertyRecord(node, rpst, process);

        // Labeled Case
        if (orPropRec.isGatewayLabeled() == true) {
            return null;

            // Unlabeled case
        } else {
            return textToIMConverter.convertORSimple(node, null, false);
        }
    }

    private ConverterRecord getANDConverterRecord(RPSTNode<ControlFlow, Node> node) {

        ArrayList<RPSTNode<ControlFlow, Node>> andNodes = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);

        // Only General Case, no need for non-bulletin and-branches
        ConverterRecord rec = textToIMConverter.convertANDGeneral(node, andNodes.size(), null);
        return rec;
    }

    private void addBondPreStatements(ConverterRecord convRecord, int level, RPSTNode<ControlFlow, Node> node) {
        if (convRecord != null && convRecord.preStatements != null) {
            for (DSynTSentence preStatement : convRecord.preStatements) {
                if (passedFragments.size() > 0) {
                    if (tagWithBullet) {
                        preStatement.getExecutableFragment().sen_hasBullet = true;
                        preStatement.getExecutableFragment().sen_level = level;
                        passedFragments.get(0).sen_hasBullet = true;
                        passedFragments.get(0).sen_level = level;
                        tagWithBullet = false;
                    }
                    DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(preStatement.getExecutableFragment(), passedFragments, getProcessElement(node));
                    passedFragments.clear();
                    sentencePlan.add(dsyntSentence);
                } else {
                    if (tagWithBullet) {
                        preStatement.getExecutableFragment().sen_hasBullet = true;
                        preStatement.getExecutableFragment().sen_level = level;
                        tagWithBullet = false;
                    }
                    preStatement.getExecutableFragment().sen_level = level;
                    if (passedMods.size() > 0) {
                        preStatement.getExecutableFragment().addMod(passedMods.get(0).getLemma(), passedMods.get(0));
                        preStatement.getExecutableFragment().sen_hasConnective = true;
                        passedMods.clear();
                    }

                    DSynTSentence dSynTSentence2 = new DSynTMainSentence(preStatement.getExecutableFragment());
                    dSynTSentence2.addProcessElementDocument(getProcessElement(node));
                    sentencePlan.add(dSynTSentence2);
                }
            }
        }
    }

    private String getProcessElement(RPSTNode<ControlFlow, Node> node) {
        ProcessElementType processElementType = null;

        if (PlanningHelper.isLoop(node, rpst)) {
            processElementType = ProcessElementType.LOOP;
        } else if (PlanningHelper.isSkip(node, rpst)) {
            processElementType = ProcessElementType.SKIP;
        } else if (PlanningHelper.isXORSplit(node, rpst)) {
            processElementType = ProcessElementType.XORSPLIT;
        } else if (PlanningHelper.isEventSplit(node, rpst)) {
            processElementType = ProcessElementType.EVENTSPLIT;
        } else if (PlanningHelper.isORSplit(node, rpst)) {
            processElementType = ProcessElementType.ORSPLIT;
        } else if (PlanningHelper.isANDSplit(node, rpst)) {
            processElementType = ProcessElementType.ANDSPLIT;
        }

        if (processElementType != null) {
            return processElementType.getValue();
        }

        return "unknown";
    }
    
    private void convertBondToText(RPSTNode<ControlFlow, Node> node, int level) throws FileNotFoundException, JWNLException {
        if (PlanningHelper.isLoop(node, rpst) || PlanningHelper.isSkip(node, rpst)) {
            convertToText(node, level);
        }
        if (PlanningHelper.isXORSplit(node, rpst) || PlanningHelper.isORSplit(node, rpst) || PlanningHelper.isEventSplit(node, rpst)) {
            ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            for (RPSTNode<ControlFlow, Node> path : paths) {
                tagWithBullet = true;
                convertToText(path, level + 1);
            }
        }
        if (PlanningHelper.isANDSplit(node, rpst)) {

            ArrayList<RPSTNode<ControlFlow, Node>> paths = PlanningHelper.sortTreeLevel(node, node.getEntry(), rpst);
            for (RPSTNode<ControlFlow, Node> path : paths) {
                tagWithBullet = true;
                convertToText(path, level + 1);
            }
        }
    }

    /**
     * HANDLE RIGID
     */

    private void handleRigid(RPSTNode<ControlFlow, Node> node, int level) {
        ArrayList<Integer> validIDs = new ArrayList<Integer>();
        validIDs.addAll(process.getActivites().keySet());
        validIDs.addAll(process.getEvents().keySet());

        // Transforming RPST subtree to Petri Net
        ArrayList<ArrayList<String>> runSequences = PlanningHelper.getRunSequencesFromRPSTFragment(node, process);

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

    private void addRigid(RPSTNode<ControlFlow, Node> node) {
        TemplateLoader loader = new TemplateLoader();
        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        loader.loadTemplate(TemplateLoader.RIGID);
        eFrag = new ExecutableFragment(loader.getAction(), loader.getAddition(), loader.getObject(), "");
        eFrag.bo_hasIndefArticle = true;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument("rigid 1");
        sentencePlan.add(dSynTSentence);
    }

    private void addRigidMain() {
        TemplateLoader loader = new TemplateLoader();
        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        loader.loadTemplate(TemplateLoader.RIGID_MAIN);
        eFrag = new ExecutableFragment(loader.getAction(), loader.getObject(), "", loader.getAddition());
        eFrag.sen_hasConnective = true;
        eFrag.bo_hasArticle = false;
        eFrag.add_hasArticle = false;
        eFrag.bo_isSubject = true;
        eFrag.sen_hasColon = true;
        dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument("rigid 2");
        sentencePlan.add(dSynTSentence);
    }

    private void addRigidDev(RPSTNode<ControlFlow, Node> node) {
        TemplateLoader loader = new TemplateLoader();
        DSynTSentence dSynTSentence;
        ExecutableFragment eFrag;

        loader.loadTemplate(TemplateLoader.RIGID_DEV);
        eFrag = new ExecutableFragment(loader.getAction(), loader.getObject(), "", loader.getAddition());
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");
        eFrag.addMod("However,", modRecord);
        eFrag.bo_hasArticle = true;
        eFrag.bo_isSubject = true;
        eFrag.sen_hasConnective = true;
        eFrag.addAssociation(Integer.valueOf(node.getEntry().getId()));
        dSynTSentence = new DSynTMainSentence(eFrag);
        dSynTSentence.addProcessElementDocument("rigid 3");
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
            sentencePlan.add(dsyntSentence);
        } else {
        }
    }

    private void convertRigidStartActivity(int id, int level) {
        Activity activity = process.getActivity(id);
        if (activity != null) {
            Annotation anno1 = activity.getAnnotations().get(0);

            ExecutableFragment eFrag = null;
            eFrag = new ExecutableFragment("may", "also begin with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
            eFrag.addAssociation(activity.getId());
            String role = getRole(activity, eFrag);
            eFrag.setRole(role);
            eFrag.bo_hasArticle = false;
            eFrag.sen_hasBullet = true;
            eFrag.sen_level = level + 1;

            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            sentencePlan.add(dsyntSentence);
        } else {
        }
    }

    private void convertRigidEndActivity(int id, int level) {
        Activity activity = process.getActivity(id);
        if (activity != null) {
            Annotation anno1 = activity.getAnnotations().get(0);
            ExecutableFragment eFrag = null;
            eFrag = new ExecutableFragment("may", "also end with " + anno1.getActions().get(0) + "ing " + anno1.getBusinessObjects().get(0), "", anno1.getAddition());
            eFrag.addAssociation(activity.getId());
            String role = getRole(activity, eFrag);
            eFrag.setRole(role);
            eFrag.bo_hasArticle = false;
            eFrag.sen_hasBullet = true;
            eFrag.sen_level = level + 1;

            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            sentencePlan.add(dsyntSentence);
        }
    }

    private void convertIsolatedRigidActivity(int id, int prevId, int level) {

        Activity currActivity = process.getActivity(id);
        Activity prevActivity = process.getActivity(prevId);
        Annotation currAnno = currActivity.getAnnotations().get(0);
        Annotation prevAnno = prevActivity.getAnnotations().get(0);


        ExecutableFragment eFrag = null;
        String modLemma = "after " + prevAnno.getActions().get(0) + "ing " + prevAnno.getBusinessObjects().get(0);
        ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
        modRecord.addAttribute("adv-type", "sentential");

        eFrag = new ExecutableFragment("may", "also " + currAnno.getActions().get(0) + " the " + currAnno.getBusinessObjects().get(0), "", "");
        String role = getRole(currActivity, eFrag);
        eFrag.setRole(role);
        eFrag.bo_hasArticle = false;
        eFrag.sen_hasBullet = true;
        eFrag.sen_level = level + 1;
        eFrag.addMod(modLemma, modRecord);

        DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
        sentencePlan.add(dsyntSentence);
    }

    /**
     * HANDLE ACTIVITIES
     */

    private void handleActivities(RPSTNode<ControlFlow, Node> node, int level, int depth) throws JWNLException, FileNotFoundException {

        boolean planned = false;

        Activity activity = process.getActivity(Integer.parseInt(node.getEntry().getId()));

        Annotation anno = activity.getAnnotations().get(0);
        ExecutableFragment eFrag = null;

        ConditionFragment cFrag = null;

        // Start of the process
        if (start == true && isAlternative == false) {
            start = false;
            ModifierRecord modRecord = new ModifierRecord(ModifierRecord.TYPE_ADV, ModifierRecord.TARGET_VERB);
            modRecord.addAttribute("starting_point", "+");

            String bo = anno.getBusinessObjects().get(0);
            eFrag = new ExecutableFragment(anno.getActions().get(0), bo, "", anno.getAddition());

            eFrag.addAssociation(activity.getId());
            eFrag.addMod("the process begins when", modRecord);

            String role = getRole(activity, eFrag);
            eFrag.setRole(role);
            if (anno.getActions().size() == 2) {
                ExecutableFragment eFrag2 = null;
                if (anno.getBusinessObjects().size() == 2) {
                    eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
                    eFrag2.addAssociation(activity.getId());
                } else {
                    eFrag2 = new ExecutableFragment(anno.getActions().get(1), "", "", "");
                    eFrag2.addAssociation(activity.getId());
                }

                correctArticleSettings(eFrag2);
                eFrag.addSentence(eFrag2);
            }

            if (bo.endsWith("s") && lHelper.isNoun(bo.substring(0, bo.length() - 1))) {
                eFrag.bo_hasArticle = true;
            } else {
                eFrag.bo_hasIndefArticle = true;
            }

            // If imperative mode
            if (imperative == true && imperativeRole.equals(role) == true) {
                eFrag.verb_isImperative = true;
                eFrag.role_isImperative = true;
            }
            correctArticleSettings(eFrag);
            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(ProcessElementType.ACTIVITY.getValue());
            sentencePlan.add(dsyntSentence);
            activitiySentenceMap.add(new Pair<Integer, DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
            planned = true;
        }

        // Standard case
        eFrag = new ExecutableFragment(anno.getActions().get(0), anno.getBusinessObjects().get(0), "", anno.getAddition());
        eFrag.addAssociation(activity.getId());
        String role = getRole(activity, eFrag);
        eFrag.setRole(role);
        if (anno.getActions().size() == 2) {
            ExecutableFragment eFrag2 = null;
            if (anno.getBusinessObjects().size() == 2) {
                eFrag2 = new ExecutableFragment(anno.getActions().get(1), anno.getBusinessObjects().get(1), "", "");
                if (eFrag.verb_IsPassive == true) {
                    if (anno.getBusinessObjects().get(0).equals("") == true) {
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
                if (eFrag.verb_IsPassive == true) {
                    eFrag2.verb_IsPassive = true;
                }
            }

            correctArticleSettings(eFrag2);
            eFrag2.addAssociation(activity.getId());
            eFrag.addSentence(eFrag2);
        }

        eFrag.sen_level = level;
        if (imperative == true && imperativeRole.equals(role) == true) {
            correctArticleSettings(eFrag);
            eFrag.verb_isImperative = true;
            eFrag.setRole("");
        }
        if (activity.getSubProcessID() > 0) {
            eFrag.sen_level = level + 1;
        }

        // In case of passed modifications (NOT AND - Split)
        if (passedMods.size() > 0 && planned == false) {
            correctArticleSettings(eFrag);
            eFrag.addMod(passedMods.get(0).getLemma(), passedMods.get(0));
            eFrag.sen_hasConnective = true;
            passedMods.clear();
        }

        // In case of passed modifications (e.g. AND - Split)
        if (passedMod != null && planned == false) {
            correctArticleSettings(eFrag);
            eFrag.addMod(passedMod.getLemma(), passedMod);
            eFrag.sen_hasConnective = true;
            passedMod = null;
        }

        if (tagWithBullet == true) {
            eFrag.sen_hasBullet = true;
            tagWithBullet = false;
        }

        // In case of passed fragments (General handling)
        if (passedFragments.size() > 0 && planned == false) {
            correctArticleSettings(eFrag);
            DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(eFrag, passedFragments, ProcessElementType.ACTIVITY.getValue());
            sentencePlan.add(dsyntSentence);
            activitiySentenceMap.add(new Pair<Integer, DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
            passedFragments.clear();
            planned = true;
        }

        if (planned == false) {
            correctArticleSettings(eFrag);
            DSynTMainSentence dsyntSentence = new DSynTMainSentence(eFrag);
            dsyntSentence.addProcessElementDocument(ProcessElementType.ACTIVITY.getValue());
            sentencePlan.add(dsyntSentence);
            activitiySentenceMap.add(new Pair<Integer, DSynTSentence>(Integer.valueOf(node.getEntry().getId()), dsyntSentence));
        }


        // If activity has attached Events
        if (activity.hasAttachedEvents()) {
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
                    TextPlanner converter = new TextPlanner(rpst, alternative, lDeriver, lHelper, imperativeRole, imperative, true);
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
                        sen.addProcessElementDocument(ProcessElementType.ACTIVITY.getValue());
                        sentencePlan.add(sen);
                    }
                    converter = null;

                    // Print sentence for subsequent normal execution
                    DSynTSentence dSynTSentence = textToIMConverter.getAttachedEventPostStatement(alternative.getEvents().get(attEvent));
                    dSynTSentence.addProcessElementDocument(ProcessElementType.ACTIVITY.getValue());
                    sentencePlan.add(dSynTSentence);
                }
            }
        }


        if (depth > 0) {
            convertToText(node, level);
        }
    }

    // Checks and corrects the article settings.
    public void correctArticleSettings(AbstractFragment frag) {
        String bo = frag.getBo();
        if (bo.endsWith("s") && bo.endsWith("ss") == false && frag.bo_hasArticle == true && lHelper.isNoun(bo.substring(0, bo.length() - 1)) == true) {
            bo = bo.substring(0, bo.length() - 1);
            frag.setBo(bo);
            frag.bo_isPlural = true;
        }
        if (bo.contains("&")) {
            frag.bo_isPlural = true;
        }
        if (frag.bo_hasArticle == true) {
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

    /**
     * HANDLE EVENT
     */

    private void handleEvent(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) {
        Event event = process.getEvents().get((Integer.valueOf(node.getEntry().getId())));
        int currentPosition = orderedTopNodes.indexOf(node);
        // Start Event
        if (currentPosition == 0) {

            // Start event should be printed
            if (start == true && isAlternative == false) {

                // Event is followed by gateway --> full sentence
                if (event.getType() == EventType.START_EVENT && currentPosition < orderedTopNodes.size() - 1 && PlanningHelper.isBond(orderedTopNodes.get(currentPosition + 1))) {
                    start = false;
                    ExecutableFragment eFrag = new ExecutableFragment("start", "process", "", "with a decision");
                    eFrag.add_hasArticle = false;
                    eFrag.bo_isSubject = true;
                    DSynTSentence dSynTSentence = new DSynTMainSentence(eFrag);
                    dSynTSentence.addProcessElementDocument(ProcessElementType.STARTEVENT.getValue());
                    sentencePlan.add(dSynTSentence);
                }
                if (event.getType() != EventType.START_EVENT) {
                    start = false;
                    ConverterRecord convRecord = textToIMConverter.convertEvent(event);
                    if (convRecord != null && convRecord.hasPreStatements() == true) {
                        DSynTSentence dSynTSentence = convRecord.preStatements.get(0);
                        dSynTSentence.addProcessElementDocument(ProcessElementType.STARTEVENT.getValue());
                        sentencePlan.add(dSynTSentence);
                    }
                }
            }


            // Intermediate Events
        } else {
            ConverterRecord convRecord = textToIMConverter.convertEvent(event);

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

                    if (tagWithBullet == true) {
                        sen.getExecutableFragment().sen_hasBullet = true;
                        sen.getExecutableFragment().sen_level = level;
                        tagWithBullet = false;
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
                        DSynTConditionSentence dsyntSentence = getDSyntConditionSentence(sen.getExecutableFragment(), passedFragments, ProcessElementType.INTERMEDIATEEVENT.getValue());
                        sentencePlan.add(dsyntSentence);
                        passedFragments.clear();
                    } else {
                        if (sen.getClass().toString().endsWith("DSynTConditionSentence")) {
                            DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(sen.getExecutableFragment(), ((DSynTConditionSentence) sen).getConditionFragment());
                            dsyntSentence.addProcessElementDocument(ProcessElementType.INTERMEDIATEEVENT.getValue());
                            sentencePlan.add(dsyntSentence);
                        } else {
                            DSynTMainSentence dsyntSentence = new DSynTMainSentence(sen.getExecutableFragment());
                            dsyntSentence.addProcessElementDocument(ProcessElementType.INTERMEDIATEEVENT.getValue());
                            sentencePlan.add(dsyntSentence);
                        }
                    }
                }
            }
        }
    }

    private void handleEndEvent(RPSTNode<ControlFlow, Node> node, int level) {
        Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
        DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
        sen.getExecutableFragment().sen_level = level;
        sen.addProcessElementDocument("8");
        sentencePlan.add(sen);
    }

    private void handleEndEvent(RPSTNode<ControlFlow, Node> node, ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes, int level) {
        end = false;
        Event event = process.getEvents().get((Integer.valueOf(node.getExit().getId())));
        if (event.getType() == EventType.END_EVENT && orderedTopNodes.indexOf(node) == orderedTopNodes.size() - 1) {
            // Adjust level and add to sentence plan
            DSynTSentence sen = textToIMConverter.convertEvent(event).preStatements.get(0);
            sen.getExecutableFragment().sen_level = level;
            if (event.getSubProcessID() > 0) {
                sen.getExecutableFragment().sen_level = level + 1;
            }
            sen.addProcessElementDocument(ProcessElementType.ENDEVENT.getValue());
            sentencePlan.add(sen);
        }
    }

    /**
     * OTHERS
     */

    // Returns role of a fragment.
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

    public ArrayList<DSynTSentence> getSentencePlan() {
        return sentencePlan;
    }

    // DSYNT CONDITION SENTENCE

    private DSynTConditionSentence getDSyntConditionSentence(ExecutableFragment eFrag, List<ConditionFragment> passedFragments, String processElementMain) {
        DSynTConditionSentence dsyntSentence = new DSynTConditionSentence(eFrag, passedFragments.get(0));
        if (passedFragments.size() > 1) {
            for (int i = 1; i < passedFragments.size(); i++) {
                dsyntSentence.addCondition(passedFragments.get(i), true);
                dsyntSentence.getConditionFragment().addCondition(passedFragments.get(i));
            }
        }

        generateProcessElementList(dsyntSentence, processElementMain);
        return dsyntSentence;
    }

    private void generateProcessElementList(DSynTConditionSentence dSynTSentence, String processElementMain) {
        String processElement = processElementMain;
        Document document = dSynTSentence.getDocuments().get(0);
        dSynTSentence.addProcessElementDocument(processElement, document);

        for (int i = 0; i < passedFragments.size(); i++) {
            processElement = passedFragments.get(i).getProcessElement();
            document = dSynTSentence.getDocuments().get(i + 1);
            dSynTSentence.addProcessElementDocument(processElement, document);
        }
    }

}


