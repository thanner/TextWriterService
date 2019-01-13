package br.edu.ufrgs.inf.bpm.validation;


import br.edu.ufrgs.inf.bpm.builder.elementType.ProcessElementType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ValidationView {

    public String getValidation(List<ValidationDataText> validationList) {
        StringBuilder sb = new StringBuilder();

        for (ValidationDataText validationDataText : validationList) {
            sb.append("New text: ").append(validationDataText.getTextName());
            sb.append("\n");
            Map<ProcessElementType, ProcessElementValidation> orderedMap = new TreeMap<>(validationDataText.getValidationElements());
            for (ProcessElementType processElementType : orderedMap.keySet()) {
                sb.append(processElementType).append(": ").append(getPrecision(orderedMap.get(processElementType))).append("\n");
                sb.append(getErrors(orderedMap.get(processElementType)));
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    private float getPrecision(ProcessElementValidation processElementValidation) {
        if (processElementValidation.getTotalInstances() == 0) {
            return 0;
        }

        return ((float) processElementValidation.getAmountInstancesFoundCorrectly() / processElementValidation.getTotalInstances()) * 100;
    }

    private String getErrors(ProcessElementValidation processElementValidation) {
        StringBuilder sb = new StringBuilder();

        if (!processElementValidation.getIdsNotMapped().isEmpty()) {
            sb.append("Errors: ").append("\n");

            for (String idNotMapped : processElementValidation.getIdsNotMapped()) {
                sb.append("\t").append("Id not mapped: ").append(idNotMapped);
                sb.append("\n");
            }
        }

        return sb.toString();
    }

}
