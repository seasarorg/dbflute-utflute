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

import java.util.concurrent.CountDownLatch;

/**
 * @author jflute
 * @since 0.3.8 (2014/02/25 Tuesday)
 */
public class CannonballLatch {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected int _threadCount; // decremented when breakaway
    protected final CannonballLogger _logger;
    protected volatile CountDownLatch _ourLatch;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public CannonballLatch(int threadCount, CannonballLogger logger) {
        _threadCount = threadCount;
        _logger = logger;
    }

    // ===================================================================================
    //                                                                     CountDown Latch
    //                                                                     ===============
    public void await() {
        doAwait(false);
    }

    public void awaitSilently() {
        doAwait(true);
    }

    protected void doAwait(boolean silently) {
        final CountDownLatch latch;
        final boolean last;
        synchronized (this) {
            latch = prepareLatch();
            last = (actuallyGetCount(latch) == 1);
            if (last) {
                if (!silently) {
                    _logger.log("Ready...Go! (restart)");
                }
                clearLatch();
            }
            actuallyCountDown(latch); // ready go if last
        }
        if (!last) {
            if (isWaitingLatch()) {
                // to be exact, possible that threads after restart come here but no problem
                if (!silently) {
                    _logger.log("...Awaiting arrivals: count=" + actuallyGetCount(latch));
                }
                actuallyAwait(latch);
            }
        }
    }

    protected CountDownLatch prepareLatch() {
        if (_ourLatch == null) {
            _ourLatch = new CountDownLatch(_threadCount);
        }
        return _ourLatch;
    }

    protected void clearLatch() {
        _ourLatch = null;
    }

    protected boolean isWaitingLatch() {
        return _ourLatch != null;
    }

    public void forcedlyCountDown() {
        if (isWaitingLatch()) {
            actuallyCountDown(_ourLatch);
        }
    }

    protected long actuallyGetCount(CountDownLatch latch) {
        return latch.getCount();
    }

    protected void actuallyCountDown(CountDownLatch latch) {
        latch.countDown();
    }

    protected void actuallyAwait(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            String msg = "Failed to await by your latch: latch=" + latch;
            throw new CannonballRetireException(msg, e);
        }
    }

    public synchronized void breakaway() {
        decrementThreadCount();
        reset();
    }

    protected void decrementThreadCount() {
        --_threadCount;
    }

    public synchronized void reset() {
        if (_ourLatch == null) {
            return;
        }
        final long count = _ourLatch.getCount();
        if (count > 0) {
            _logger.log("...Resetting your latch: count=" + count);
            for (int i = 0; i < count; i++) {
                _ourLatch.countDown(); // is thread safe and allowed over count down
            }
        }
        _ourLatch = null;
    }
}
