package br.edu.ufrgs.inf.bpm.validation;

import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;
import br.edu.ufrgs.inf.bpm.textmetadata.TSentence;
import br.edu.ufrgs.inf.bpm.textmetadata.TSnippet;
import br.edu.ufrgs.inf.bpm.textmetadata.TText;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import br.edu.ufrgs.inf.bpm.wrapper.JaxbWrapper;
import org.omg.spec.bpmn._20100524.model.*;

import javax.xml.bind.JAXBElement;
import java.util.ArrayList;
import java.util.List;

public class Validation {

    private ValidationDataText validationDataText;

    private TText metaText;
    private List<TProcess> originalProcessList;

    public ValidationDataText getTextValidationData(String processName, String bpmnString, TText metaText) {
        TDefinitions definitions = JaxbWrapper.convertXMLToObject(bpmnString);
        BpmnWrapper processModelWrapper = new BpmnWrapper(definitions);
        this.originalProcessList = processModelWrapper.getProcessList();
        this.metaText = metaText;

        validationDataText = new ValidationDataText();
        validationDataText.setTextName(processName);

        validateElement(ProcessElementType.ACTIVITY, getProcessElementList(TActivity.class));
        validateElement(ProcessElementType.STARTEVENT, getProcessElementList(TStartEvent.class));
        validateElement(ProcessElementType.ENDEVENT, getProcessElementList(TEndEvent.class));
        Class[] intermediateEvent = {TIntermediateCatchEvent.class, TIntermediateThrowEvent.class};
        validateElement(ProcessElementType.INTERMEDIATEEVENT, getProcessElementList(intermediateEvent));
        validateElement(ProcessElementType.XORSPLIT, getProcessElementGatewayList(TExclusiveGateway.class, true));
        validateElement(ProcessElementType.XORJOIN, getProcessElementGatewayList(TExclusiveGateway.class, false));
        validateElement(ProcessElementType.ANDSPLIT, getProcessElementGatewayList(TParallelGateway.class, true));
        validateElement(ProcessElementType.ANDJOIN, getProcessElementGatewayList(TParallelGateway.class, false));
        validateElement(ProcessElementType.ORSPLIT, getProcessElementGatewayList(TInclusiveGateway.class, true));
        validateElement(ProcessElementType.ORJOIN, getProcessElementGatewayList(TInclusiveGateway.class, false));
        validateElement(ProcessElementType.GATEWAYBASEDEVENTSPLIT, getProcessElementGatewayList(TEventBasedGateway.class, true));

        return validationDataText;
    }

    private void validateElement(ProcessElementType processElementType, List<String> processElements) {
        List<String> textElements = getElementsMetaText(processElementType.value());
        validateProcessElementType(processElements, textElements, processElementType);
    }

    private List<String> getProcessElementList(Class processElementClass) {
        List<String> processElements = new ArrayList<>();
        for (TProcess process : originalProcessList) {
            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (processElementClass.isInstance(flowElement.getValue())) {
                    processElements.add((flowElement.getValue()).getId());
                }
            }
        }

        return processElements;
    }

    private List<String> getProcessElementList(Class[] processElementClassList) {
        List<String> processElements = new ArrayList<>();

        for (Class clazz : processElementClassList) {
            processElements.addAll(getProcessElementList(clazz));
        }

        return processElements;
    }

    private List<String> getProcessElementGatewayList(Class processElementClass, boolean isSplit) {
        List<String> processElements = new ArrayList<>();
        for (TProcess process : originalProcessList) {
            for (JAXBElement<? extends TFlowElement> flowElement : process.getFlowElement()) {
                if (processElementClass.isInstance(flowElement.getValue())) {
                    TFlowNode flowNode = (TFlowNode) flowElement.getValue();
                    if (isSplit && flowNode.getOutgoing().size() > 1) {
                        processElements.add((flowElement.getValue()).getId());
                    } else if (!isSplit && flowNode.getIncoming().size() > 1) {
                        processElements.add((flowElement.getValue()).getId());
                    }
                }
            }
        }

        return processElements;
    }

    private List<String> getElementsMetaText(String elementTypeName) {
        List<String> elementTypeIds = new ArrayList<>();

        for (TSentence sentence : metaText.getSentenceList()) {
            for (TSnippet snippet : sentence.getSnippetList()) {
                if (snippet.getProcessElementType().equals(elementTypeName)) {
                    elementTypeIds.add(snippet.getProcessElementId());
                }
            }
        }
        return elementTypeIds;
    }

    private void validateProcessElementType(List<String> originalProcessList, List<String> textProcessList, ProcessElementType processElementType) {
        if (!originalProcessList.isEmpty()) {
            validationDataText.setTotalInstances(processElementType, originalProcessList.size());

            int amountFoundCorrectly = 0;
            for (String originalElementId : originalProcessList) {
                if (textProcessList.contains(originalElementId)) {
                    amountFoundCorrectly++;
                } else {
                    validationDataText.addProcessElementNotMapped(processElementType, originalElementId);
                }
            }
            validationDataText.setAmountInstancesFoundCorrectly(processElementType, amountFoundCorrectly);
        }
    }

}
