package io.github.mianalysis.miaserver.parameters;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.text.StringP;

public class ClickListenerP extends StringP {

    public ClickListenerP(String name, Module module) {
        super(name, module);
    }

    public ClickListenerP(String name, Module module, String description) {
        super(name, module, description);
    }
}
