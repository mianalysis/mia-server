package io.github.mianalysis.miaserver.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;
import io.github.mianalysis.mia.object.parameters.objects.OutputObjectsP;

import com.drew.lang.annotations.NotNull;

public class ParameterP extends ChoiceType {
    private String selectedModuleID = "";

    public ParameterP(String name, Module module) {
        super(name, module);
    }

    public ParameterP(String name, Module module, String description) {
        super(name, module, description);
    }

    public ParameterP(String name, Module module, @NotNull String choice, @NotNull String selectedModuleID) {
        super(name, module);
        this.selectedModuleID = selectedModuleID;
        this.choice = choice;

    }

    public ParameterP(String name, Module module, @NotNull String choice, @NotNull String selectedModuleID,
            String description) {
        super(name, module, description);
        this.selectedModuleID = selectedModuleID;
        this.choice = choice;

    }

    public String getSelectedModuleID() {
        return selectedModuleID;
    }

    public void setSelectedModuleID(String moduleID) {
        this.selectedModuleID = moduleID;
    }

    @Override
    public String[] getChoices() {
        Module selectedModule = getModule().getModules().getModuleByID(selectedModuleID);
        if (selectedModule != null) { 
            return selectedModule.updateAndGetParameters().keySet().stream().toArray(String[]::new);
        } else
            return new String[0];

    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ParameterP newParameter = new ParameterP(name, newModule, choice, selectedModuleID, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
