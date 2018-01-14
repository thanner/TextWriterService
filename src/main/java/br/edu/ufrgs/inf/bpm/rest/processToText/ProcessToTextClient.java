package br.edu.ufrgs.inf.bpm.rest.processToText; /**
package br.edu.ufrgs.inf.bpm.rest.processToText;

import br.edu.ufrgs.inf.bpm.wrapper.ProcessModelXmlBuilder;
import processToText.dataModel.process.ProcessModel;

public class ProcessToTextClient {

    public String getText(Process process) {
        String text = "";
        TextGeneration textGeneration = new TextGeneration();
        try {
            ProcessModelXmlBuilder processModelBuilder = new ProcessModelXmlBuilder();
            ProcessModel processModel = processModelBuilder.buildProcess(process);
            text = TextGeneration.toText(processModel, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    private static void createFromFile(ProcessModel model) throws JsonSyntaxException, IOException {
        try{
                        // Multi Pool Model
                if (model.getPools().size() > 1) {
                    long time = System.currentTimeMillis();
                    System.out.println();
                    System.out.print("The model contains " + model.getPools().size() + " pools: ");
                    int count = 0;
                    for (String role : model.getPools()) {
                        if (count > 0 && model.getPools().size() > 2) {
                            System.out.print(", ");
                        }
                        if (count == model.getPools().size() - 1) {
                            System.out.print(" and ");
                        }
                        System.out.print(role + " (" + (count + 1) + ")");
                        count++;
                    }

                    HashMap<Integer, ProcessModel> newModels = model.getModelForEachPool();
                    for (ProcessModel m : newModels.values()) {
                        try {
                            m.normalize();
                            m.normalizeEndEvents();
                        } catch (Exception e) {
                            System.out.println("Error: Normalization impossible");
                            e.printStackTrace();
                        }
                        String surfaceText = toText(m, counter);
                        System.out.println(surfaceText.replaceAll(" process ", " " + m.getPools().get(0) + " process "));
                    }
                } else {
                    try {
                        model.normalize();
                        model.normalizeEndEvents();
                    } catch (Exception e) {
                        System.out.println("Error: Normalization impossible");
                        e.printStackTrace();
                    }
                    String surfaceText = toText(model, counter);
                    System.out.println(surfaceText);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
 **/
