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
package org.seasar.dbflute.unit.spring.web.webmock;

/**
 * @author jflute
 * @since 0.4.1 (2014/03/25 Tuesday)
 */
public class WebMock2ndUsedTest extends WebMockBaseTestCase {

    public void test_noMock_basic() throws Exception {
        doTest_existingMock();
    }

    public void test_noMock_more() throws Exception {
        doTest_existingMock(); // assert not inherit them
    }

    protected void doTest_existingMock() {
        assertNotNull(getMockRequest());
        assertNotNull(getMockResponse());
        assertNotNull(getMockSession());

        assertHasZeroElement(getMockRequest().getParameterMap().keySet());
        addMockRequestParameter("fooParam", "barParam");
        assertHasAnyElement(getMockRequest().getParameterMap().keySet());

        assertNull(getMockRequestAttribute("fooRequest"));
        setMockRequestAttribute("fooRequest", "barRequest");
        assertNotNull(getMockRequestAttribute("fooRequest"));

        assertNull(getMockSessionAttribute("fooSession"));
        setMockSessionAttribute("fooSession", "barSession");
        assertNotNull(getMockSessionAttribute("fooSession"));
    }
}
