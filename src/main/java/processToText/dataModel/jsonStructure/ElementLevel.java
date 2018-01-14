package processToText.dataModel.jsonStructure;

import java.util.ArrayList;

public class ElementLevel {

    String resourceId;
    processToText.dataModel.jsonStructure.ElementProperties properties;
    Stencil stencil;
    ArrayList<ElementLevel> childShapes;
    ArrayList<ElementLevel> outgoing;
    ArrayList<ElementLevel> dockers;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public processToText.dataModel.jsonStructure.ElementProperties getProps() {
        return properties;
    }

    public void setProps(processToText.dataModel.jsonStructure.ElementProperties props) {
        this.properties = props;
    }

    public Stencil getStencil() {
        return stencil;
    }

    public void setStencil(Stencil stencil) {
        this.stencil = stencil;
    }

    public ArrayList<ElementLevel> getChildShapes() {
        return childShapes;
    }

    public void setChildShapes(ArrayList<ElementLevel> childShapes) {
        this.childShapes = childShapes;
    }

    public ArrayList<ElementLevel> getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(ArrayList<ElementLevel> outgoing) {
        this.outgoing = outgoing;
    }

    public ArrayList<ElementLevel> getDockers() {
        return dockers;
    }

    public void setDockers(ArrayList<ElementLevel> dockers) {
        this.dockers = dockers;
    }


}
