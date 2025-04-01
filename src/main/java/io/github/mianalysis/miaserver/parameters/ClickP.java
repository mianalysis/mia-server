package io.github.mianalysis.miaserver.parameters;

import org.apache.poi.ss.formula.functions.T;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.module.system.GlobalVariables;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.BooleanType;
import io.github.mianalysis.mia.object.parameters.abstrakt.Parameter;

public class ClickP extends BooleanType {
    public ClickP(String name, Module module, boolean enabled) {
        super(name, module, enabled);
    }

    public ClickP(String name, Module module, boolean enabled, String description) {
        super(name, module, enabled, description);
    }

    public boolean getAndSetFalse(Workspace workspace) {
        String converted = GlobalVariables.convertString(value, module.getModules());
        if ((Boolean) Boolean.parseBoolean(converted)) {
            setValue(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public <T extends Parameter> T duplicate(Module newModule) {
        ClickP newParameter = new ClickP(name,newModule,getValue(null),getDescription());
        newParameter.setNickname(getNickname());
        newParameter.setVisible(isVisible());
        newParameter.setExported(isExported());

        return (T) newParameter;
        
    }
}
