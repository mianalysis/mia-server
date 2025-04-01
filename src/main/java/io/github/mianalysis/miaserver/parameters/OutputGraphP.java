package io.github.mianalysis.miaserver.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class OutputGraphP extends AbstractRenderableP {

    public OutputGraphP(String name, Module module) {
        super(name, module);
    }

    public OutputGraphP(String name, Module module, @NotNull String graphName) {
        super(name, module, graphName);
    }

    public OutputGraphP(String name, Module module, @NotNull String graphName, String description) {
        super(name, module, graphName, description);
    }

    public String getGraphName() {
        return renderableName;
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        OutputGraphP newParameter = new OutputGraphP(name, newModule, renderableName, getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
