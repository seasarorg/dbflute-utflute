package org.seasar.dbflute.unit.spring.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooService {

    @Autowired
    protected PlatformTransactionManager transactionManager;
}
