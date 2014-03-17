package org.seasar.dbflute.unit.guice.bean;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooControllerImpl implements FooController {

    @com.google.inject.Inject
    protected FooFacade fooFacade;

    public FooFacade facadeInstance() {
        return fooFacade;
    }
}
