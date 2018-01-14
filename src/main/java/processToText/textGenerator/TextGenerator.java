package processToText.textGenerator;

import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Node;
import de.hpi.bpt.process.Process;
import processToText.contentDetermination.labelAnalysis.EnglishLabelDeriver;
import processToText.contentDetermination.labelAnalysis.EnglishLabelHelper;
import processToText.dataModel.dsynt.DSynTSentence;
import processToText.dataModel.pnmlReader.PNMLReader;
import processToText.dataModel.pnmlReader.PetriNet.PetriNet;
import processToText.dataModel.pnmlReader.PetriNetToProcessConverter;
import processToText.dataModel.process.ProcessModel;
import processToText.preprocessing.FormatConverter;
import processToText.preprocessing.RigidStructurer;
import processToText.sentencePlanning.DiscourseMarker;
import processToText.sentencePlanning.ReferringExpressionGenerator;
import processToText.sentencePlanning.SentenceAggregator;
import processToText.sentenceRealization.SurfaceRealizer;
import processToText.textPlanning.PlanningHelper;
import processToText.textPlanning.TextPlanner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class TextGenerator {

    public TextGenerator() {
    }

    public String toText(String input) throws Exception {

        String imperativeRole = "";
        boolean imperative = false;

        File inputFile = new File(input);
        PNMLReader pnmlReader = new PNMLReader();
        PetriNet petriNet = pnmlReader.getPetriNetFromPNML(inputFile);
        PetriNetToProcessConverter pnConverter = new PetriNetToProcessConverter();
        ProcessModel model = pnConverter.convertToProcess(petriNet);

        HashMap<Integer, String> transformedElemsRev = pnConverter.transformedElemsRev;

        EnglishLabelHelper lHelper = new EnglishLabelHelper();
        EnglishLabelDeriver lDeriver = new EnglishLabelDeriver(lHelper);

        // Annotate model
        model.annotateModel(0, lDeriver, lHelper);

        // Convert to RPST
        FormatConverter formatConverter = new FormatConverter();
        Process p = formatConverter.transformToRPSTFormat(model);
        RPST<ControlFlow, Node> rpst = new RPST<ControlFlow, Node>(p);

        // Check for Rigids
        boolean containsRigids = PlanningHelper.containsRigid(rpst.getRoot(), 1, rpst);

        // Structure Rigid and convert back
        if (containsRigids) {
            p = formatConverter.transformToRigidFormat(model);
            RigidStructurer rigidStructurer = new RigidStructurer();
            p = rigidStructurer.structureProcess(p);
            model = formatConverter.transformFromRigidFormat(p);
            p = formatConverter.transformToRPSTFormat(model);
            rpst = new RPST<ControlFlow, Node>(p);
        }

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
        String surfaceText = surfaceRealizer.realizeSentenceMap(sentencePlan, transformedElemsRev);

        // Cleaning
        if (imperative == true) {
            surfaceText = surfaceRealizer.cleanTextForImperativeStyle(surfaceText, imperativeRole, model.getLanes());
        }
        surfaceText = surfaceRealizer.postProcessText(surfaceText);
        String newFile = appendTextToFile(input, surfaceText);
        System.out.println(newFile);
        return surfaceText;
    }

    private String appendTextToFile(String file, String text) {
        String newFile = "";
        try {
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                if (!strLine.equals("</pnml>")) {
                    newFile = newFile + strLine + "\n";
                } else {
                    newFile = newFile + text + "\n</pnml>";
                }
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newFile;
    }

}
