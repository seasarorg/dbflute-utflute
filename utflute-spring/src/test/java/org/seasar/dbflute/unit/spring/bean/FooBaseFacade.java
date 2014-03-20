package org.seasar.dbflute.unit.spring.bean;

import javax.annotation.Resource;

import org.seasar.dbflute.unit.spring.dbflute.exbhv.FooBhv;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/20 Thursday)
 */
public abstract class FooBaseFacade {

    @Resource
    private FooBhv fooBhv; // super's private field

    public FooBhv superBehaviorInstance() {
        return fooBhv;
    }
}
