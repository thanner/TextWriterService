package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.changes.sentenceRealization.SurfaceRealizer;
import br.edu.ufrgs.inf.bpm.changes.textPlanning.TextPlanner;
import br.edu.ufrgs.inf.bpm.util.Paths;
import br.edu.ufrgs.inf.bpm.util.ResourceLoader;
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
import processToText.sentencePlanning.DiscourseMarker;
import processToText.sentencePlanning.ReferringExpressionGenerator;
import processToText.sentencePlanning.SentenceAggregator;

import java.io.IOException;
import java.util.ArrayList;

public class TextGenerator {

    /**
     * Function for generating text from a model. The according process model must be provided to the function.
     */
    public static String generateText(ProcessModel model, int counter) throws IOException, JWNLException {
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
        TextPlanner converter = new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false);
        converter.convertToText(rpst.getRoot(), 0);
        ArrayList<DSynTSentence> sentencePlan = converter.getSentencePlan();

        // Aggregation
        SentenceAggregator sentenceAggregator = new SentenceAggregator(lHelper);
        sentencePlan = sentenceAggregator.performRoleAggregation(sentencePlan, model);

        // Referring Expression
        ReferringExpressionGenerator refExpGenerator = new ReferringExpressionGenerator(lHelper);
        sentencePlan = refExpGenerator.insertReferringExpressions(sentencePlan, model, false);

        // Discourse Marker
        DiscourseMarker discourseMarker = new DiscourseMarker();
        sentencePlan = discourseMarker.insertSequenceConnectives(sentencePlan);

        // Realization
        SurfaceRealizer surfaceRealizer = new SurfaceRealizer();
        String surfaceText = surfaceRealizer.realizePlan(sentencePlan);

        // Cleaning
        if (imperative == true) {
            surfaceText = surfaceRealizer.cleanTextForImperativeStyle(surfaceText, imperativeRole, model.getLanes());
        }

        surfaceText = surfaceRealizer.postProcessText(surfaceText);

        // if(surfaceText.startsWith(" \n")){
        //    surfaceText = surfaceText.replaceFirst(" \n", "" );
        // }

        return surfaceText;
    }

}
