package org.seasar.dbflute.unit.core.thread;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class ThreadFireFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ThreadFireFailureException(String msg, Throwable e) {
        super(msg, e);
    }
}
