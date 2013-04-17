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
}
