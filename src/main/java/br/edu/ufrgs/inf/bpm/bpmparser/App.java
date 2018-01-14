package br.edu.ufrgs.inf.bpm.bpmparser;

/**
 * package br.edu.ufrgs.bpmparser;
 * <p>
 * import br.edu.ufrgs.bpmparser.ontology.ProcessElement;
 * import br.edu.ufrgs.bpmparser.util.Paths;
 * import br.edu.ufrgs.bpmparser.wrapper.ProcessToTextWrapper;
 * import br.edu.ufrgs.bpmparser.wrapper.TextToProcessWrapper;
 * import com.inubit.research.textToProcess.TextToProcess;
 * import com.inubit.research.textToProcess.transform.ProcessModelBuilder;
 * import com.inubit.research.textToProcess.worldModel.WorldModel;
 * import net.frapu.code.visualization.bpmn.BPMNModel;
 * import org.semanticweb.owlapi.model.OWLOntology;
 * <p>
 * import java.io.File;
 * import java.util.List;
 * <p>
 * public class App {
 * <p>
 * // PQ Lexicalized parser tá chamando o BinaryGrammar e não o Old.edu.nlp.binarygrammar???
 * <p>
 * private static TextToProcess f_analyzer;
 * <p>
 * private static String fileTestPath = "Ex1 - Test";
 * <p>
 * public static void main(String[] args) {
 * File file = new File(Paths.inputTestFilesPath + fileTestPath);
 * <p>
 * <p>
 * // TEXTO PARA ONTOLOGIA
 * <p>
 * // Atual
 * TextToProcess textToProcess = null;
 * WorldModel worldModel = new WorldModel();
 * // TextToProcess f_processor = new TextToProcess(this,f_textModelControler,f_lsoControler);
 * // f_processor.analyzeText(true);
 * <p>
 * ProcessModelBuilder processModelBuilder = new ProcessModelBuilder(textToProcess);
 * BPMNModel model = processModelBuilder.createProcessModel(worldModel);
 * <p>
 * TextToProcessWrapper textToProcessWrapper = new TextToProcessWrapper();
 * List<ProcessElement> processElementList = textToProcessWrapper.convertToOntology(model);
 * <p>
 * // LISTA DE PROCESSELEMENT PARA ONTOLOGIA
 * OWLOntology owlOntology = null;
 * <p>
 * // ONTOLOGIA PARA TEXTO
 * String text = ProcessToTextWrapper.convertToText(owlOntology);
 * <p>
 * // IMPRIMIR TEXTO
 * System.out.println(text);
 * }
 * <p>
 * public void analyzeText(boolean rebuildTextModel) {
 * // Esse F_analyzer que deve criar o modelo de processo
 * f_analyzer.analyze(f_text);
 * if(rebuildTextModel) {
 * TextModel _model = f_builder.createModel(f_analyzer);
 * f_listener.textModelChanged(_model);
 * if(f_textModelControler != null)
 * f_textModelControler.setModels(this, f_analyzer,f_builder,_model);
 * }
 * ProcessModelBuilder _builder = new ProcessModelBuilder(this);
 * f_generatedModel = _builder.createProcessModel(f_analyzer.getWorld());
 * if(f_lsoControler != null)
 * f_lsoControler.setCommLinks(_builder.getCommLinks());
 * f_listener.modelGenerated(f_generatedModel);
 * }
 * <p>
 * }
 **/

/**
public class App {

    private static final String FILE_PATH = "/Users/thanner/IdeaProjects/BpmParser/src/main/resources/TestData/Text/HU/Ex1 - bycicle manufacturing.txt";

    public static void main(String[] args) {
        try {

            // TextToProcessClient textToProcessClient = new TextToProcessClient();
            // File file = new File(FILE_PATH);
            // Process process = textToProcessClient.getProcess(FileUtils.readFileToString(file, StandardCharsets.UTF_8));

            ProcessModel process = new ProcessModel(1, "Model 1");
            Pool p = new Pool(2, "Pool 1");
            Lane l = new Lane(3, "Lane 1", p);
            Activity a = new Activity(4, "Act", l, p, 1);
            Event startEvent = new Event(5, "Start Event", l, p, 1);
            Event endEvent = new Event(6, "End Event", l, p, 11);

            process.addPool(p.getName());
            process.addLane(l.getName());

            process.addEvent(startEvent);
            process.addActivity(a);
            process.addEvent(endEvent);

            process.addArc(new Arc(7, "Arc 1", startEvent, a));
            process.addArc(new Arc(8, "Arc 2", a, endEvent));

            ProcessToTextClient processToTextClient = new ProcessToTextClient();
            String text = processToTextClient.getText(process);

            System.out.println(text);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}**/