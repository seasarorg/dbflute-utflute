package org.seasar.dbflute.unit.seasar.action;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.seasar.dbflute.exbhv.FooBhv;
import org.seasar.framework.container.annotation.tiger.Binding;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class FooAction {

    @Binding
    protected FooBhv fooBhv;

    @Binding
    protected TransactionManager transactionManager;
}
