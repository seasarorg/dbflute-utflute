package org.seasar.dbflute.unit.spring;

import org.seasar.dbflute.unit.core.transaction.TransactionFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public class SpringTransactionResource implements TransactionResource {

    protected PlatformTransactionManager _transactionManager;
    protected TransactionStatus _transactionStatus;

    public void commit() {
        try {
            _transactionManager.commit(_transactionStatus);
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to commit the transaction.", e);
        }
    }

    public void rollback() {
        try {
            _transactionManager.rollback(_transactionStatus);
        } catch (RuntimeException e) {
            throw new TransactionFailureException("Failed to roll-back the transaction.", e);
        }
    }

    public PlatformTransactionManager getTransactionManager() {
        return _transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this._transactionManager = transactionManager;
    }

    public TransactionStatus getTransactionStatus() {
        return _transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this._transactionStatus = transactionStatus;
    }
}
