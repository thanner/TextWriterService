package br.edu.ufrgs.inf.bpm.builder;

import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoader;
import br.edu.ufrgs.inf.bpm.changes.templates.TemplateLoaderType;
import processToText.dataModel.intermediate.ConditionFragment;
import processToText.dataModel.intermediate.ExecutableFragment;

import java.util.HashMap;
import java.util.Map;

public class FragmentGenerator {

    private static TemplateLoader loader = new TemplateLoader();

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType template) {
        return generateExecutableFragment(template, "", new HashMap<>());
    }

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType template, String role) {
        return generateExecutableFragment(template, role, new HashMap<>());
    }

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType template, Map<String, String> modificationMap) {
        return generateExecutableFragment(template, "", modificationMap);
    }

    public static ExecutableFragment generateExecutableFragment(TemplateLoaderType template, String role, Map<String, String> modificationMap) {
        loader.loadTemplate(template);
        handlePlaceholders(modificationMap);
        return new ExecutableFragment(loader.getAction(), loader.getObject(), role, loader.getAddition());
    }

    private static void handlePlaceholders(Map<String, String> modificationMap) {
        for (Map.Entry<String, String> modification : modificationMap.entrySet()) {
            loader.setAction(loader.getAction().replace(modification.getKey(), modification.getValue()));
            loader.setObject(loader.getObject().replace(modification.getKey(), modification.getValue()));
            loader.setAddition(loader.getAddition().replace(modification.getKey(), modification.getValue()));
        }
    }

    public static ConditionFragment generateConditionFragment(TemplateLoaderType template, Map<String, String> modificationMap, int conditionFragmentType) {
        loader.loadTemplate(template);
        handlePlaceholders(modificationMap);
        return new ConditionFragment(loader.getAction(), loader.getObject(), "", loader.getAddition(), conditionFragmentType, new HashMap<>());
    }

}
