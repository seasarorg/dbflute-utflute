package org.seasar.dbflute.unit.core.binding;

/**
 * @author jflute
 * @since 0.1.2 (2011/09/16 Friday)
 */
public interface ComponentProvider {

    <COMPONENT> COMPONENT provideComponent(Class<COMPONENT> type);

    <COMPONENT> COMPONENT provideComponent(String name);

    boolean existsComponent(Class<?> type);

    boolean existsComponent(String name);
}
