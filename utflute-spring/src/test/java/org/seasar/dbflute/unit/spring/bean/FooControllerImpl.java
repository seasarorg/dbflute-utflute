package org.seasar.dbflute.unit.spring.bean;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooControllerImpl implements FooController {

    protected FooFacade fooFacade;

    public FooFacade facadeInstance() {
        return fooFacade;
    }

    @Autowired
    public void setFooFacade(FooFacade fooFacade) {
        this.fooFacade = fooFacade;
    }
}
