package br.edu.ufrgs.inf.bpm.validation;

import java.util.ArrayList;
import java.util.List;

public class ProcessElementValidation {

    private int amountInstancesFoundCorrectly;
    private int totalInstances;
    private List<String> idsNotMapped = new ArrayList<>();

    public int getAmountInstancesFoundCorrectly() {
        return amountInstancesFoundCorrectly;
    }

    public void setAmountInstancesFoundCorrectly(int amountInstancesFoundCorrectly) {
        this.amountInstancesFoundCorrectly = amountInstancesFoundCorrectly;
    }

    public int getTotalInstances() {
        return totalInstances;
    }

    public void setTotalInstances(int totalInstances) {
        this.totalInstances = totalInstances;
    }

    public void addIdNotMapped(String id) {
        idsNotMapped.add(id);
    }

    public List<String> getIdsNotMapped() {
        return idsNotMapped;
    }

}
