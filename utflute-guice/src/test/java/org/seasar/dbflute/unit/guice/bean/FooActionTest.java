/*
 * Copyright 2004-2014 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.dbflute.unit.guice.bean;

import org.seasar.dbflute.unit.guice.MockUnitTestCase;
import org.seasar.dbflute.unit.guice.dbflute.exbhv.FooBhv;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class FooActionTest extends MockUnitTestCase {

    public void test_inject_basic() throws Exception {
        // ## Arrange ##
        FooAction action = new FooAction();

        // ## Act ##
        inject(action);

        // ## Assert ##
        log(action.fooBhv);
        log(action.fooController);
        log(action.fooHelper);
        log(action.fooLogic);
        log(action.serviceToString());
        log(action.transactionManager);
        // no web here
        //log(action.request);

        // why cannot field injection?
        assertNotNull(action.fooBhv);
        assertNotNull(action.fooBhv.getTransactionManager());
        assertNull(action.fooController);
        assertNotNull(action.barController);
        assertNotNull(action.barController.facadeInstance());
        assertNotNull(action.fooHelper);
        assertNotNull(action.facadeInstance());
        assertNotNull(action.facadeInstance().myBehaviorInstance());
        assertNotNull(action.facadeInstance().superBehaviorInstance());
        assertNotNull(action.facadeInstance().fooService);
        assertNotNull(action.fooLogic);
        assertNotNull(action.fooLogic.behaviorToString());
        assertNotNull(action.fooLogic.fooHelper);
        assertNotNull(action.fooLogic.fooService);
        assertNotNull(action.fooLogic.getTransactionManager());
        // no web here
        //assertNull(action.fooLogic.request);
        assertNotNull(action.serviceToString());
        assertNull(action.serviceInstance().transactionManager);
        assertNull(action.transactionManager);
        // no web here
        //assertNotNull(action.request);
    }

    public void test_inject_mockInstance_injected() throws Exception {
        // ## Arrange ##
        FooAction action = new FooAction();
        FooBhv bhv = new FooBhv();
        FooController controller = new FooControllerImpl();
        FooLogic logic = new FooLogic();
        inject(bhv);
        inject(controller);
        inject(logic);
        registerMockInstance(bhv);
        registerMockInstance(controller);
        registerMockInstance(logic);

        // ## Act ##
        inject(action);

        // ## Assert ##
        log(action.fooBhv);
        log(action.fooController);
        log(action.transactionManager);
        assertNotNull(action.fooBhv);
        assertNotNull(action.fooBhv.getTransactionManager());
        assertNull(action.fooController);
        assertNotNull(action.barController);
        assertNotNull(action.barController.facadeInstance());
        assertNotNull(action.fooLogic);
        assertNotNull(action.fooLogic.behaviorToString());
        assertNotNull(action.fooLogic.fooHelper);
        assertNotNull(action.fooLogic.fooService);
        assertNotNull(action.fooLogic.getTransactionManager());
        assertNull(action.transactionManager);
        assertSame(bhv, action.fooBhv);
        assertSame(logic, action.fooLogic);
    }

    public void test_inject_mockInstance_plain() throws Exception {
        // ## Arrange ##
        FooAction action = new FooAction();
        FooBhv bhv = new FooBhv();
        FooController controller = new FooControllerImpl();
        FooLogic logic = new FooLogic();
        registerMockInstance(bhv);
        registerMockInstance(controller);
        registerMockInstance(logic);

        // ## Act ##
        inject(action);

        // ## Assert ##
        log(action.fooBhv);
        log(action.transactionManager);
        assertNotNull(action.fooBhv);
        assertNull(action.fooBhv.getTransactionManager());
        assertNull(action.fooController);
        assertNotNull(action.barController);
        assertNull(action.barController.facadeInstance());
        assertNotNull(action.fooLogic);
        assertNull(action.fooLogic.behaviorToString());
        assertNull(action.fooLogic.fooHelper);
        assertNull(action.fooLogic.fooService);
        assertNull(action.fooLogic.getTransactionManager());
        assertNull(action.transactionManager);
        assertSame(bhv, action.fooBhv);
        assertSame(logic, action.fooLogic);
    }
}
