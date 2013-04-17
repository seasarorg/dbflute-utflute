package org.seasar.dbflute.unit.spring.action;

import org.seasar.dbflute.unit.spring.dbflute.exbhv.FooBhv;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author jflute
 * @since 0.1.1 (2011/07/25 Monday)
 */
public class FooAction {

    protected FooBhv fooBhv;

    protected PlatformTransactionManager transactionManager;
}
