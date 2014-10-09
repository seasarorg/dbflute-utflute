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

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seasar.dbflute.AccessContext;
import org.seasar.dbflute.cbean.PagingResultBean;
import org.seasar.dbflute.unit.core.cannonball.CannonballDirector;
import org.seasar.dbflute.unit.core.cannonball.CannonballOption;
import org.seasar.dbflute.unit.core.cannonball.CannonballRun;
import org.seasar.dbflute.unit.core.cannonball.CannonballStaff;
import org.seasar.dbflute.unit.core.filesystem.FileLineHandler;
import org.seasar.dbflute.unit.core.filesystem.FilesystemPlayer;
import org.seasar.dbflute.unit.core.markhere.MarkHereManager;
import org.seasar.dbflute.unit.core.policestory.PoliceStory;
import org.seasar.dbflute.unit.core.policestory.javaclass.PoliceStoryJavaClassHandler;
import org.seasar.dbflute.unit.core.policestory.jspfile.PoliceStoryJspFileHandler;
import org.seasar.dbflute.unit.core.policestory.miscfile.PoliceStoryMiscFileHandler;
import org.seasar.dbflute.unit.core.policestory.pjresource.PoliceStoryProjectResourceHandler;
import org.seasar.dbflute.unit.core.policestory.webresource.PoliceStoryWebResourceHandler;
import org.seasar.dbflute.unit.core.thread.ThreadFireExecution;
import org.seasar.dbflute.unit.core.thread.ThreadFireHelper;
import org.seasar.dbflute.unit.core.thread.ThreadFireMan;
import org.seasar.dbflute.unit.core.thread.ThreadFireOption;
import org.seasar.dbflute.unit.core.transaction.TransactionPerformFailureException;
import org.seasar.dbflute.unit.core.transaction.TransactionPerformer;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfResourceUtil;
import org.seasar.dbflute.util.DfTypeUtil;
import org.seasar.dbflute.util.Srl;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public abstract class PlainTestCase extends TestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for sub class. (NotNull) */
    protected final Log _xlogger = LogFactory.getLog(getClass());
    // UTFlute wants to use logger for caller output
    // but should remove the dependency to Log4j
    // (logging through commons-logging gives us fixed caller...)
    //protected final Logger _xlogger = Logger.getLogger(getClass());

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    /** The manager of mark here. (NullAllowed: lazy-loaded) */
    protected MarkHereManager _xmarkHereManager;

    /** The reserved title for logging test case beginning. (NullAllowed: before preparation or already showed) */
    protected String _xreservedTitle;

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void setUp() throws Exception {
        xreserveShowTitle();
        xprepareAccessContext();
        super.setUp();
    }

    protected void xreserveShowTitle() {
        // lazy-logging (no logging test case, no title)
        _xreservedTitle = "<<< " + xgetCaseDisp() + " >>>";
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        xclearAccessContext();
        xclearMark();
    }

    protected void xprepareAccessContext() {
        final AccessContext context = new AccessContext();
        context.setAccessTimestamp(currentTimestamp());
        context.setAccessDate(currentDate());
        context.setAccessUser(Thread.currentThread().getName());
        context.setAccessProcess(getClass().getSimpleName());
        context.setAccessModule(getClass().getSimpleName());
        AccessContext.setAccessContextOnThread(context);
    }

    /**
     * Get the access context for common column auto setup of DBFlute.
     * @return The instance of access context on the thread. (basically NotNull)
     */
    protected AccessContext getAccessContext() { // user method
        return AccessContext.getAccessContextOnThread();
    }

    protected void xclearAccessContext() {
        AccessContext.clearAccessContextOnThread();
    }

    protected void xclearMark() {
        if (xhasMarkHereManager()) {
            xgetMarkHereManager().checkNonAssertedMark();
            xgetMarkHereManager().clearMarkMap();
            xdestroyMarkHereManager();
        }
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    // -----------------------------------------------------
    //                                                Equals
    //                                                ------
    // to avoid setting like this:
    //  assertEquals(Integer.valueOf(3), member.getMemberId())
    protected void assertEquals(String message, int expected, Integer actual) {
        assertEquals(message, Integer.valueOf(expected), actual);
    }

    protected void assertEquals(int expected, Integer actual) {
        assertEquals(null, Integer.valueOf(expected), actual);
    }

    // -----------------------------------------------------
    //                                            True/False
    //                                            ----------
    protected void assertTrueAll(boolean... conditions) {
        int index = 0;
        for (boolean condition : conditions) {
            assertTrue("conditions[" + index + "]" + " expected: <true> but was: " + condition, condition);
            ++index;
        }
    }

    protected void assertTrueAny(boolean... conditions) {
        boolean hasTrue = false;
        for (boolean condition : conditions) {
            if (condition) {
                hasTrue = true;
                break;
            }
        }
        assertTrue("all conditions were false", hasTrue);
    }

    protected void assertFalseAll(boolean... conditions) {
        int index = 0;
        for (boolean condition : conditions) {
            assertFalse("conditions[" + index + "]" + " expected: <false> but was: " + condition, condition);
            ++index;
        }
    }

    protected void assertFalseAny(boolean... conditions) {
        boolean hasFalse = false;
        for (boolean condition : conditions) {
            if (!condition) {
                hasFalse = true;
                break;
            }
        }
        assertTrue("all conditions were true", hasFalse);
    }

    // -----------------------------------------------------
    //                                                String
    //                                                ------
    /**
     * Assert that the string contains the keyword.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContains(String str, String keyword) {
        if (!Srl.contains(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the string contains the keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContainsIgnoreCase(String str, String keyword) {
        if (!Srl.containsIgnoreCase(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the string contains all keywords.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fx", "oo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAll(String str, String... keywords) {
        if (!Srl.containsAll(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains all keywords. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "fx", "oo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAllIgnoreCase(String str, String... keywords) {
        if (!Srl.containsAllIgnoreCase(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains any keyword.
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "F", "qux"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fx", "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAny(String str, String... keywords) {
        if (!Srl.containsAny(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string contains any keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "fo", "oo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "f", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "F", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "fx", "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsAnyIgnoreCase(String str, String... keywords) {
        if (!Srl.containsAnyIgnoreCase(str, keywords)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the string does not contains the keyword.
     * <pre>
     * String str = "foo";
     * assertNotContains(str, "ux"); <span style="color: #3F7E5E">// true</span>
     * assertNotContains(str, "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertNotContains(str, "fo"); <span style="color: #3F7E5E">// false</span>
     * assertNotContains(str, "oo"); <span style="color: #3F7E5E">// false</span>
     * assertNotContains(str, "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertNotContains(String str, String keyword) {
        if (Srl.contains(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should not have the keyword but found: " + keyword);
        }
    }

    /**
     * Assert that the string does not contains the keyword. (ignore case)
     * <pre>
     * String str = "foo";
     * assertContains(str, "ux"); <span style="color: #3F7E5E">// true</span>
     * assertContains(str, "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "fo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "oo"); <span style="color: #3F7E5E">// false</span>
     * assertContains(str, "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param str The string to assert. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertNotContainsIgnoreCase(String str, String keyword) {
        if (Srl.containsIgnoreCase(str, keyword)) {
            log("Asserted string: " + str); // might be large so show at log
            fail("the string should not have the keyword but found: " + keyword);
        }
    }

    // -----------------------------------------------------
    //                                                  List
    //                                                  ----
    /**
     * Assert that the list has an element containing the keyword.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "ar"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContainsKeyword(strList, "ux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContainsKeyword(Collection<String> strList, String keyword) {
        if (!Srl.containsKeyword(strList, keyword)) {
            fail("the list should have the keyword but not found: " + keyword);
        }
    }

    /**
     * Assert that the list has an element containing all keywords.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ar", "Foo"); <span style="color: #3F7E5E">// false</span>
     * assertContainsKeyword(strList, "fo", "ux", "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAll(Collection<String> strList, String... keywords) {
        if (!Srl.containsKeywordAll(strList, keywords)) {
            fail("the list should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing all keywords. (ignore case)
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fO", "ar", "Foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "foo"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAllIgnoreCase(Collection<String> strList, String... keywords) {
        if (!Srl.containsKeywordAllIgnoreCase(strList, keywords)) {
            fail("the list should have all keywords (case ignored) but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing any keyword.
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Fo", "ux", "qux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAny(Collection<String> strList, String... keywords) {
        if (!Srl.containsKeywordAny(strList, keywords)) {
            fail("the list should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has an element containing any keyword. (ignore case)
     * <pre>
     * List&lt;String&gt; strList = ...; <span style="color: #3F7E5E">// [foo, bar]</span>
     * assertContainsKeyword(strList, "fo", "ar", "foo"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "Fo", "ux", "qux"); <span style="color: #3F7E5E">// true</span>
     * assertContainsKeyword(strList, "po", "ux", "qux"); <span style="color: #3F7E5E">// false</span>
     * </pre>
     * @param strList The list of string. (NotNull)
     * @param keywords The array of keyword string. (NotNull) 
     */
    protected void assertContainsKeywordAnyIgnoreCase(Collection<String> strList, String... keywords) {
        if (!Srl.containsKeywordAnyIgnoreCase(strList, keywords)) {
            fail("the list should have any keyword (case ignored) but not found: " + newArrayList(keywords));
        }
    }

    /**
     * Assert that the list has any element (not empty). <br />
     * You can use this to guarantee assertion in loop like this:
     * <pre>
     * List&lt;Member&gt; memberList = memberBhv.selectList(cb);
     * <span style="color: #FD4747">assertHasAnyElement(memberList);</span>
     * for (Member member : memberList) {
     *     assertTrue(member.getMemberName().startsWith("S"));
     * }
     * </pre>
     * @param notEmptyList The list expected not empty. (NotNull)
     */
    protected void assertHasAnyElement(Collection<?> notEmptyList) {
        if (notEmptyList.isEmpty()) {
            fail("the list should have any element (not empty) but empty.");
        }
    }

    protected void assertHasOnlyOneElement(Collection<?> lonelyList) {
        if (lonelyList.size() != 1) {
            fail("the list should have the only one element but: " + lonelyList);
        }
    }

    protected void assertHasPluralElement(Collection<?> crowdedList) {
        if (crowdedList.size() < 2) {
            fail("the list should have plural elements but: " + crowdedList);
        }
    }

    protected void assertHasZeroElement(Collection<?> emptyList) {
        if (!emptyList.isEmpty()) {
            fail("the list should have zero element (empty) but: " + emptyList);
        }
    }

    /**
     * @param list
     * @deprecated use {@link #assertHasAnyElement(Collection)}
     */
    protected void assertListNotEmpty(List<?> list) { // old style
        if (list.isEmpty()) {
            fail("the list should NOT be empty but empty.");
        }
    }

    // -----------------------------------------------------
    //                                             Mark Here
    //                                             ---------
    /**
     * Mark here to assert that it goes through the road.
     * <pre>
     * final String mark = "cursor";
     * MemberCB cb = new MemberCB();
     * memberBhv.selectCursor(cb, entity -&gt; {
     *     <span style="color: #FD4747">markHere</span>(mark);
     * });
     * assertMarked(mark); <span style="color: #3F7E5E">// the callback called</span>
     * </pre>
     * @param mark The your original mark expression as string. (NotNull)
     */
    protected void markHere(String mark) {
        assertNotNull(mark);
        xgetMarkHereManager().mark(mark);
    }

    /**
     * Assert the mark is marked. (found in existing marks)
     * <pre>
     * final String mark = "cursor";
     * MemberCB cb = new MemberCB();
     * memberBhv.selectCursor(cb, entity -&gt; {
     *     markHere(mark);
     * });
     * <span style="color: #FD4747">assertMarked</span>(mark); <span style="color: #3F7E5E">// the callback called</span>
     * </pre>
     * @param mark The your original mark expression as string. (NotNull)
     */
    protected void assertMarked(String mark) {
        assertNotNull(mark);
        xgetMarkHereManager().assertMarked(mark);
    }

    /**
     * Is the mark marked? (found the mark in existing marks?)
     * @param mark The your original mark expression as string. (NotNull)
     * @return The determination, true or false.
     */
    protected boolean isMarked(String mark) {
        assertNotNull(mark);
        return xgetMarkHereManager().isMarked(mark);
    }

    protected MarkHereManager xgetMarkHereManager() {
        if (_xmarkHereManager == null) {
            _xmarkHereManager = new MarkHereManager();
        }
        return _xmarkHereManager;
    }

    protected boolean xhasMarkHereManager() {
        return _xmarkHereManager != null;
    }

    protected void xdestroyMarkHereManager() {
        _xmarkHereManager = null;
    }

    // ===================================================================================
    //                                                                      Logging Helper
    //                                                                      ==============
    /**
     * Log the messages. <br />
     * If you set an exception object to the last element, it shows stack traces.
     * <pre>
     * Member member = ...;
     * <span style="color: #FD4747">log</span>(member.getMemberName(), member.getBirthdate());
     * <span style="color: #3F7E5E">// -&gt; Stojkovic, 1965/03/03</span>
     * 
     * Exception e = ...;
     * <span style="color: #FD4747">log</span>(member.getMemberName(), member.getBirthdate(), e);
     * <span style="color: #3F7E5E">// -&gt; Stojkovic, 1965/03/03</span>
     * <span style="color: #3F7E5E">//  (and stack traces)</span>
     * </pre>
     * @param msgs The array of messages. (NotNull)
     */
    protected void log(Object... msgs) {
        if (msgs == null) {
            throw new IllegalArgumentException("The argument 'msgs' should not be null.");
        }
        Throwable cause = null;
        final int arrayLength = msgs.length;
        if (arrayLength > 0) {
            final Object lastElement = msgs[arrayLength - 1];
            if (lastElement instanceof Throwable) {
                cause = (Throwable) lastElement;
            }
        }
        final StringBuilder sb = new StringBuilder();
        int index = 0;
        for (Object msg : msgs) {
            if (index == arrayLength - 1 && cause != null) { // last loop and it is cause
                break;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            final String appended;
            if (msg instanceof Timestamp) {
                appended = toString(msg, "yyyy/MM/dd HH:mm:ss.SSS");
            } else if (msg instanceof Date) {
                appended = toString(msg, "yyyy/MM/dd");
            } else {
                appended = msg != null ? msg.toString() : null;
            }
            sb.append(appended);
            ++index;
        }
        final String msg = sb.toString();
        if (_xreservedTitle != null) {
            _xlogger.debug("");
            _xlogger.debug(_xreservedTitle);
            _xreservedTitle = null;
        }
        if (cause != null) {
            _xlogger.debug(msg, cause);
        } else {
            _xlogger.debug(msg);
        }
        // see comment for logger definition for the detail
        //_xlogger.log(PlainTestCase.class.getName(), Level.DEBUG, msg, cause);
    }

    // ===================================================================================
    //                                                                         Show Helper
    //                                                                         ===========
    protected void showPage(PagingResultBean<?>... pages) {
        int count = 1;
        for (PagingResultBean<? extends Object> page : pages) {
            log("[page" + count + "]");
            for (Object entity : page) {
                log("  " + entity);
            }
            ++count;
        }
    }

    protected void showList(List<?>... list) {
        int count = 1;
        for (List<? extends Object> ls : list) {
            log("[list" + count + "]");
            for (Object entity : ls) {
                log("  " + entity);
            }
            ++count;
        }
    }

    // ===================================================================================
    //                                                                       String Helper
    //                                                                       =============
    protected String replace(String str, String fromStr, String toStr) {
        return Srl.replace(str, fromStr, toStr);
    }

    protected List<String> splitList(String str, String delimiter) {
        return Srl.splitList(str, delimiter);
    }

    protected List<String> splitListTrimmed(String str, String delimiter) {
        return Srl.splitListTrimmed(str, delimiter);
    }

    protected String toString(Object obj) {
        return DfTypeUtil.toString(obj);
    }

    protected String toString(Object obj, String pattern) {
        return DfTypeUtil.toString(obj, pattern);
    }

    // ===================================================================================
    //                                                                       Number Helper
    //                                                                       =============
    protected Integer toInteger(Object obj) {
        return DfTypeUtil.toInteger(obj);
    }

    protected Long toLong(Object obj) {
        return DfTypeUtil.toLong(obj);
    }

    protected BigDecimal toBigDecimal(Object obj) {
        return DfTypeUtil.toBigDecimal(obj);
    }

    // ===================================================================================
    //                                                                         Date Helper
    //                                                                         ===========
    protected Date currentDate() {
        return new Date(System.currentTimeMillis());
    }

    protected Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }

    protected Date toDate(Object obj) {
        return DfTypeUtil.toDate(obj);
    }

    protected Timestamp toTimestamp(Object obj) {
        return DfTypeUtil.toTimestamp(obj);
    }

    // ===================================================================================
    //                                                                   Collection Helper
    //                                                                   =================
    protected <ELEMENT> ArrayList<ELEMENT> newArrayList() {
        return DfCollectionUtil.newArrayList();
    }

    public <ELEMENT> ArrayList<ELEMENT> newArrayList(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    protected <ELEMENT> ArrayList<ELEMENT> newArrayList(ELEMENT... elements) {
        return DfCollectionUtil.newArrayList(elements);
    }

    protected <ELEMENT> HashSet<ELEMENT> newHashSet() {
        return DfCollectionUtil.newHashSet();
    }

    protected <ELEMENT> HashSet<ELEMENT> newHashSet(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newHashSet(elements);
    }

    protected <ELEMENT> HashSet<ELEMENT> newHashSet(ELEMENT... elements) {
        return DfCollectionUtil.newHashSet(elements);
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet() {
        return DfCollectionUtil.newLinkedHashSet();
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet(Collection<ELEMENT> elements) {
        return DfCollectionUtil.newLinkedHashSet(elements);
    }

    protected <ELEMENT> LinkedHashSet<ELEMENT> newLinkedHashSet(ELEMENT... elements) {
        return DfCollectionUtil.newLinkedHashSet(elements);
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap() {
        return DfCollectionUtil.newHashMap();
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key, VALUE value) {
        return DfCollectionUtil.newHashMap(key, value);
    }

    protected <KEY, VALUE> HashMap<KEY, VALUE> newHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        return DfCollectionUtil.newHashMap(key1, value1, key2, value2);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap() {
        return DfCollectionUtil.newLinkedHashMap();
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key, VALUE value) {
        return DfCollectionUtil.newLinkedHashMap(key, value);
    }

    protected <KEY, VALUE> LinkedHashMap<KEY, VALUE> newLinkedHashMap(KEY key1, VALUE value1, KEY key2, VALUE value2) {
        return DfCollectionUtil.newLinkedHashMap(key1, value1, key2, value2);
    }

    // ===================================================================================
    //                                                                         Transaction
    //                                                                         ===========
    // reserved interfaces
    /**
     * Begin new transaction (even if the transaction has already been begun). <br />
     * You can manually commit or roll-back at your favorite timing by returned transaction resource. <br />
     * On the other hand, you might have mistake of transaction handling. <br />
     * So, also you can use {@link #performNewTransaction(TransactionPerformer)}. (easier)
     * @return The resource of transaction, you can commit or roll-back it. (basically NotNull: if null, transaction unsupported)
     */
    protected TransactionResource beginNewTransaction() {
        // should be overridden by DI container's test case
        return null;
    }

    /**
     * Commit the specified transaction.
     * @param resource The resource of transaction provided by beginNewTransaction(). (NotNull)
     */
    protected void commitTransaction(TransactionResource resource) {
    }

    /**
     * Roll-back the specified transaction.
     * @param resource The resource of transaction provided by beginNewTransaction(). (NotNull)
     */
    protected void rollbackTransaction(TransactionResource resource) {
    }

    /**
     * Perform the process in new transaction (even if the transaction has already been begun). <br />
     * You can select commit or roll-back by returned value of the callback method. 
     * <pre>
     * performNewTransaction(new TransactionPerformer() {
     *     public boolean perform() { <span style="color: #3F7E5E">// transaction scope</span>
     *         ...
     *         return false; <span style="color: #3F7E5E">// true: commit, false: roll-back</span>
     *     }
     * });
     * </pre>
     * @param performer The callback for the transaction process. (NotNull)
     * @throws TransactionPerformFailureException When the performance fails.
     */
    protected void performNewTransaction(TransactionPerformer performer) {
        assertNotNull(performer);
        final TransactionResource resource = beginNewTransaction();
        Exception cause = null;
        boolean commit = false;
        try {
            commit = performer.perform();
        } catch (RuntimeException e) {
            cause = e;
        } catch (SQLException e) {
            cause = e;
        } finally {
            if (commit && cause == null) {
                try {
                    commitTransaction(resource);
                } catch (RuntimeException e) {
                    cause = e;
                }
            } else {
                try {
                    rollbackTransaction(resource);
                } catch (RuntimeException e) {
                    if (cause != null) {
                        log(e.getMessage());
                    } else {
                        cause = e;
                    }
                }
            }
        }
        if (cause != null) {
            String msg = "Failed to perform the process in transaction: " + performer;
            throw new TransactionPerformFailureException(msg, cause);
        }
    }

    protected void xassertTransactionResourceNotNull(TransactionResource resource) {
        if (resource == null) {
            String msg = "The argument 'resource' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Get the data source for database.
     * @return The instance from DI container. (basically NotNull: if null, data source unsupported)
     */
    protected DataSource getDataSource() {
        // should be overridden by DI container's test case
        return null;
    }

    // ===================================================================================
    //                                                                         Cannon-ball
    //                                                                         ===========
    /**
     * Execute the cannon-ball run. (Do you know cannon-ball run?) <br />
     * Default thread count is 10, and repeat count is 1.
     * <pre>
     * <span style="color: #FD4747">cannonball</span>(new CannonballRun() {
     *     public void drive(CannonballCar car) {
     *         ... <span style="color: #3F7E5E">// 10 threads is running at the same time</span>
     *     }
     * }, new CannonballOption().expect...);
     * @param run The callback for the run. (NotNull)
     * @param option The option for the run. (NotNull)
     */
    protected void cannonball(CannonballRun run, CannonballOption option) {
        assertNotNull(run);
        assertNotNull(option);
        createCannonballDirector().readyGo(run, option);
    }

    /**
     * Create the instance of cannon-ball director.
     * @return The new-created instance of the director. (NotNull)
     */
    protected CannonballDirector createCannonballDirector() { // customize point
        return new CannonballDirector(xcreateCannonballStaff());
    }

    /**
     * Create the instance of cannon-ball staff.
     * @return The new-created instance of the staff. (NotNull)
     */
    protected CannonballStaff xcreateCannonballStaff() {
        return new CannonballStaff() {
            public TransactionResource help_beginTransaction() {
                return beginNewTransaction();
            }

            public void help_prepareAccessContext() {
                xprepareAccessContext();
            }

            public void help_clearAccessContext() {
                xclearAccessContext();
            }

            public void help_assertEquals(Object expected, Object actual) {
                assertEquals(expected, actual);
            }

            public void help_fail(String msg) {
                fail(msg);
            }

            public void help_log(Object... msgs) {
                log(msgs);
            }

            public String help_ln() {
                return ln();
            }
        };
    }

    // ===================================================================================
    //                                                                        Police Story
    //                                                                        ============
    /**
     * Tell me about your police story of Java class chase. (default: '.java' files under src/main/java)
     * <pre>
     * policeStoryOfJavaClassChase(new PoliceStoryJavaClassHandler() {
     *     public void handle(File srcFile, Class<?> clazz) {
     *         <span style="color: #3F7E5E">// handle the class as you like it</span>
     *         <span style="color: #3F7E5E">// e.g. clazz.getMethods(), readLine(srcFile, ...)</span>
     *     }
     * });
     * </pre>
     * @param handler The handler of Java class. (NotNull)
     */
    public void policeStoryOfJavaClassChase(PoliceStoryJavaClassHandler handler) {
        assertNotNull(handler);
        createPoliceStory().chaseJavaClass(handler);
    }

    /**
     * Tell me about your police story of JSP file chase. (default: '.jsp' files under src/main/webapp)
     * <pre>
     * policeStoryOfJspFileChase(new PoliceStoryJspFileHandler() {
     *     public void handle(File jspFile) {
     *         <span style="color: #3F7E5E">// handle the class as you like it</span>
     *         <span style="color: #3F7E5E">// e.g. readLine(jspFile, ...)</span>
     *     }
     * });
     * </pre>
     * @param handler The handler of JSP file. (NotNull)
     */
    public void policeStoryOfJspFileChase(PoliceStoryJspFileHandler handler) {
        assertNotNull(handler);
        createPoliceStory().chaseJspFile(handler);
    }

    /**
     * Tell me about your police story of miscellaneous resource chase.
     * <pre>
     * policeStoryOfMiscFileChase(new PoliceStoryMiscFileHandler() {
     *     public void handle(File miscFile) {
     *         <span style="color: #3F7E5E">// handle the class as you like it</span>
     *         <span style="color: #3F7E5E">// e.g. readLine(miscFile, ...)</span>
     *     }
     * }, miscDir); <span style="color: #3F7E5E">// you can specify base directory of file</span>
     * </pre>
     * @param handler The handler of miscellaneous resource. (NotNull)
     * @param baseDir The base directory for the miscellaneous file. (NotNull)
     */
    public void policeStoryOfMiscFileChase(PoliceStoryMiscFileHandler handler, File baseDir) {
        assertNotNull(handler);
        assertNotNull(baseDir);
        createPoliceStory().chaseMiscFile(handler, baseDir);
    }

    /**
     * Tell me about your police story of project resource chase. (default: under target/test-classes/../../)
     * <pre>
     * policeStoryOfProjectResourceChase(new PoliceStoryProjectResourceHandler() {
     *     public void handle(File resourceFile) {
     *         <span style="color: #3F7E5E">// handle the class as you like it</span>
     *         <span style="color: #3F7E5E">// e.g. readLine(resourceFile, ...)</span>
     *     }
     * });
     * </pre>
     * @param handler The handler of project resource. (NotNull)
     */
    public void policeStoryOfProjectResourceChase(PoliceStoryProjectResourceHandler handler) {
        assertNotNull(handler);
        createPoliceStory().chaseProjectResource(handler);
    }

    /**
     * Tell me about your police story of web resource chase. (default: under src/main/webapp)
     * <pre>
     * policeStoryOfWebResourceChase(new PoliceStoryWebResourceHandler() {
     *     public void handle(File resourceFile) {
     *         <span style="color: #3F7E5E">// handle the class as you like it</span>
     *         <span style="color: #3F7E5E">// e.g. readLine(resourceFile, ...)</span>
     *     }
     * });
     * </pre>
     * @param handler The handler of web resource. (NotNull)
     */
    public void policeStoryOfWebResourceChase(PoliceStoryWebResourceHandler handler) {
        assertNotNull(handler);
        createPoliceStory().chaseWebResource(handler);
    }

    /**
     * Create the instance of police story for many story.
     * @return The new-created instance of the police story. (NotNull)
     */
    protected PoliceStory createPoliceStory() { // customize point
        return new PoliceStory(this, getProjectDir());
    }

    // ===================================================================================
    //                                                                          Filesystem
    //                                                                          ==========
    /**
     * Read the line of the text file.
     * @param textFile The file object of text. (NotNull)
     * @param encoding The encoding of the file. (NotNull)
     * @param handler The handler of line string for the text file. (NotNull)
     * @throws IllegalStateException When it fails to read the text file.
     */
    protected void readLine(File textFile, String encoding, FileLineHandler handler) {
        assertNotNull(textFile);
        assertNotNull(encoding);
        assertNotNull(handler);
        final FilesystemPlayer reader = createFilesystemPlayer();
        reader.readLine(textFile, encoding, handler);
    }

    /**
     * Create the filesystem player for e.g. reading line.
     * @return The new-created instance of the player. (NotNull)
     */
    protected FilesystemPlayer createFilesystemPlayer() { // customize point
        return new FilesystemPlayer();
    }

    /**
     * Get the directory object of the (application or Eclipse) project. (default: target/test-classes/../../)
     * @return The file object of the directory. (NotNull)
     */
    protected File getProjectDir() { // customize point
        return getTestCaseBuildDir().getParentFile().getParentFile(); // target/test-classes/../../
    }

    /**
     * Get the directory object of the build for the test case. (default: target/test-classes)
     * @return The file object of the directory. (NotNull)
     */
    protected File getTestCaseBuildDir() {
        return DfResourceUtil.getBuildDir(getClass()); // target/test-classes
    }

    // ===================================================================================
    //                                                                         Thread Fire
    //                                                                         ===========
    // not deprecated for now (only treated as old style in comment)
    /**
     * It's old style. You can use cannonball().
     * @param execution The execution of thread-fire
     */
    protected <RESULT> void threadFire(ThreadFireExecution<RESULT> execution) {
        threadFire(execution, new ThreadFireOption());
    }

    /**
     * It's old style. You can use cannonball().
     * @param execution The execution of thread-fire
     * @param option The option of thread-fire
     */
    protected <RESULT> void threadFire(ThreadFireExecution<RESULT> execution, ThreadFireOption option) {
        final ThreadFireMan fireMan = new ThreadFireMan(new ThreadFireHelper() {
            public TransactionResource help_beginTransaction() {
                return beginNewTransaction();
            }

            public void help_prepareAccessContext() {
                xprepareAccessContext();
            }

            public void help_clearAccessContext() {
                xclearAccessContext();
            }

            public void help_assertEquals(Object expected, Object actual) {
                assertEquals(expected, actual);
            }

            public void help_fail(String msg) {
                fail(msg);
            }

            public void help_log(Object... msgs) {
                log(msgs);
            }

            public String help_ln() {
                return ln();
            }
        });
        fireMan.threadFire(execution, option);
    }

    /**
     * Sleep the current thread.
     * @param millis The millisecond to sleep.
     */
    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            String msg = "Failed to sleep but I want to sleep here...Zzz...";
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                       System Helper
    //                                                                       =============
    /**
     * Get the line separator. (LF fixedly)
     * @return The string of the line separator. (NotNull)
     */
    protected String ln() {
        return "\n";
    }

    protected String xgetCaseDisp() {
        return getClass().getSimpleName() + "." + getName() + "()";
    }
}
