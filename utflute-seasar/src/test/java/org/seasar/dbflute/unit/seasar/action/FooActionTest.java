package org.seasar.dbflute.unit.seasar.action;

import junit.framework.AssertionFailedError;

import org.seasar.dbflute.unit.core.thread.ThreadFireExecution;
import org.seasar.dbflute.unit.core.thread.ThreadFireOption;
import org.seasar.dbflute.unit.core.thread.ThreadFireResource;
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

    public void test_threadFire_expectSameResult_basic() throws Exception {
        threadFire(new ThreadFireExecution<String>() {
            public String execute(ThreadFireResource resource) {
                return "foo";
            }
        }, new ThreadFireOption().expectSameResult()); // expect no exception
    }

    public void test_threadFire_expectSameResult_failure() throws Exception {
        // ## Arrange ##
        try {
            // ## Act ##
            threadFire(new ThreadFireExecution<String>() {
                public String execute(ThreadFireResource resource) {
                    if (resource.getThreadId() % 2 == 0) {
                        return "foo";
                    } else {
                        return "bar";
                    }
                }
            }, new ThreadFireOption().expectSameResult());

            // ## Assert ##
            fail();
        } catch (AssertionFailedError e) {
            // OK
            log(e.getMessage());
        }
    }
}
