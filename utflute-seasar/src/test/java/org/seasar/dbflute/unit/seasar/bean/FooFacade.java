package org.seasar.dbflute.unit.seasar.bean;

import javax.annotation.Resource;
import javax.transaction.TransactionManager;

import org.seasar.framework.container.annotation.tiger.Binding;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooFacade {

    @Resource
    protected TransactionManager transactionManager;

    protected FooService fooService;

    public FooService getFooService() {
        return fooService;
    }

    @Binding
    public void setFooService(FooService fooService) {
        this.fooService = fooService;
    }
}
