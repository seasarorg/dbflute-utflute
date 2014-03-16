package org.seasar.dbflute.unit.seasar.dbflute.exbhv;

import javax.transaction.TransactionManager;

import org.seasar.framework.container.annotation.tiger.Binding;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class FooBhv {

    @Binding
    protected TransactionManager transactionManager;

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }
}
