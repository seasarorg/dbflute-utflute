package org.seasar.dbflute.unit.spring.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooFacade {

    protected PlatformTransactionManager transactionManager;

    protected FooService fooService; // annotation for setter

    public FooService getFooService() {
        return fooService;
    }

    @Autowired
    public void setFooService(FooService fooService) {
        this.fooService = fooService;
    }
}
