package org.seasar.dbflute.unit.seasar.bean;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.TransactionManager;

import org.seasar.dbflute.unit.seasar.dbflute.exbhv.FooBhv;
import org.seasar.framework.container.annotation.tiger.Binding;
import org.seasar.framework.container.annotation.tiger.BindingType;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class FooLogic {

    @Resource
    private FooBhv fooBhv; // private field

    @Resource(name = "fooService")
    protected FooService fooHelper; // name wrong but component name is specified

    protected FooService fooService; // annotation for setter but none

    @Resource
    protected HttpServletRequest request; // mocklet (can be injected if prototype)

    public String behaviorToString() {
        return fooBhv != null ? fooBhv.toString() : null;
    }

    public FooService getFooService() {
        return fooService;
    }

    @Binding(bindingType = BindingType.NONE)
    public void setFooService(FooService fooService) {
        this.fooService = fooService;
    }

    public TransactionManager getTransactionManager() {
        return fooBhv != null ? fooBhv.getTransactionManager() : null;
    }
}
