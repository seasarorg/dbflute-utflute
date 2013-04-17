package org.seasar.dbflute.unit.core.transaction;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public interface TransactionResource {

    /**
     * Commit the transaction.
     * @throws TransactionFailureException When the transaction failed.
     */
    void commit();

    /**
     * Roll-back the transaction.
     * @throws TransactionFailureException When the transaction failed.
     */
    void rollback();
}
