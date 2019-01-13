package br.edu.ufrgs.inf.bpm.validation;

import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;

import java.util.HashMap;
import java.util.Map;

public class ValidationDataText {
    private String textName;
    private Map<ProcessElementType, ProcessElementValidation> validationElements = new HashMap<>();

    public String getTextName() {
        return textName;
    }

    public void setTextName(String textName) {
        this.textName = textName;
    }

    public Map<ProcessElementType, ProcessElementValidation> getValidationElements() {
        return validationElements;
    }

    public void setValidationElements(Map<ProcessElementType, ProcessElementValidation> validationElements) {
        this.validationElements = validationElements;
    }

    public void setAmountInstancesFoundCorrectly(ProcessElementType processElementType, int amount) {
        verifyElementExists(processElementType);
        validationElements.get(processElementType).setAmountInstancesFoundCorrectly(amount);
    }

    public void setTotalInstances(ProcessElementType processElementType, int amount) {
        verifyElementExists(processElementType);
        validationElements.get(processElementType).setTotalInstances(amount);
    }

    public void addProcessElementNotMapped(ProcessElementType processElementType, String originalElementId) {
        verifyElementExists(processElementType);
        validationElements.get(processElementType).addIdNotMapped(originalElementId);
    }

    public void verifyElementExists(ProcessElementType processElementType) {
        if (!validationElements.containsKey(processElementType)) {
            validationElements.put(processElementType, new ProcessElementValidation());
        }
    }

}
