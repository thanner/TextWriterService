package br.edu.ufrgs.inf.bpm.wrapper;


import org.camunda.bpm.model.bpmn.instance.*;
import processToText.dataModel.process.ProcessModel;

public class TestBpmnXml {
    public static void main(String[] args) {

        BpmnXmlWrapper bpmnXmlWrapper = new BpmnXmlWrapper();

        bpmnXmlWrapper.createPool("process1", "Piscina");

        LaneSet laneSet1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "LaneSet1", "Nome da pool", LaneSet.class);

        Lane lane1 = bpmnXmlWrapper.createElement(laneSet1, "Raia1", "lane1", Lane.class);

        Lane lane2 = bpmnXmlWrapper.createElement(laneSet1, "Raia2", "lane2", Lane.class);

        // create elements
        StartEvent startEvent1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "EventoInicio1", "Nome do evento de inicio", StartEvent.class);

        Task task1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "Tarefa1", "Nome da tarefa 1", Task.class);
        SubProcess task2 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "Tarefa2", "Nome da tarefa 2", SubProcess.class);

        EndEvent endEvent1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "EventoFim1", "Nome do evento de fim", EndEvent.class);

        Gateway gateway1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess("process1"), "gateway1", "Nome do gateway", EventBasedGateway.class);

        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess("process1"), "Nome do arco", startEvent1, task1);
        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess("process1"), "Nome do arco 2", task1, task2);
        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess("process1"), "Nome do arco 3", task2, endEvent1);

        // Relacionar lanes
        lane1.getFlowNodeRefs().add(startEvent1);
        lane1.getFlowNodeRefs().add(task1);
        lane1.getFlowNodeRefs().add(task2);
        lane2.getFlowNodeRefs().add(gateway1);
        lane1.getFlowNodeRefs().add(endEvent1);

        String bpmnString = bpmnXmlWrapper.getBpmnXmlString();
        System.out.println(bpmnString);

        ProcessModelXmlBuilder processModelXmlBuilder = new ProcessModelXmlBuilder();
        ProcessModel processModel = processModelXmlBuilder.buildProcess(bpmnString);

        processModel.print();

        // create process
        /*
        Process process2 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getDefinitions(), "Process2", Process.class);
        participant2.setProcess(process2);

        StartEvent startEvent2 = bpmnXmlWrapper.createElement(process2, "EventoInicio2", StartEvent.class);
        Task task3 = bpmnXmlWrapper.createElement(process2, "Tarefa3", Task.class);
        EndEvent endEvent2 = bpmnXmlWrapper.createElement(process2, "EventoFim2", EndEvent.class);

        bpmnXmlWrapper.createSequenceFlow(process2, startEvent2, task3);
        bpmnXmlWrapper.createSequenceFlow(process2, task3, endEvent2);

        System.out.println(bpmnXmlWrapper.getBpmnXmlString());
        */
    }
}
