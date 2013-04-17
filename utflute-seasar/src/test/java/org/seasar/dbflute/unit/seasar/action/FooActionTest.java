package org.seasar.dbflute.unit.seasar.action;

import org.seasar.dbflute.unit.seasar.ContainerTestCase;

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
