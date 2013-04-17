package org.seasar.dbflute.unit.spring.action;

import org.seasar.dbflute.unit.spring.ContainerTestCase;

public class FooActionTest extends ContainerTestCase {

    public void test_inject_basic() throws Exception {
        // ## Arrange ##
        FooAction action = new FooAction();

        // ## Act ##
        inject(action);

        // ## Assert ##
        log(action.fooBhv);
        log(action.transactionManager);
        assertNotNull(action.fooBhv);
        assertNotNull(action.transactionManager);
    }
}
