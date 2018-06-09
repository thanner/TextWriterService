package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.bpmn.TDefinitions;
import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.DiscourseMarker;
import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.ReferringExpressionGenerator;
import br.edu.ufrgs.inf.bpm.changes.sentencePlanning.SentenceAggregator;
import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.changes.textPlanning.TextPlanner;
import br.edu.ufrgs.inf.bpm.rest.textwriter.model.Text;
import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.WordNetWrapper;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.process.ProcessModel;
import processToText.preprocessing.FormatConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class TextGenerator {

    public static Text generateText(String bpmnString) throws IOException, JWNLException {
        TDefinitions definitions = JaxbWrapper.convertXMLToObject(bpmnString);

        ProcessModelBuilder processModelBuilder = new ProcessModelBuilder();
        ProcessModel processModel = processModelBuilder.buildProcess(definitions);
        Map<Integer, String> bpmnIdMap = processModelBuilder.getBpmnIdMap();

        return TextGenerator.generateText(processModel, bpmnIdMap, 0);
    }

    public static Text generateText(ProcessModel model, Map<Integer, String> bpmnIdMap, int counter) throws IOException, JWNLException {
        Dictionary dictionary = WordNetWrapper.getDictionary();
        MaxentTagger maxentTagger = new MaxentTagger(ResourceLoader.getResource(Paths.StanfordBidirectionalDistsimPath));
        EnglishLabelHelper lHelper = new EnglishLabelHelper(dictionary, maxentTagger);
        EnglishLabelDeriver lDeriver = new EnglishLabelDeriver(lHelper);

        String imperativeRole = "";
        boolean imperative = false;

        // Annotate model
        model.annotateModel(0, lDeriver, lHelper);

        // Convert to RPST
        FormatConverter formatConverter = new FormatConverter();
        Process p = formatConverter.transformToRPSTFormat(model);
        RPST<ControlFlow, Node> rpst = new RPST<ControlFlow, Node>(p);

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
        TextPlanner converter = new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false, bpmnIdMap);
        converter.convertToText(rpst.getRoot(), 0);
        ArrayList<DSynTSentence> sentencePlan = converter.getSentencePlan();

        // Aggregation (Only ROLE aggregation)
        SentenceAggregator sentenceAggregator = new SentenceAggregator();
        sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan);

        // Referring Expression (He, She, It)
        ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
        sentencePlan = refExpGenerator.insertReferringExpressions(sentencePlan, false);

        sentencePlan.forEach(DSynTSentence::fixDocuments);

        // Discourse Marker
        DiscourseMarker discourseMarker = new DiscourseMarker();
        sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);

        // Realization
        SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
        // String surfaceText = surfaceRealizer.generateXMLSentence(sentencePlan);
        Text text = surfaceRealizer.generateText(sentencePlan);

        // Cleaning
        // if (imperative) {
        //    surfaceText = surfaceRealizer.cleanTextForImperativeStyle(surfaceText, imperativeRole, model.getLanes());
        // }

        text = surfaceRealizer.postProcessText(text);

        return text;
    }

}
