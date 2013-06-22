/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
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
