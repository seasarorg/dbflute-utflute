package org.seasar.dbflute.unit.core.binding;

import java.lang.annotation.Annotation;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public interface NonBindingDeterminer {

    /**
     * Should be the annotation treated as non-binding?
     * @param bindingAnno The annotation instance for binding to determine. (NotNull)
     * @return The determination, true or false.
     */
    boolean isNonBinding(Annotation bindingAnno);
}
