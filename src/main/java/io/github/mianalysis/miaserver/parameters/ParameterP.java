package io.github.mianalysis.miaserver.parameters;

import java.util.LinkedHashMap;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.ParameterGroup;
import io.github.mianalysis.mia.object.parameters.Parameters;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

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

    public Parameter getSelectedParameter() {
        Module selectedModule = getModule().getModules().getModuleByID(selectedModuleID);
        if (selectedModule != null) {
            Parameters params = selectedModule.updateAndGetParameters();

            // If the parameter doesn't contain dividers, we can easily return it
            if (!choice.contains(" // "))
                return selectedModule.getParameter(choice);

            for (Parameter param : params.values()) {
                if (param instanceof ParameterGroup) {
                    LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) param).getCollections(true);
                    for (int idx : collections.keySet())
                        for (Parameter param2 : collections.get(idx).values())
                            if ((param.getName() + " // " + param2.getName() + " // " + idx).equals(choice))
                                return param2;
                }
            }

            return null;

        } else {
            return null;

        }
    }

    public Parameter getParentGroup() {
        Module selectedModule = getModule().getModules().getModuleByID(selectedModuleID);
        if (selectedModule != null) {
            Parameters params = selectedModule.updateAndGetParameters();

            // If the parameter doesn't contain dividers, we can easily return it
            if (!choice.contains(" // "))
                return null;

            for (Parameter param : params.values()) {
                if (param instanceof ParameterGroup) {
                    LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) param).getCollections(true);
                    for (int idx : collections.keySet())
                        for (Parameter param2 : collections.get(idx).values())
                            if ((param.getName() + " // " + param2.getName() + " // " + idx).equals(choice))
                                return param;
                }
            }

            return null;

        } else {
            return null;

        }
    }

    public Integer getGroupCollectionNumber() {
        Module selectedModule = getModule().getModules().getModuleByID(selectedModuleID);
        if (selectedModule != null) {
            Parameters params = selectedModule.updateAndGetParameters();

            // If the parameter doesn't contain dividers, we can easily return it
            if (!choice.contains(" // "))
                return null;

            for (Parameter param : params.values()) {
                if (param instanceof ParameterGroup) {
                    LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) param).getCollections(true);
                    for (int idx : collections.keySet())
                        for (Parameter param2 : collections.get(idx).values())
                            if ((param.getName() + " // " + param2.getName() + " // " + idx).equals(choice))
                                return new Integer(idx);
                }
            }

            return null;

        } else {
            return null;

        }
    }

    @Override
    public String[] getChoices() {
        Module selectedModule = getModule().getModules().getModuleByID(selectedModuleID);
        if (selectedModule != null) {
            Parameters params = selectedModule.updateAndGetParameters();

            // Determining number of parameters
            int count = 0;
            for (Parameter param : params.values()) {
                if (param instanceof ParameterGroup) {
                    for (Parameters params2 : ((ParameterGroup) param).getCollections(true).values())
                        for (Parameter param2 : params2.values())
                            count++;
                } else {
                    count++;
                }
            }

            // Compiling list of parameters
            String[] choices = new String[count];
            count = 0;
            for (Parameter param : params.values()) {
                if (param instanceof ParameterGroup) {
                    LinkedHashMap<Integer, Parameters> collections = ((ParameterGroup) param).getCollections(true);
                    for (int idx : collections.keySet())
                        for (Parameter param2 : collections.get(idx).values())
                            choices[count++] = param.getName() + " // " + param2.getName() + " // " + idx;
                } else {
                    choices[count++] = param.getName();
                }
            }

            return choices;

        } else {
            return new String[0];

        }
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
