package org.seasar.dbflute.unit.core.thread;

import java.util.concurrent.CountDownLatch;

/**
 * @author jflute
 * @since 0.1.7 (2012/08/30 Thursday)
 */
public class ThreadFireLatch {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final int _threadCount;
    protected final ThreadFireLogger _logger;
    protected CountDownLatch _yourLatch;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ThreadFireLatch(int threadCount, ThreadFireLogger logger) {
        _threadCount = threadCount;
        _logger = logger;
    }

    // ===================================================================================
    //                                                                     CountDown Latch
    //                                                                     ===============
    public void await() {
        final CountDownLatch latch;
        final boolean last;
        synchronized (this) {
            latch = prepareLatch();
            last = (doGetCount(latch) == 1);
            if (last) {
                _logger.log("Ready...Go! (restart)");
                clearLatch();
            }
            doCountDown(latch); // ready go if last
        }
        if (!last) {
            _logger.log("...Awaiting other threads: count=" + doGetCount(latch));
            doAwait(latch);
        }
    }

    protected CountDownLatch prepareLatch() {
        if (_yourLatch == null) {
            _yourLatch = new CountDownLatch(_threadCount);
        }
        return _yourLatch;
    }

    protected void clearLatch() {
        _yourLatch = null;
    }

    protected long doGetCount(CountDownLatch latch) {
        return latch.getCount();
    }

    protected void doCountDown(CountDownLatch latch) {
        latch.countDown();
    }

    protected void doAwait(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            String msg = "Failed to await by your latch: latch=" + latch;
            throw new ThreadFireFailureException(msg, e);
        }
    }

    public synchronized void reset() {
        if (_yourLatch == null) {
            return;
        }
        final long count = _yourLatch.getCount();
        if (count > 0) {
            _logger.log("...Resetting your latch: count=" + count);
            for (int i = 0; i < count; i++) {
                _yourLatch.countDown();
            }
        }
        _yourLatch = null;
    }
}
