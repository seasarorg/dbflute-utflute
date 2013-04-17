package org.seasar.dbflute.unit.core.thread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class ThreadFireOption {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final int DEFAULT_THREAD_COUNT = 10;
    public static final int DEFAULT_REPEAT_COUNT = 5;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _threadCount = DEFAULT_THREAD_COUNT;
    protected int _repeatCount = DEFAULT_REPEAT_COUNT;
    protected final List<Object> _parameterList = new ArrayList<Object>();
    protected boolean _expectedSameResult;
    protected boolean _commitTransaction;
    protected boolean _suppressTransaction;
    protected String _expectedExceptionMessageAny;
    protected Class<? extends Exception> _expectedExceptionTypeAny;
    protected ThreadFireFinallyRunner _finallyRunner;

    // ===================================================================================
    //                                                                         Easy-to-Use
    //                                                                         ===========
    public ThreadFireOption threadCount(int threadCount) {
        _threadCount = threadCount;
        return this;
    }

    public ThreadFireOption repeatCount(int repeatCount) {
        _repeatCount = repeatCount;
        return this;
    }

    public ThreadFireOption parameter(Object... values) {
        if (values.length == 0) {
            _parameterList.clear();
            threadCount(DEFAULT_THREAD_COUNT);
            return this;
        }
        _parameterList.addAll(Arrays.asList(values));
        threadCount(values.length);
        return this;
    }

    public ThreadFireOption commitTx() {
        _commitTransaction = true;
        return this;
    }

    public ThreadFireOption suppressTx() {
        _suppressTransaction = true;
        return this;
    }

    public ThreadFireOption expectSameResult() {
        _expectedSameResult = true;
        return this;
    }

    public ThreadFireOption expectExceptionAny(Class<? extends Exception> expectedExceptionType) {
        _expectedExceptionTypeAny = expectedExceptionType;
        return this;
    }

    public ThreadFireOption expectExceptionAny(String expectedExceptionMessage) {
        _expectedExceptionMessageAny = expectedExceptionMessage;
        return this;
    }

    public boolean isCheckExpectedExceptionAny() {
        return _expectedExceptionTypeAny != null || _expectedExceptionMessageAny != null;
    }

    public boolean isMatchExpectedExceptionAny(Throwable cause) {
        if (_expectedExceptionTypeAny != null) {
            return _expectedExceptionTypeAny.isAssignableFrom(cause.getClass());
        }
        if (_expectedExceptionMessageAny != null) {
            final String msg = cause.getMessage();
            return msg != null && msg.contains(_expectedExceptionMessageAny);
        }
        return false;
    }

    public String getExpectedExpceptionAnyExp() {
        return _expectedExceptionTypeAny != null ? _expectedExceptionTypeAny.toString() : _expectedExceptionMessageAny;
    }

    public ThreadFireOption finallyRunner(ThreadFireFinallyRunner finallyRunner) {
        _finallyRunner = finallyRunner;
        return this;
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public int getThreadCount() {
        return _threadCount;
    }

    public void setRepeatCount(int repeatCount) {
        this._repeatCount = repeatCount;
    }

    public int getRepeatCount() {
        return _repeatCount;
    }

    public void setThreadCount(int threadCount) {
        this._threadCount = threadCount;
    }

    public List<Object> getParameterList() {
        return _parameterList;
    }

    public boolean isExpectedSameResult() {
        return _expectedSameResult;
    }

    public void setExpectedSameResult(boolean expectedSameResult) {
        this._expectedSameResult = expectedSameResult;
    }

    public boolean isCommitTransaction() {
        return _commitTransaction;
    }

    public void setCommitTransaction(boolean commitTransaction) {
        this._commitTransaction = commitTransaction;
    }

    public boolean isSuppressTransaction() {
        return _suppressTransaction;
    }

    public void setSuppressTransaction(boolean suppressTransaction) {
        this._suppressTransaction = suppressTransaction;
    }

    public String getExpectedExceptionMessageAny() {
        return _expectedExceptionMessageAny;
    }

    public void setExpectedExceptionMessageAny(String expectedExceptionMessageAny) {
        this._expectedExceptionMessageAny = expectedExceptionMessageAny;
    }

    public Class<? extends Exception> getExpectedExceptionTypeAny() {
        return _expectedExceptionTypeAny;
    }

    public void setExpectedExceptionTypeAny(Class<? extends Exception> expectedExceptionTypeAny) {
        this._expectedExceptionTypeAny = expectedExceptionTypeAny;
    }

    public ThreadFireFinallyRunner getFinallyRunner() {
        return _finallyRunner;
    }
}
