package br.edu.ufrgs.inf.bpm.wrapper;/*
package br.edu.ufrgs.inf.bpm.wrapper;

import processToText.dataModel.process.ProcessModel;

public class ProcessModelBuilder {

    private ProcessModel processModel;
    private ProcessModelWrapper processModelWrapper;

    public ProcessModelBuilder() {
        this.processModelWrapper = new ProcessModelWrapper();
    }

    public ProcessModel buildProcess(br.edu.ufrgs.inf.bpm.process.Process process) {
        processModel = new ProcessModel(process.getId(), process.getName());

        process.getPools().forEach(p -> processModelWrapper.addPool(processModel, p));
        process.getLanes().forEach(l -> processModelWrapper.addLane(processModel, l));
        process.getActivites().values().forEach(a -> processModelWrapper.addActivity(processModel, a));
        process.getEvents().values().forEach(e -> processModelWrapper.addEvent(processModel, e));
        process.getGateways().values().forEach(g -> processModelWrapper.addGateway(processModel, g));
        process.getArcs().values().forEach(a -> processModelWrapper.addArc(processModel, a));

        return processModel;
    }
}
*/