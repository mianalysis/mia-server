package io.github.mianalysis.miaserver.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.parameters.text.StringP;

public class ObjectSelectorP extends StringP {

    public ObjectSelectorP(String name, Module module) {
        super(name, module);
    }

    public ObjectSelectorP(String name, Module module, @NotNull String value) {
        super(name, module, value);
    }

    public ObjectSelectorP(String name, Module module, @NotNull String value, String description) {
        super(name, module, value, description);
    }
    
}
