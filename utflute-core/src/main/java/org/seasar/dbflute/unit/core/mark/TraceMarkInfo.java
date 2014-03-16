package org.seasar.dbflute.unit.core.mark;

import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 */
public class TraceMarkInfo {

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
