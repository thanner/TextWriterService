package processToText.dataModel.process;

public class Arc {

    private int id;
    private String label;
    private processToText.dataModel.process.Element source;
    private processToText.dataModel.process.Element target;
    private String type;

    public Arc(int id, String label, processToText.dataModel.process.Element source, processToText.dataModel.process.Element target) {
        this.id = id;
        this.label = label;
        this.source = source;
        this.target = target;
        this.type = "";
    }

    public Arc(int id, String label, processToText.dataModel.process.Element source, processToText.dataModel.process.Element target, String type) {
        this.id = id;
        this.label = label;
        this.source = source;
        this.target = target;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public processToText.dataModel.process.Element getSource() {
        return source;
    }

    public void setSource(processToText.dataModel.process.Element source) {
        this.source = source;
    }

    public processToText.dataModel.process.Element getTarget() {
        return target;
    }

    public void setTarget(processToText.dataModel.process.Element target) {
        this.target = target;
    }


}
