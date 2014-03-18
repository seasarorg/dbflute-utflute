/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
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
package org.seasar.dbflute.unit.core.markhere;

import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class MarkHereInfo {

    protected String _mark;
    protected int _count;
    protected boolean _asserted;

    @Override
    public String toString() {
        final String classTitle = DfTypeUtil.toClassTitle(this);
        return classTitle + ":{" + _mark + ", " + _count + ", " + _asserted + "}";
    }

    public String getMark() {
        return _mark;
    }

    public void setMark(String mark) {
        this._mark = mark;
    }

    public int getCount() {
        return _count;
    }

    public void incrementCount() {
        _count++;
    }

    public boolean isAsserted() {
        return _asserted;
    }

    public void finishAssertion() {
        _asserted = true;
    }
}
