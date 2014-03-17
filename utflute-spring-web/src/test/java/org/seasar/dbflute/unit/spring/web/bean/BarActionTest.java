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
package org.seasar.dbflute.unit.spring.web.bean;

import org.seasar.dbflute.unit.spring.web.WebContainerTestCase;

/**
 * @author jflute
 * @since 0.4.0 (2014/03/16 Sunday)
 */
public class BarActionTest extends WebContainerTestCase {

    public void test_inject_request() throws Exception {
        // ## Arrange ##
        BarAction action = new BarAction();

        // ## Act ##
        inject(action);

        // ## Assert ##
        log(action.barBhv);
        log(action.barLogic);
        log(action.transactionManager);
        log(action.request);
        assertNotNull(action.barBhv);
        assertNotNull(action.barLogic);
        assertNull(action.barLogic.request);
        assertNotNull(action.transactionManager);
        assertNotNull(action.request);
    }
}
