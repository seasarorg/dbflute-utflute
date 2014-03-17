package org.seasar.dbflute.unit.guice.bean;

import javax.transaction.TransactionManager;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooFacade {

    protected TransactionManager transactionManager;

    protected FooService fooService; // annotation for setter

    public FooService getFooService() {
        return fooService;
    }

    @com.google.inject.Inject
    public void setFooService(FooService fooService) {
        this.fooService = fooService;
    }
}
