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

import junit.framework.AssertionFailedError;

/**
 * @author jflute
 * @since 0.3.8 (2014/02/25 Tuesday)
 */
public class CannonballCar {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final long _threadId;
    protected final CannonballLatch _ourLatch;
    protected final int _entryNumber;
    protected final Object _lockObj;
    protected final int _countOfEntry; // to check
    protected final CannonballLogger _logger;
    protected Object _runResult;
    protected Long projectATimeLimit;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public CannonballCar(long threadId, CannonballLatch ourLatch, int entryNumber, Object lockObj, int countOfEntry,
            CannonballLogger logger) {
        _threadId = threadId;
        _ourLatch = ourLatch;
        _entryNumber = entryNumber;
        _lockObj = lockObj;
        _countOfEntry = countOfEntry;
        _logger = logger;
    }

    // ===================================================================================
    //                                                               Basic Thread Handling 
    //                                                               =====================
    public void teaBreak(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            String msg = "Failed to have a tea break but I want to...";
            throw new IllegalStateException(msg, e);
        }
    }

    // ===================================================================================
    //                                                                     CountDown Latch
    //                                                                     ===============
    /**
     * All cars restart from here.
     * <pre>
     * cannonball(new CannonballRun() {
     *     public void drive(CannonballCar car) {
     *         ...
     *         // all cars stop and wait for other cars coming here
     *         car.restart();
     *         ...
     *     }
     * }, new CannonballOption());
     * </pre>
     */
    public void restart() {
        _ourLatch.await();
    }

    /**
     * Execute project A plan for the entry. (other cars wait for the plan completion) <br />
     * But if the plan costs 3 seconds (as default), other cars go without waiting the plan completion.
     * <pre>
     * cannonball(new CannonballRun() {
     *     public void drive(CannonballCar car) {
     *         ...
     *         // entry number 1 only execute the plan, other cars wait for the plan completion.
     *         car.projectA(new CannonballProjectA() {
     *             public void plan() {
     *                 ...
     *             }
     *         }, 1);
     *         ...
     *     }
     * }, new CannonballOption());
     * </pre>
     * @param projectA The callback for the project A plan. (NotNull)
     * @param entryNumber The entry number that executes the plan. e.g. 1, 2, 3, ...(till thread count)
     */
    public void projectA(CannonballProjectA projectA, final int entryNumber) {
        checkEntryNumber(entryNumber);
        _ourLatch.awaitSilently(); // all cars gathers at first
        final CannonballWatchingStatus watchingStatus = new CannonballWatchingStatus();
        CannonballDragon dragon = null;
        if (isEntryNumber(entryNumber)) {
            _logger.log("...Executing projectA: " + entryNumber);
            dragon = createDragon(watchingStatus);
            dragon.releaseIfOvertime(3000); // fall-back watch
            projectA.plan(dragon); // execute the plan
            synchronized (watchingStatus) {
                watchingStatus.markDone(); // to suppress unnecessary forcedly count down
            }
        }
        synchronized (watchingStatus) {
            final boolean forcedly = watchingStatus.containsForcedly();
            if (!forcedly) { // except forcedly car
                restart(); // not plan target cars are always here
            } else { // forcedly car
                _logger.log("...Coming back from projectA finally: " + entryNumber);
            }
            if (isEntryNumber(entryNumber)) {
                // dragon is not null here
                if (dragon.isExpectedNormallyDone()) {
                    if (forcedly) {
                        String msg = "expected: normally done but was: the plan overtime: " + entryNumber;
                        throw new AssertionFailedError(msg);
                    }
                }
                if (dragon.isExpectedOvertime()) {
                    if (!forcedly) {
                        String msg = "expected: overtime but was: the plan normally done: " + entryNumber;
                        throw new AssertionFailedError(msg);
                    }
                }
            }
        }
    }

    protected CannonballDragon createDragon(CannonballWatchingStatus watchingStatus) {
        return new CannonballDragon(this, watchingStatus);
    }

    // ===================================================================================
    //                                                                        Entry Number
    //                                                                        ============
    /**
     * Is this car same as the specified entry number?
     * @param entryNumber The entry number to compare.
     * @return The determination, true or false.
     */
    public boolean isEntryNumber(int entryNumber) {
        checkEntryNumber(entryNumber);
        return _entryNumber == entryNumber;
    }

    protected void checkEntryNumber(int entryNumber) {
        if (entryNumber > _countOfEntry) {
            String msg = "The entry number is over count of entries: entryNumber=" + entryNumber + ", countOfEntry="
                    + _countOfEntry;
            throw new IllegalArgumentException(msg);
        }
    }

    // ===================================================================================
    //                                                                          Run Result
    //                                                                          ==========
    public void goal(Object runResult) {
        this._runResult = runResult;
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    @Override
    public String toString() {
        return "{" + _threadId + ", " + _entryNumber + "}";
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    /**
     * Get the thread ID of the car (current thread).
     * @return The long value of thread ID.
     */
    public long getThreadId() {
        return _threadId;
    }

    /**
     * Get the our latch to handle the threads.
     * @return The instance of our latch for cannon-ball. (NotNull)
     */
    public CannonballLatch getOurLatch() {
        return _ourLatch;
    }

    /**
     * Get the entry number of the car (current thread).
     * @return The assigned number. e.g. 1, 2, 3... (NotNull)
     */
    public int getEntryNumber() {
        return _entryNumber;
    }

    /**
     * Get the lock object to handle threads as you like it.
     * @return The common instance for all cars. (NotNull)
     */
    public Object getLockObj() {
        return _lockObj;
    }

    /**
     * Get the logger for the cannon-ball.
     * @return The common instance for all cars. (NotNull) 
     */
    public CannonballLogger getLogger() {
        return _logger;
    }

    /**
     * Get the run result for the car.
     * @return The object that is provided from the car. (NullAllowed: if no set)
     */
    public Object getRunResult() {
        return _runResult;
    }
}
