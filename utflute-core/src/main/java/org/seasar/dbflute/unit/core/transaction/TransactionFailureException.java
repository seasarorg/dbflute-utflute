package org.seasar.dbflute.unit.core.transaction;

/**
 * @author jflute
 * @since 0.1.7 (2012/08/30 Thursday)
 */
public class TransactionFailureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TransactionFailureException(String msg, Throwable e) {
        super(msg, e);
    }
}
