package io.github.mianalysis.miaserver.parameters;

import com.drew.lang.annotations.NotNull;

import io.github.mianalysis.mia.module.Module;
import io.github.mianalysis.mia.object.Workspace;
import io.github.mianalysis.mia.object.parameters.abstrakt.TextType;

public abstract class AbstractRenderableP extends TextType {
    protected String renderableName = "";

    public AbstractRenderableP(String name, Module module) {
        super(name, module);
    }

    public AbstractRenderableP(String name, Module module, @NotNull String renderableName) {
        super(name, module);
        this.renderableName = renderableName;
    }

    public AbstractRenderableP(String name, Module module, @NotNull String renderableName, String description) {
        super(name, module, description);
        this.renderableName = renderableName;
    }

    public String getRenderableName() {
        return renderableName;
    }

    public void setObjectsName(String renderableName) {
        this.renderableName = renderableName;
    }

    @Override
    public <T> T getValue(Workspace workspace) {
        return (T) renderableName;
    }

    @Override
    public <T> void setValue(T value) {
        renderableName = (String) value;
    }

    @Override
    public String getRawStringValue() {
        return renderableName;
    }

    @Override
    public void setValueFromString(String value) {
        renderableName = value;
    }

    @Override
    public boolean verify() {
        return super.verify();

    }
}
