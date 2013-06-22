/*
 * Copyright 2004-2013 the Seasar Foundation and the Others.
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.seasar.dbflute.AccessContext;
import org.seasar.dbflute.cbean.PagingResultBean;
import org.seasar.dbflute.unit.core.thread.ThreadFireExecution;
import org.seasar.dbflute.unit.core.thread.ThreadFireHelper;
import org.seasar.dbflute.unit.core.thread.ThreadFireMan;
import org.seasar.dbflute.unit.core.thread.ThreadFireOption;
import org.seasar.dbflute.unit.core.transaction.TransactionPerformer;
import org.seasar.dbflute.unit.core.transaction.TransactionResource;
import org.seasar.dbflute.util.DfCollectionUtil;
import org.seasar.dbflute.util.DfStringUtil;
import org.seasar.dbflute.util.DfTypeUtil;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public abstract class PlainTestCase extends TestCase {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    /** Log instance for sub class. */
    private final Logger _logger = Logger.getLogger(getClass());

    // ===================================================================================
    //                                                                            Settings
    //                                                                            ========
    @Override
    protected void setUp() throws Exception {
        xprepareAccessContext();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        xclearAccessContext();
    }

    protected void xprepareAccessContext() {
        final AccessContext context = new AccessContext();
        context.setAccessTimestamp(currentTimestamp());
        context.setAccessDate(currentDate());
        context.setAccessUser(Thread.currentThread().getName());
        context.setAccessProcess(getClass().getSimpleName());
        AccessContext.setAccessContextOnThread(context);
    }

    protected AccessContext getAccessContext() { // user method
        return AccessContext.getAccessContextOnThread();
    }

    protected void xclearAccessContext() {
        AccessContext.clearAccessContextOnThread();
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
    //                                                  List
    //                                                  ----
    /**
     * Assert that the list has an element containing the keyword.
     * @param strList The list of string. (NotNull)
     * @param keyword The keyword string. (NotNull) 
     */
    protected void assertContainsKeyword(Collection<String> strList, String keyword) {
        if (!DfStringUtil.containsKeyword(newArrayList(strList), keyword)) {
            fail("the list should have the keyword but not found: " + keyword);
        }
    }

    protected void assertContainsKeywordAll(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAll(newArrayList(strList), keywords)) {
            fail("the list should have all keywords but not found: " + newArrayList(keywords));
        }
    }

    protected void assertContainsKeywordAllIgnoreCase(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAllIgnoreCase(newArrayList(strList), keywords)) {
            fail("the list should have all keywords (case ignored) but not found: " + newArrayList(keywords));
        }
    }

    protected void assertContainsKeywordAny(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAny(newArrayList(strList), keywords)) {
            fail("the list should have any keyword but not found: " + newArrayList(keywords));
        }
    }

    protected void assertContainsKeywordAnyIgnoreCase(Collection<String> strList, String... keywords) {
        if (!DfStringUtil.containsKeywordAnyIgnoreCase(newArrayList(strList), keywords)) {
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
        _logger.log(PlainTestCase.class.getName(), Level.DEBUG, sb.toString(), cause);
    }

    // ===================================================================================
    //                                                                         Show Helper
    //                                                                         ===========
    protected void showPage(PagingResultBean<? extends Object>... pages) {
        int count = 1;
        for (PagingResultBean<? extends Object> page : pages) {
            log("[page" + count + "]");
            for (Object entity : page) {
                log("  " + entity);
            }
            ++count;
        }
    }

    protected void showList(List<? extends Object>... lss) {
        int count = 1;
        for (List<? extends Object> ls : lss) {
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
        return DfStringUtil.replace(str, fromStr, toStr);
    }

    protected List<String> splitList(String str, String delimiter) {
        return DfStringUtil.splitList(str, delimiter);
    }

    protected List<String> splitListTrimmed(String str, String delimiter) {
        return DfStringUtil.splitListTrimmed(str, delimiter);
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
    //                                                                       Thread Helper
    //                                                                       =============
    protected <RESULT> void threadFire(ThreadFireExecution<RESULT> execution) {
        threadFire(execution, new ThreadFireOption());
    }

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

    protected void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            String msg = "Failed to sleep but I want to sleep here...Zzz...";
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                  Reserved Interface
    //                                                                  ==================
    /**
     * Begin new transaction even if the transaction has already been begun. <br />
     * Also you can use {@link #performNewTransaction(TransactionPerformer)}.
     * @return The resource of transaction, you can commit or roll-back it. (basically NotNull: if null, transaction unsupported)
     */
    protected TransactionResource beginNewTransaction() {
        // should be overridden by DI container's test case
        return null;
    }

    protected void commitTransaction(TransactionResource resource) {
    }

    protected void rollbackTransaction(TransactionResource resource) {
    }

    /**
     * Perform the process in new transaction. you can select commit or roll-back.
     * @param performer The callback for the transaction process. (NotNull)
     */
    protected void performNewTransaction(TransactionPerformer performer) {
        final TransactionResource resource = beginNewTransaction();
        RuntimeException cause = null;
        boolean commit = false;
        try {
            commit = performer.perform();
        } catch (RuntimeException e) {
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
            throw cause;
        }
    }

    protected void xassertTransactionResourceNotNull(TransactionResource resource) {
        if (resource == null) {
            String msg = "The argument 'resource' should not be null.";
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                       System Helper
    //                                                                       =============
    protected String ln() {
        return "\n";
    }
}
