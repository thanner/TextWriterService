package br.edu.ufrgs.inf.bpm.rest.processToText;

import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import net.didion.jwnl.JWNLException;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.process.ProcessModel;
import processToText.preprocessing.FormatConverter;
import processToText.sentencePlanning.DiscourseMarker;
import processToText.sentencePlanning.ReferringExpressionGenerator;
import processToText.sentencePlanning.SentenceAggregator;
import processToText.sentenceRealization.SurfaceRealizer;
import processToText.textPlanning.TextPlanner;

import java.io.IOException;
import java.util.ArrayList;

public class TextGeneration {

    /**
     * Function for generating text from a model. The according process model must be provided to the function.
     */
    public static String toText(ProcessModel model, int counter) throws JWNLException, IOException {
        EnglishLabelHelper lHelper = new EnglishLabelHelper();
        EnglishLabelDeriver lDeriver = new EnglishLabelDeriver(lHelper);

        String imperativeRole = "";
        boolean imperative = false;

        // Annotate model
        model.annotateModel(0, lDeriver, lHelper);

        // Convert to RPST
        FormatConverter formatConverter = new FormatConverter();
        Process p = formatConverter.transformToRPSTFormat(model);
        RPST<ControlFlow, Node> rpst = new RPST<ControlFlow, Node>(p);

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
        return surfaceText;
    }

}
