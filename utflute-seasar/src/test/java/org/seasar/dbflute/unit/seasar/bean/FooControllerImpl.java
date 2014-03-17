package org.seasar.dbflute.unit.seasar.bean;

import javax.annotation.Resource;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooControllerImpl implements FooController {

    @Resource
    protected FooFacade fooFacade;

    public FooFacade facadeInstance() {
        return fooFacade;
    }
}
