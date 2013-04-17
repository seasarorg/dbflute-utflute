package org.seasar.dbflute.unit.core.binding;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class BoundResult {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final List<Field> _boundFieldList = new ArrayList<Field>();

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public List<Field> getBoundFieldList() {
        return _boundFieldList;
    }

    public void addBoundField(Field boundField) {
        _boundFieldList.add(boundField);
    }
}
