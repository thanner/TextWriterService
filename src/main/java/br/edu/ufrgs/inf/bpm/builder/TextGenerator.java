package br.edu.ufrgs.inf.bpm.builder;

import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import net.didion.jwnl.JWNL;
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
import processToText.sentenceRealization.SurfaceRealizer;
import processToText.textPlanning.TextPlanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class TextGenerator {

    /**
     * Function for generating text from a model. The according process model must be provided to the function.
     */
    public static String generateText(ProcessModel model, int counter) throws IOException, JWNLException {
        Dictionary dictionary = generateWordNet();

        MaxentTagger maxentTagger = new MaxentTagger(TextGenerator.class.getResource("/wsj-0-18-bidirectional-distsim.tagger").openStream());
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

        // Convert to Text
        TextPlanner converter = new TextPlanner(rpst, model, lDeriver, lHelper, imperativeRole, imperative, false);
        // FIXME: ERRO NO TextPlanner
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

        return surfaceRealizer.postProcessText(surfaceText);
    }

    private static Dictionary generateWordNet() {
        try {
            URL _url = TextGenerator.class.getResource("/file_properties.xml");
            if (_url == null) {
                File _f = new File("resources/file_properties.xml");
                JWNL.initialize(new FileInputStream(_f));
            } else {
                JWNL.initialize(_url.openStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Dictionary.getInstance();
    }

}
