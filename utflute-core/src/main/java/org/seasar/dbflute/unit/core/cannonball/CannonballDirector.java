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
package org.seasar.dbflute.unit.core.cannonball;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.AssertionFailedError;

import org.seasar.dbflute.unit.core.transaction.TransactionResource;

/**
 * @author jflute
 * @since 0.3.8 (2014/02/25 Tuesday)
 */
public class CannonballDirector {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final CannonballStaff _cannonballHelper;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public CannonballDirector(CannonballStaff cannonballHelper) {
        _cannonballHelper = cannonballHelper;
    }

    // ===================================================================================
    //                                                                         Thread Fire
    //                                                                         ===========
    public void readyGo(CannonballRun execution, CannonballOption option) {
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
                final CannonballFinalizer finallyRunner = option.getFinalizer();
                if (finallyRunner != null) {
                    try {
                        log("...Running finally for fired threads");
                        finallyRunner.run();
                    } catch (RuntimeException continued) {
                        log("Failed to run finally: " + continued.getMessage());
                    }
                }
            }
        } catch (CannonballRetireException e) {
            final Throwable cause = e.getCause();
            if (option.isCheckExpectedExceptionAny() && option.isMatchExpectedExceptionAny(cause)) {
                thrownAny = cause;
            } else {
                if (cause instanceof AssertionFailedError) {
                    throw (AssertionFailedError) cause;
                }
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

    protected List<Object> doThreadFire(CannonballRun execution, CannonballOption option) {
        // ## Arrange ##
        final ExecutorService service = Executors.newCachedThreadPool();
        final int threadCount = option.getThreadCount();
        final CountDownLatch ready = new CountDownLatch(threadCount);
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch goal = new CountDownLatch(threadCount);
        final CannonballLogger logger = createLogger();
        final CannonballLatch ourLatch = new CannonballLatch(threadCount, logger);
        final Object lockObj = new Object();
        final List<Future<Object>> futureList = new ArrayList<Future<Object>>();
        for (int i = 0; i < threadCount; i++) { // basically synchronized with parameter size
            final int entryNumber = i + 1;
            final Callable<Object> callable = createCallable(execution, option, ready, start, goal, ourLatch,
                    entryNumber, lockObj, logger);
            final Future<Object> future = service.submit(callable);
            futureList.add(future);
        }

        // ## Act ##
        log("/- - - - - - - - - - - - - - - - - - - - - -");
        log("                                 Cannon-ball");
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
        final List<Object> resultList = handleFuture(option, futureList);
        assertSameResultIfExpected(option, resultList);
        return resultList;
    }

    protected CannonballLogger createLogger() {
        return new CannonballLogger() {
            public void log(Object... msgs) {
                CannonballDirector.this.log(msgs);
            }
        };
    }

    protected List<Object> handleFuture(CannonballOption option, final List<Future<Object>> futureList) {
        final List<Object> resultList = new ArrayList<Object>();
        for (Future<Object> future : futureList) {
            try {
                final Object result = future.get();
                resultList.add(result);
            } catch (InterruptedException e) {
                String msg = "future.get() was interrupted!";
                throw new IllegalStateException(msg, e);
            } catch (ExecutionException e) {
                String msg = "Failed to fire the thread: " + future;
                throw new CannonballRetireException(msg, e.getCause());
            }
        }
        return resultList;
    }

    protected <RESULT> void assertSameResultIfExpected(CannonballOption option, final List<RESULT> resultList) {
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
    protected Callable<Object> createCallable(final CannonballRun run, final CannonballOption option,
            final CountDownLatch ready, final CountDownLatch start, final CountDownLatch goal,
            final CannonballLatch ourLatch, final int entryNumber, final Object lockObj, final CannonballLogger logger) {
        return new Callable<Object>() {
            public Object call() { // each thread here
                final long threadId = Thread.currentThread().getId();
                final CannonballCar car = createCar(threadId, ourLatch, entryNumber, lockObj, option, logger);
                boolean failure = false;
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
                    Object result = null;
                    try {
                        run.drive(car);
                        result = car.getRunResult();
                    } catch (RuntimeException e) {
                        failure = true;
                        throw e;
                    } finally {
                        if (txRes != null) {
                            try {
                                if (!failure && option.isCommitTransaction()) {
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
                    return result;
                } finally {
                    goal.countDown();

                    // release waiting threads
                    final boolean suppressDecrement = car.isSuppressDecrementWhenBreakAway();
                    if (failure) {
                        ourLatch.breakAway(entryNumber, suppressDecrement);
                    } else {
                        ourLatch.complete(entryNumber, suppressDecrement);
                    }
                }
            }
        };
    }

    protected CannonballCar createCar(long threadId, CannonballLatch ourLatch, int entryNumber, Object lockObj,
            CannonballOption option, CannonballLogger logger) {
        final int countOfEntry = option.getThreadCount();
        return new CannonballCar(threadId, ourLatch, entryNumber, lockObj, countOfEntry, logger);
    }

    // ===================================================================================
    //                                                                         Fire Helper
    //                                                                         ===========
    protected TransactionResource beginTransaction() {
        return _cannonballHelper.help_beginTransaction();
    }

    protected void prepareAccessContext() {
        _cannonballHelper.help_prepareAccessContext();
    }

    protected void clearAccessContext() {
        _cannonballHelper.help_clearAccessContext();
    }

    protected void assertEquals(Object expected, Object actual) {
        _cannonballHelper.help_assertEquals(expected, actual);
    }

    protected void fail(String msg) {
        _cannonballHelper.help_fail(msg);
    }

    protected void log(Object... msgs) {
        _cannonballHelper.help_log(msgs);
    }

    protected String ln() {
        return _cannonballHelper.help_ln();
    }
}
