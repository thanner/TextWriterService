package br.edu.ufrgs.inf.bpm.wrapper;


import org.camunda.bpm.model.bpmn.instance.*;
import processToText.dataModel.process.ProcessModel;

public class TestBpmnXml {
    public static void main(String[] args) {

        BpmnXmlWrapper bpmnXmlWrapper = new BpmnXmlWrapper();

        // Create structure
        String processId = "Process1";
        bpmnXmlWrapper.createPool(processId, "Pool");
        LaneSet laneSet1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "LaneSet1", "LaneSet Name", LaneSet.class);
        Lane lane1 = bpmnXmlWrapper.createElement(laneSet1, "Lane1", "Lane1 Name", Lane.class);
        Lane lane2 = bpmnXmlWrapper.createElement(laneSet1, "Lane2", "Lane2 Name", Lane.class);

        // Create Flow Elements
        StartEvent startEvent1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "StartEvent1", "StartEvent1 Name", StartEvent.class);
        Task task1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "Task1", "Task1 Name", Task.class);
        SubProcess subprocess1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "Subprocess1", "Subprocess1 Name", SubProcess.class);
        Gateway gateway1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "Gateway1", "Gateway1 Name", EventBasedGateway.class);
        EndEvent endEvent1 = bpmnXmlWrapper.createElement(bpmnXmlWrapper.getProcess(processId), "EndEvent1", "EndEvent1 Name", EndEvent.class);

        // Create Sequence Flow
        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess(processId), "SequenceFlow1 Name", startEvent1, task1);
        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess(processId), "SequenceFlow2 Name", task1, subprocess1);
        bpmnXmlWrapper.createSequenceFlow(bpmnXmlWrapper.getProcess(processId), "SequenceFlow3 Name", subprocess1, endEvent1);

        // Create relation between Flow Elements and Lanes
        lane1.getFlowNodeRefs().add(startEvent1);
        lane1.getFlowNodeRefs().add(task1);
        lane1.getFlowNodeRefs().add(subprocess1);
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
