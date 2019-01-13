package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.DiscourseMarker;
import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.ReferringExpressionGenerator;
import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.SentenceAggregator;
import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.changes.textPlanning.TextPlanner;
import br.edu.ufrgs.inf.bpm.textmetadata.TSentence;
import br.edu.ufrgs.inf.bpm.textmetadata.TSnippet;
import br.edu.ufrgs.inf.bpm.textmetadata.TText;
import br.edu.ufrgs.inf.bpm.textmetadata.TTextMetadata;
import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import processToText.Main;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.process.ProcessModel;
import processToText.preprocessing.FormatConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MetaTextGenerator {

    public static EnglishLabelHelper lHelper;
    public static EnglishLabelDeriver lDeriver;

    public static TTextMetadata generateMetaText(String bpmnString) throws IOException, JWNLException {
        Dictionary dictionary = WordNetWrapper.getDictionary();
        MaxentTagger maxentTagger = new MaxentTagger(ResourceLoader.getResource(Paths.StanfordBidirectionalDistsimPath));
        lHelper = new EnglishLabelHelper(dictionary, maxentTagger);
        lDeriver = new EnglishLabelDeriver(lHelper);

        TTextMetadata TTextMetadata = new TTextMetadata();
        TText text = new TText();

        TDefinitions definitions = JaxbWrapper.convertXMLToObject(bpmnString);

        BpmnPreProcessor bpmnPreProcessor = new BpmnPreProcessor(definitions);
        bpmnPreProcessor.preProcessing();
        definitions = bpmnPreProcessor.gettDefinitions();

        ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
        ProcessModel processModel = processModelBuilder.buildProcess(definitions);
        Map<Integer, String> bpmnIdMap = processModelBuilder.getBpmnIdMap();

        int counter = 0;

        // Multi Pool Model
        TText poolText;
        HashMap<Integer, ProcessModel> modelsForPools = processModel.getModelForEachPool();
        if (processModel.getPools().size() > 1) {
            for (ProcessModel poolProcessModel : modelsForPools.values()) {
                poolProcessModel = applyNormalization(poolProcessModel);
                if (!isBlackBox(processModel)) {
                    // String processId = bpmnIdMap.get(poolProcessModel.getId());
                    poolText = generateMetaText(poolProcessModel, bpmnIdMap, counter, definitions);
                    text.getSentenceList().addAll(poolText.getSentenceList());
                    counter++;
                }
            }
        } else {
            processModel = applyNormalization(processModel);
            for (ProcessModel poolProcessModel : modelsForPools.values()) {
                if (!isBlackBox(processModel)) {
                    // String processId = bpmnIdMap.get(poolProcessModel.getId());
                    poolText = generateMetaText(processModel, bpmnIdMap, counter, definitions);
                    text.getSentenceList().addAll(poolText.getSentenceList());
                }
            }
        }

        TTextMetadata.setText(text);
        TTextMetadata.getProcessList().addAll(MetaTextProcessGenerator.generateMetaTextProcess(definitions));

        return TTextMetadata;
    }

    public static void generateOriginalText(String bpmnString) throws IOException, JWNLException {
        Dictionary dictionary = WordNetWrapper.getDictionary();
        MaxentTagger maxentTagger = new MaxentTagger(ResourceLoader.getResource(Paths.StanfordBidirectionalDistsimPath));
        Main.lHelper = new EnglishLabelHelper(dictionary, maxentTagger);
        Main.lDeriver = new EnglishLabelDeriver(lHelper);

        TDefinitions definitions = JaxbWrapper.convertXMLToObject(bpmnString);

        // Necessary
        BpmnPreProcessor bpmnPreProcessor = new BpmnPreProcessor(definitions);
        bpmnPreProcessor.preProcessing();
        definitions = bpmnPreProcessor.gettDefinitions();

        ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
        ProcessModel processModel = processModelBuilder.buildProcess(definitions);

        int counter = 0;

        // Multi Pool Model
        TText poolText;
        HashMap<Integer, ProcessModel> modelsForPools = processModel.getModelForEachPool();
        if (processModel.getPools().size() > 1) {
            for (ProcessModel poolProcessModel : modelsForPools.values()) {
                poolProcessModel = applyNormalization(poolProcessModel);
                if (!isBlackBox(processModel)) {
                    System.out.println(Main.toText(poolProcessModel, counter));
                    System.out.println();
                    counter++;
                }
            }
        } else {
            processModel = applyNormalization(processModel);
            for (ProcessModel poolProcessModel : modelsForPools.values()) {
                if (!isBlackBox(processModel)) {
                    System.out.println(Main.toText(poolProcessModel, counter));
                    System.out.println();
                }
            }
        }
    }

    private static ProcessModel applyNormalization(ProcessModel processModel) {
        // Preprocessamento para cada atividade ter um único source e target (add gateways se necessário)
        processModel.normalize();
        // Preprocessamento (tentou tirar os múltiplos eventos de fim, mas aparentemente não foi bem sucedido)
        processModel.normalizeEndEvents();
        return processModel;
    }

    private static boolean isBlackBox(ProcessModel processModel) {
        return processModel.getActivites().isEmpty() && processModel.getEvents().isEmpty() && processModel.getGateways().isEmpty() && processModel.getArcs().isEmpty();
    }

    public static TText generateMetaText(ProcessModel model, Map<Integer, String> bpmnIdMap, int counter, TDefinitions tDefinitions) throws IOException, JWNLException {
        String imperativeRole = "";
        boolean imperative = false;

        // Annotate model
        model.annotateModel(0, lDeriver, lHelper);

        // Convert to RPST
        FormatConverter formatConverter = new FormatConverter();
        Process p = formatConverter.transformToRPSTFormat(model);
        RPST<ControlFlow, Node> rpst = new RPST<>(p);

        // Check for Rigids
        // boolean containsRigids = PlanningHelper.containsRigid(rpst.getRoot(), 1, rpst);

        // Structure Rigid and convert back
        // if (containsRigids) {
        //    p = formatConverter.transformToRigidFormat(model);
        //    RigidStructurer rigidStructurer = new RigidStructurer();
        //    p = rigidStructurer.structureProcess(p);
        //    model = formatConverter.transformFromRigidFormat(p);
        //    p = formatConverter.transformToRPSTFormat(model);
        //    rpst = new RPST<ControlFlow, Node>(p);
        // }

        // Convert to Text
        TextPlanner converter = new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false, bpmnIdMap, tDefinitions);
        // processToText.textPlanning.TextPlanner converter = new processToText.textPlanning.TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false);
        converter.convertToText(rpst.getRoot(), 0);
        ArrayList<DSynTSentence> sentencePlan = converter.getSentencePlan();

        // Aggregation (Only ROLE aggregation)
        SentenceAggregator sentenceAggregator = new SentenceAggregator();
        sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan);

        // Referring Expression (He, She, It)
        ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
        sentencePlan = refExpGenerator.insertReferringExpressions(sentencePlan, true);
        sentencePlan.forEach(DSynTSentence::fixDocuments);

        // Discourse Marker (After, Then, In sequence, In the first procedure, In the meantime)
        DiscourseMarker discourseMarker = new DiscourseMarker();
        sentencePlan = discourseMarker.insertConnectives(sentencePlan, tDefinitions);

        // Realization
        SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
        TText tText = surfaceRealizer.generateText(sentencePlan);

        // Cleaning
        // if (imperative) {
        //    surfaceText = surfaceRealizer.cleanTextForImperativeStyle(surfaceText, imperativeRole, model.getLanes());
        // }

        tText = surfaceRealizer.postProcessText(tText);

        addResourceAndProcessIds(tText, tDefinitions);

        /*
        if (!model.getPools().isEmpty()) {
            processText.setProcessName(model.getPools().get(0));
        } else {
            processText.setProcessName("Unknown");
        }

        processText.setProcessId(processId);
        */

        return tText;
    }

    private static void addResourceAndProcessIds(TText tText, TDefinitions tDefinitions) {
        BpmnWrapper bpmnWrapper = new BpmnWrapper(tDefinitions);
        for (TSentence tSentence : tText.getSentenceList()) {
            for (TSnippet tSnippet : tSentence.getSnippetList()) {
                tSnippet.setResourceId(bpmnWrapper.getInternalLaneIdByFlowElementId(tSnippet.getProcessElementId()));
                tSnippet.setProcessId(bpmnWrapper.getProcessIdByFlowElementId(tSnippet.getProcessElementId()));
            }
        }
    }

}
