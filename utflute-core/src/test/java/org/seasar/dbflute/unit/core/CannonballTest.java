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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.seasar.dbflute.unit.core.cannonball.CannonballCar;
import org.seasar.dbflute.unit.core.cannonball.CannonballDragon;
import org.seasar.dbflute.unit.core.cannonball.CannonballOption;
import org.seasar.dbflute.unit.core.cannonball.CannonballProjectA;
import org.seasar.dbflute.unit.core.cannonball.CannonballRun;

/**
 * @author jflute
 */
public class CannonballTest extends PlainTestCase {

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    public void test_cannonball_basic() throws Exception {
        final Set<Integer> callNoList = Collections.synchronizedSet(new HashSet<Integer>());
        cannonball(new CannonballRun() {
            public void drive(CannonballCar car) {
                log(car);
                int entryNumber = car.getEntryNumber();
                assertFalse(callNoList.contains(entryNumber));
                callNoList.add(entryNumber);
            }
        }, new CannonballOption());
        assertEquals(10, callNoList.size());
    }

    public void test_cannonball_expectedSame() throws Exception {
        cannonball(new CannonballRun() {
            public void drive(CannonballCar car) {
                log(car);
                car.goal("A");
            }
        }, new CannonballOption().expectSameResult());
    }

    public void test_cannonball_expectedSameBut() throws Exception {
        try {
            cannonball(new CannonballRun() {
                public void drive(CannonballCar car) {
                    log(car);
                    if (car.isEntryNumber(3)) {
                        car.goal("A");
                    } else {
                        car.goal("B");
                    }
                }
            }, new CannonballOption().expectSameResult());
            fail();
        } catch (AssertionFailedError e) {
            log(e.getMessage());
        }
    }

    // ===================================================================================
    //                                                                             Restart
    //                                                                             =======
    public void test_cannonball_restart_basic() throws Exception {
        final Set<Integer> beforeNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        final Set<Integer> afterNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        cannonball(new CannonballRun() {
            public void drive(CannonballCar car) {
                int entryNumber = car.getEntryNumber();
                log(car);
                beforeNoSet.add(entryNumber);
                car.restart();
                assertEquals(10, beforeNoSet.size());
                afterNoSet.add(entryNumber);
            }
        }, new CannonballOption());
        assertEquals(10, afterNoSet.size());
    }

    // ===================================================================================
    //                                                                           Project A
    //                                                                           =========
    public void test_cannonball_projectA_basic() throws Exception {
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan A");
                        callNoList.add(car.getEntryNumber());
                        sleep(100);
                    }
                }, 1);
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan B");
                        callNoList.add(car.getEntryNumber());
                        sleep(100);
                    }
                }, 2);
            }
        }, new CannonballOption().threadCount(2));
        assertEquals(Arrays.asList(1, 2), callNoList);
    }

    public void test_cannonball_projectA_normallyDone_expected() throws Exception {
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.expectNormallyDone();
                        log(car);
                        callNoList.add(car.getEntryNumber());
                        car.teaBreak(1000);
                    }
                }, 1);
                assertEquals(Arrays.asList(1), callNoList);
                car.goal("ProjectA");
            }
        }, new CannonballOption().threadCount(2));
        assertEquals(Arrays.asList(1), callNoList);
    }

    public void test_cannonball_projectA_normallyDone_expectedBut_basic() throws Exception {
        final Set<Integer> beforeNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.expectNormallyDone();
                        log(car);
                        callNoList.add(car.getEntryNumber());
                        sleep(5000);
                    }
                }, 1);
                beforeNoSet.add(car.getEntryNumber());
                log("(after ProjectA) beforeNoSet: " + beforeNoSet);
                if (car.isEntryNumber(2)) {
                    assertEquals(1, beforeNoSet.size()); // another thread still sleeps
                } else { // entryNumber: 1
                    fail(); // not coming here
                }
                car.goal("ProjectA");
            }
        }, new CannonballOption().threadCount(2).expectExceptionAny("expected: normally done"));
        assertEquals(Arrays.asList(1), callNoList);
    }

    public void test_cannonball_projectA_normallyDone_expectedBut_speedy() throws Exception {
        final Set<Integer> beforeNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.expectNormallyDone();
                        dragon.releaseIfOvertime(500);
                        log(car);
                        callNoList.add(car.getEntryNumber());
                        sleep(1000);
                    }
                }, 1);
                beforeNoSet.add(car.getEntryNumber());
                log("(after ProjectA) beforeNoSet: " + beforeNoSet);
                if (car.isEntryNumber(2)) {
                    assertEquals(1, beforeNoSet.size()); // another thread still sleeps
                } else { // entryNumber: 1
                    fail(); // not coming here
                }
                car.goal("ProjectA");
            }
        }, new CannonballOption().threadCount(2).expectExceptionAny("expected: normally done"));
        assertEquals(Arrays.asList(1), callNoList);
    }

    public void test_cannonball_projectA_overtime_basic() throws Exception {
        final Set<Integer> beforeNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.expectOvertime();
                        log(car);
                        callNoList.add(car.getEntryNumber());
                        sleep(5000);
                    }
                }, 1);
                beforeNoSet.add(car.getEntryNumber());
                log("(after ProjectA) beforeNoSet: " + beforeNoSet);
                if (car.isEntryNumber(2)) {
                    assertEquals(1, beforeNoSet.size()); // another thread still sleeps
                } else { // entryNumber: 1
                    assertEquals(2, beforeNoSet.size()); // lazy coming
                }
                car.goal("ProjectA");
            }
        }, new CannonballOption().threadCount(2).expectSameResult());
        assertEquals(Arrays.asList(1), callNoList);
    }

    public void test_cannonball_projectA_overtime_expectedBut() throws Exception {
        final Set<Integer> beforeNoSet = Collections.synchronizedSet(new HashSet<Integer>());
        final List<Integer> callNoList = Collections.synchronizedList(new ArrayList<Integer>());
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.expectOvertime();
                        log(car);
                        callNoList.add(car.getEntryNumber());
                        sleep(1000);
                    }
                }, 1);
                beforeNoSet.add(car.getEntryNumber());
                log("(after ProjectA) beforeNoSet: " + beforeNoSet);
                if (car.isEntryNumber(2)) {
                    assertEquals(1, beforeNoSet.size()); // another thread still sleeps
                } else { // entryNumber: 1
                    fail(); // not coming here
                }
                car.goal("ProjectA");
            }
        }, new CannonballOption().threadCount(2).expectExceptionAny("expected: overtime"));
        assertEquals(Arrays.asList(1), callNoList);
    }

    public void test_cannonball_projectA_leaveAlone_comeBack() throws Exception {
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan A");
                    }
                }, 1);
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.releaseIfOvertime(500);
                        dragon.expectOvertime();
                        log("Plan B");
                        doWait();
                    }
                }, 2);
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.releaseIfOvertime(500);
                        log("Plan C");
                        doWait();
                    }
                }, 3);
                if (car.isEntryNumber(1)) {
                    assertEquals(3, car.getOurLatch().getInitialCount());
                    assertEquals(1, car.getOurLatch().getActiveCount());
                    log("nofityAll()");
                    doNotifyAll();
                    sleep(200); // wait for come-back
                }
                car.restart();
                assertEquals(3, car.getOurLatch().getActiveCount());
                car.restart();
            }
        }, new CannonballOption().threadCount(3));
    }

    // -----------------------------------------------------
    //                                            Break Away
    //                                            ----------
    public void test_cannonball_projectA_breakAway_basic() throws Exception {
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.releaseIfOvertime(2000);
                        dragon.expectOvertime();
                        log("Plan A");
                    }
                }, 1);
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan B");
                        sleep(100);
                    }
                }, 2);
            }
        }, new CannonballOption().threadCount(5).expectExceptionAny(AssertionFailedError.class));
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                if (car.isEntryNumber(1)) {
                    throw new IllegalStateException("breakaway");
                }
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan B");
                        sleep(100);
                    }
                }, 2);
            }
        }, new CannonballOption().threadCount(2).expectExceptionAny(IllegalStateException.class));
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                if (car.isEntryNumber(1)) {
                    throw new AssertionFailedError("breakaway");
                }
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        log("Plan B");
                        sleep(100);
                    }
                }, 2);
            }
        }, new CannonballOption().threadCount(5).expectExceptionAny(AssertionFailedError.class));
    }

    public void test_cannonball_projectA_breakAway_exception() throws Exception {
        cannonball(new CannonballRun() {
            public void drive(final CannonballCar car) {
                car.projectA(new CannonballProjectA() {
                    public void plan(CannonballDragon dragon) {
                        dragon.releaseIfOvertime(500);
                        throw new IllegalStateException("plan");
                    }
                }, 1);
            }
        }, new CannonballOption().threadCount(3).expectExceptionAny("plan"));
    }

    // ===================================================================================
    //                                                                        Entry Number
    //                                                                        ============
    public void test_cannonball_isEntryNumber_invalid() throws Exception {
        cannonball(new CannonballRun() {
            public void drive(CannonballCar car) {
                log(car);
                car.isEntryNumber(99999);
            }
        }, new CannonballOption().expectExceptionAny("over count"));
    }

    // ===================================================================================
    //                                                                       Assist Helper
    //                                                                       =============
    protected synchronized void doWait() {
        try {
            wait();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    protected synchronized void doNotifyAll() {
        notifyAll();
    }
}
