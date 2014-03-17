package org.seasar.dbflute.unit.guice.bean;

import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.guice.dbflute.exbhv.FooBhv;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class FooLogic {

    @com.google.inject.Inject
    private FooBhv fooBhv; // private field

    @com.google.inject.Inject
    protected FooService fooHelper; // name wrong

    protected FooService fooService; // at first specify none but unsupported so injected

    // no web here
    //@Resource
    //protected HttpServletRequest request; // mocklet

    public String behaviorToString() {
        return fooBhv != null ? fooBhv.toString() : null;
    }

    public FooService getFooService() {
        return fooService;
    }

    @com.google.inject.Inject
    public void setFooService(FooService fooService) {
        this.fooService = fooService;
    }

    public TransactionManager getTransactionManager() {
        return fooBhv != null ? fooBhv.getTransactionManager() : null;
    }
}
