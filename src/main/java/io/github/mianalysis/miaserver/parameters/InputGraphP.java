package io.github.mianalysis.miaserver.parameters;

import java.util.LinkedHashSet;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.abstrakt.ChoiceType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class InputGraphP extends ChoiceType {
    public InputGraphP(String name, Module module) {
        super(name,module);
    }

    public InputGraphP(String name, Module module, String description) {
        super(name,module,description);
    }

    public InputGraphP(String name, Module module, @NotNull String choice, String description) {
        super(name, module, description);
        this.choice = choice;

    }

    @Override
    public String[] getChoices() {
        LinkedHashSet<OutputGraphP> objects = module.getModules().getParametersMatchingType(OutputGraphP.class, module);
        return objects.stream().map(OutputGraphP::getGraphName).distinct().toArray(String[]::new);
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        InputGraphP newParameter = new InputGraphP(name,newModule,getRawStringValue(),getDescription());

        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;

    }
}
