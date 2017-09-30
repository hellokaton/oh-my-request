package io.github.biezhi.request;

import java.io.Flushable;
import java.io.IOException;

/**
 * Class that and ensures a {@link Flushable} gets flushed with proper
 * exception handling.
 *
 * @author biezhi
 * @date 2017/9/30
 */
public abstract class FlushOperation<V> extends Operation<V> {

    private final Flushable flushable;

    /**
     * Create flush operation
     *
     * @param flushable
     */
    protected FlushOperation(final Flushable flushable) {
        this.flushable = flushable;
    }

    @Override
    protected void done() throws IOException {
        flushable.flush();
    }

}
