package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.textmetadata.TProcess;
import br.edu.ufrgs.inf.bpm.textmetadata.TResource;
import br.edu.ufrgs.inf.bpm.wrapper.BpmnWrapper;
import org.omg.spec.bpmn._20100524.model.TDefinitions;
import org.omg.spec.bpmn._20100524.model.TLane;

import java.util.ArrayList;
import java.util.List;

public class MetaTextProcessGenerator {

    public static List<TProcess> generateMetaTextProcess(TDefinitions definitions) {
        List<TProcess> tProcesseList = new ArrayList<>();
        BpmnWrapper bpmnWrapper = new BpmnWrapper(definitions);

        for (org.omg.spec.bpmn._20100524.model.TProcess processModel : bpmnWrapper.getProcessList()) {
            TProcess tProcess = new TProcess();
            tProcess.setId(processModel.getId());
            tProcess.setName(processModel.getName());

            for (TLane laneModel : bpmnWrapper.getDeepLanesByProcess(processModel)) {
                TResource tResource = new TResource();
                tResource.setId(laneModel.getId());
                tResource.setName(laneModel.getName());
                tProcess.getResourceList().add(tResource);
            }

            tProcesseList.add(tProcess);
        }

        return tProcesseList;
    }

}
