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
package org.seasar.dbflute.unit.core;

import junit.framework.AssertionFailedError;

/**
 * @author jflute
 */
public class PlainTestCaseTest extends PlainTestCase {

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    public void test_assertContains() throws Exception {
        assertContainsKeyword(newArrayList("foo", "bar", "qux"), "ar");
        try {
            assertContainsKeyword(newArrayList("foo", "bar", "qux"), "co");
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        assertContainsKeywordAll(newArrayList("foo", "bar", "qux"), "ar", "ux");
        try {
            assertContainsKeywordAll(newArrayList("foo", "bar", "qux"), "ar", "no");
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        assertContainsKeywordAny(newArrayList("foo", "bar", "qux"), "ar", "ux");
        assertContainsKeywordAny(newArrayList("foo", "bar", "qux"), "ar", "no");
        try {
            assertContainsKeywordAny(newArrayList("foo", "bar", "qux"), "no1", "no2");
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
    }

    public void test_assertHas() throws Exception {
        assertHasAnyElement(newArrayList("foo"));
        assertHasAnyElement(newArrayList("foo", "bar"));
        try {
            assertHasAnyElement(newArrayList());
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        assertHasOnlyOneElement(newArrayList("foo"));
        try {
            assertHasOnlyOneElement(newArrayList("foo", "bar"));
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        try {
            assertHasOnlyOneElement(newArrayList());
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        assertHasPluralElement(newArrayList("foo", "bar"));
        try {
            assertHasPluralElement(newArrayList("foo"));
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        try {
            assertHasPluralElement(newArrayList());
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        assertHasZeroElement(newArrayList());
        try {
            assertHasZeroElement(newArrayList("foo"));
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
        try {
            assertHasZeroElement(newArrayList("foo", "bar"));
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                      Logging Helper
    //                                                                      ==============
    public void test_log() throws Exception {
        // check your eyes
        log("foo");
        log("foo", "bar");
        log("foo", "bar", "qux");
        log("foo", "bar", "qux", new RuntimeException("corge"));
        log("foo", currentDate(), currentTimestamp());
    }

    // ===================================================================================
    //                                                                         Cannon-ball
    //                                                                         ===========
    // at CannonballTest
}
