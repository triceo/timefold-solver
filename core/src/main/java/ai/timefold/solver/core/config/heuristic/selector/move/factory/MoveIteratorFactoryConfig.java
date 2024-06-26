package ai.timefold.solver.core.config.heuristic.selector.move.factory;

import java.util.Map;
import java.util.function.Consumer;

import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import ai.timefold.solver.core.config.heuristic.selector.move.MoveSelectorConfig;
import ai.timefold.solver.core.config.util.ConfigUtils;
import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import ai.timefold.solver.core.impl.io.jaxb.adapter.JaxbCustomPropertiesAdapter;

@XmlType(propOrder = {
        "moveIteratorFactoryClass",
        "moveIteratorFactoryCustomProperties"
})
public class MoveIteratorFactoryConfig extends MoveSelectorConfig<MoveIteratorFactoryConfig> {

    public static final String XML_ELEMENT_NAME = "moveIteratorFactory";

    protected Class<? extends MoveIteratorFactory> moveIteratorFactoryClass = null;

    @XmlJavaTypeAdapter(JaxbCustomPropertiesAdapter.class)
    protected Map<String, String> moveIteratorFactoryCustomProperties = null;

    public Class<? extends MoveIteratorFactory> getMoveIteratorFactoryClass() {
        return moveIteratorFactoryClass;
    }

    public void setMoveIteratorFactoryClass(Class<? extends MoveIteratorFactory> moveIteratorFactoryClass) {
        this.moveIteratorFactoryClass = moveIteratorFactoryClass;
    }

    public Map<String, String> getMoveIteratorFactoryCustomProperties() {
        return moveIteratorFactoryCustomProperties;
    }

    public void setMoveIteratorFactoryCustomProperties(Map<String, String> moveIteratorFactoryCustomProperties) {
        this.moveIteratorFactoryCustomProperties = moveIteratorFactoryCustomProperties;
    }

    // ************************************************************************
    // With methods
    // ************************************************************************

    public MoveIteratorFactoryConfig
            withMoveIteratorFactoryClass(Class<? extends MoveIteratorFactory> moveIteratorFactoryClass) {
        this.setMoveIteratorFactoryClass(moveIteratorFactoryClass);
        return this;
    }

    public MoveIteratorFactoryConfig
            withMoveIteratorFactoryCustomProperties(Map<String, String> moveIteratorFactoryCustomProperties) {
        this.setMoveIteratorFactoryCustomProperties(moveIteratorFactoryCustomProperties);
        return this;
    }

    @Override
    public MoveIteratorFactoryConfig inherit(MoveIteratorFactoryConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        moveIteratorFactoryClass = ConfigUtils.inheritOverwritableProperty(
                moveIteratorFactoryClass, inheritedConfig.getMoveIteratorFactoryClass());
        moveIteratorFactoryCustomProperties = ConfigUtils.inheritMergeableMapProperty(
                moveIteratorFactoryCustomProperties, inheritedConfig.getMoveIteratorFactoryCustomProperties());
        return this;
    }

    @Override
    public MoveIteratorFactoryConfig copyConfig() {
        return new MoveIteratorFactoryConfig().inherit(this);
    }

    @Override
    public void visitReferencedClasses(Consumer<Class<?>> classVisitor) {
        visitCommonReferencedClasses(classVisitor);
        classVisitor.accept(moveIteratorFactoryClass);
    }

    @Override
    public boolean hasNearbySelectionConfig() {
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + moveIteratorFactoryClass + ")";
    }

}
