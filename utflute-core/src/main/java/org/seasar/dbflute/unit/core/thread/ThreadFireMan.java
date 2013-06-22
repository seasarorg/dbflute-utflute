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
package org.seasar.dbflute.unit.core.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.seasar.dbflute.unit.core.transaction.TransactionResource;

/**
 * @author jflute
 * @since 0.1.0 (2011/07/24 Sunday)
 */
public class ThreadFireMan {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ThreadFireHelper _threadFireHelper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ThreadFireMan(ThreadFireHelper threadFireHelper) {
        _threadFireHelper = threadFireHelper;
    }

    // ===================================================================================
    //                                                                         Thread Fire
    //                                                                         ===========
    public <RESULT> void threadFire(ThreadFireExecution<RESULT> execution, ThreadFireOption option) {
        if (execution == null) {
            String msg = "The argument 'execution' should be not null.";
            throw new IllegalArgumentException(msg);
        }
        if (option == null) {
            String msg = "The argument 'option' should be not null.";
            throw new IllegalArgumentException(msg);
        }
        Throwable thrownAny = null;
        try {
            try {
                for (int i = 0; i < option.getRepeatCount(); i++) {
                    doThreadFire(execution, option);
                }
            } finally {
                final ThreadFireFinallyRunner finallyRunner = option.getFinallyRunner();
                if (finallyRunner != null) {
                    try {
                        log("...Running finally for fired threads");
                        finallyRunner.run();
                    } catch (RuntimeException continued) {
                        log("Failed to run finally: " + continued.getMessage());
                    }
                }
            }
        } catch (ThreadFireFailureException e) {
            if (!option.isCheckExpectedExceptionAny()) {
                throw e;
            }
            final Throwable cause = e.getCause();
            if (option.isMatchExpectedExceptionAny(cause)) {
                thrownAny = cause;
            } else {
                throw e;
            }
        }
        if (option.isCheckExpectedExceptionAny()) {
            if (thrownAny != null) {
                log("the expected exception:" + ln() + thrownAny.getMessage());
            } else {
                fail("The excutions should throw the exception: " + option.getExpectedExpceptionAnyExp());
            }
        }
    }

    protected <RESULT> List<RESULT> doThreadFire(ThreadFireExecution<RESULT> execution, ThreadFireOption option) {
        // ## Arrange ##
        final ExecutorService service = Executors.newCachedThreadPool();
        final int threadCount = option.getThreadCount();
        final CountDownLatch ready = new CountDownLatch(threadCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch goal = new CountDownLatch(threadCount);
        final ThreadFireLatch yourLatch = new ThreadFireLatch(threadCount, createLogger());
        final List<Future<RESULT>> futureList = new ArrayList<Future<RESULT>>();
        final List<Object> parameterList = option.getParameterList();
        for (int i = 0; i < threadCount; i++) { // basically synchronized with parameter size
            final Object parameter = parameterList.size() > i ? parameterList.get(i) : null; // just in case
            final Callable<RESULT> callable = createCallable(execution, option, ready, start, goal, parameter,
                    yourLatch);
            final Future<RESULT> future = service.submit(callable);
            futureList.add(future);
        }

        // ## Act ##
        log("/- - - - - - - - - - - - - - - - - - - - - -");
        log("                                 Thread Fire");
        log("                                 - - - - - -");
        start.countDown();
        try {
            // wait until all threads are finished
            goal.await();
        } catch (InterruptedException e) {
            String msg = "goal.await() was interrupted!";
            throw new IllegalStateException(msg, e);
        }
        log("- - - - - - - - -/ *All threads were fired");

        // ## Assert ##
        final List<RESULT> resultList = handleFuture(option, futureList);
        assertSameResultIfExpected(option, resultList);
        return resultList;
    }

    protected ThreadFireLogger createLogger() {
        return new ThreadFireLogger() {
            public void log(Object... msgs) {
                ThreadFireMan.this.log(msgs);
            }
        };
    }

    protected <RESULT> List<RESULT> handleFuture(ThreadFireOption option, final List<Future<RESULT>> futureList) {
        final List<RESULT> resultList = new ArrayList<RESULT>();
        for (Future<RESULT> future : futureList) {
            try {
                final RESULT result = future.get();
                resultList.add(result);
            } catch (InterruptedException e) {
                String msg = "future.get() was interrupted!";
                throw new IllegalStateException(msg, e);
            } catch (ExecutionException e) {
                String msg = "Failed to fire the thread: " + future;
                throw new ThreadFireFailureException(msg, e.getCause());
            }
        }
        return resultList;
    }

    protected <RESULT> void assertSameResultIfExpected(ThreadFireOption option, final List<RESULT> resultList) {
        if (option.isExpectedSameResult()) {
            RESULT preResult = null;
            for (RESULT result : resultList) {
                log(result);
                if (preResult == null) {
                    preResult = result;
                    continue;
                }
                assertEquals(preResult, result);
            }
        }
    }

    // ===================================================================================
    //                                                                            Callable
    //                                                                            ========
    protected <RESULT> Callable<RESULT> createCallable(final ThreadFireExecution<RESULT> execution,
            final ThreadFireOption option, final CountDownLatch ready, final CountDownLatch start,
            final CountDownLatch goal, final Object parameter, final ThreadFireLatch yourLatch) {
        return new Callable<RESULT>() {
            public RESULT call() { // each thread here
                final long threadId = Thread.currentThread().getId();
                try {
                    ready.countDown();
                    try {
                        start.await();
                    } catch (InterruptedException e) {
                        String msg = "start.await() was interrupted: start=" + start;
                        throw new IllegalStateException(msg, e);
                    }
                    prepareAccessContext();
                    TransactionResource txRes = null;
                    if (!option.isSuppressTransaction()) {
                        txRes = beginTransaction();
                    }
                    RESULT result = null;
                    RuntimeException cause = null;
                    try {
                        execution.execute(new ThreadFireResource(threadId, parameter, yourLatch));
                    } catch (RuntimeException e) {
                        cause = e;
                    } finally {
                        if (txRes != null) {
                            try {
                                if (cause == null && option.isCommitTransaction()) {
                                    txRes.commit();
                                } else {
                                    txRes.rollback();
                                }
                            } catch (Exception continued) {
                                log("*Failed to commit or roll-back: " + continued.getMessage());
                            }
                        }
                        clearAccessContext();
                    }
                    if (cause != null) {
                        throw cause;
                    }
                    return result;
                } finally {
                    goal.countDown();
                    yourLatch.reset(); // to release waiting threads
                }
            }
        };
    }

    // ===================================================================================
    //                                                                         Fire Helper
    //                                                                         ===========
    protected TransactionResource beginTransaction() {
        return _threadFireHelper.help_beginTransaction();
    }

    protected void prepareAccessContext() {
        _threadFireHelper.help_prepareAccessContext();
    }

    protected void clearAccessContext() {
        _threadFireHelper.help_clearAccessContext();
    }

    protected void assertEquals(Object expected, Object actual) {
        _threadFireHelper.help_assertEquals(expected, actual);
    }

    protected void fail(String msg) {
        _threadFireHelper.help_fail(msg);
    }

    protected void log(Object... msgs) {
        _threadFireHelper.help_log(msgs);
    }

    protected String ln() {
        return _threadFireHelper.help_ln();
    }
}
