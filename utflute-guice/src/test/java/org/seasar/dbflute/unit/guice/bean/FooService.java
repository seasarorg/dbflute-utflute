package org.seasar.dbflute.unit.guice.bean;

import javax.annotation.Resource;
import javax.transaction.TransactionManager;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooService {

    @Resource
    protected TransactionManager transactionManager; // Resource is unsupported so not injected
}
