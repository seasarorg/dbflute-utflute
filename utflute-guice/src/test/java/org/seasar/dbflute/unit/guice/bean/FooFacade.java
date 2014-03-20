package org.seasar.dbflute.unit.guice.bean;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.guice.dbflute.exbhv.FooBhv;

import com.google.inject.Inject;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooFacade extends FooBaseFacade {

    @Inject
    private FooBhv fooBhv; // same name as super's

    protected TransactionManager transactionManager; // no annotation, no setter

    protected FooService fooService; // annotation for protected setter

    public FooBhv myBehaviorInstance() {
        return fooBhv;
    }

    public FooService getFooService() {
        return fooService;
    }

    @Inject
    protected void setFooService(FooService fooService) {
        this.fooService = fooService;
    }
}
