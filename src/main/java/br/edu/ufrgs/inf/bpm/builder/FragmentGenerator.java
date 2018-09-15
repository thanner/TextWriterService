package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoader;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoaderType;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentGenerator {

    private static TemplateLoader templateLoader = new TemplateLoader();

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType templateLoaderType) {
        return generateExecutableFragment(templateLoaderType, new HashMap<>());
    }

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType templateLoaderType, Map<String, String> modificationMap) {
        templateLoader.loadTemplate(templateLoaderType);
        adjustModifications(modificationMap);
        return new ExecutableFragment(templateLoader.getAction(), templateLoader.getObject(), "", templateLoader.getAddition());
    }

    public static ConditionFragment generateConditionFragment(TemplateLoaderType templateLoaderType, Map<String, String> modificationMap, int conditionFragment) {
        templateLoader.loadTemplate(templateLoaderType);
        adjustModifications(modificationMap);
        return new ConditionFragment(templateLoader.getAction(), templateLoader.getObject(), "", templateLoader.getAddition(), conditionFragment, new HashMap<>());
    }

    private static void adjustModifications(Map<String, String> modificationMap) {
        for (Map.Entry<String, String> modification : modificationMap.entrySet()) {
            templateLoader.setAction(templateLoader.getAction().replaceAll(modification.getKey(), modification.getValue()));
            templateLoader.setObject(templateLoader.getObject().replaceAll(modification.getKey(), modification.getValue()));
            templateLoader.setAddition(templateLoader.getAddition().replaceAll(modification.getKey(), modification.getValue()));
        }
    }

}
