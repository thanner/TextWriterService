package br.edu.ufrgs.inf.bpm.bpmparser.wrapper; /**
 * package br.edu.ufrgs.bpmparser.wrapper;
 * <p>
 * import br.edu.ufrgs.bpmparser.ontology.ElementType;
 * import br.edu.ufrgs.bpmparser.ontology.ProcessElement;
 * import com.inubit.research.textToProcess.worldModel.Action;
 * import com.inubit.research.textToProcess.worldModel.WorldModel;
 * import net.frapu.code.visualization.bpmn.BPMNModel;
 * import net.frapu.code.visualization.bpmn.FlowObject;
 * <p>
 * import java.util.ArrayList;
 * import java.util.List;
 * <p>
 * public class TextToProcessWrapper {
 * <p>
 * private List<ProcessElement> processElementList;
 * <p>
 * // TODO: Teste 1 (WorldModel)
 * <p>
 * public List<ProcessElement> convertToOntology(WorldModel worldModel) {
 * processElementList = new ArrayList<>();
 * worldModel.getActions().stream().forEach(a -> getActivity(a));
 * return new ArrayList<>();
 * }
 * <p>
 * private void getActivity(Action action) {
 * ProcessElement processElement = new ProcessElement(ElementType.ACTIVITY);
 * // processElement.addDataProperty("", action.get);
 * processElementList.add(processElement);
 * }
 * <p>
 * // TODO: Teste 2 (BPMNModel)
 * <p>
 * public List<ProcessElement> convertToOntology(BPMNModel bpmnModel) {
 * processElementList = new ArrayList<>();
 * bpmnModel.getFlowObjects().stream().forEach(a -> getActivity(a));
 * return new ArrayList<>();
 * }
 * <p>
 * private void getActivity(FlowObject activity) {
 * ProcessElement processElement = new ProcessElement(ElementType.ACTIVITY);
 * processElement.addDataProperty("name", activity.getText());
 * processElementList.add(processElement);
 * }
 * }
 **/