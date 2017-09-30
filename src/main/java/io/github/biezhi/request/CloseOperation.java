package io.github.biezhi.request;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Class that ensures a {@link Closeable} gets closed with proper exception
 * handling.
 *
 * @author biezhi
 * @date 2017/9/30
 */
public abstract class CloseOperation<V> extends Operation<V> {

    private final Closeable closeable;

    private final boolean ignoreCloseExceptions;

    /**
     * Create closer for operation
     *
     * @param closeable
     * @param ignoreCloseExceptions
     */
    CloseOperation(final Closeable closeable,
                   final boolean ignoreCloseExceptions) {
        this.closeable = closeable;
        this.ignoreCloseExceptions = ignoreCloseExceptions;
    }

    @Override
    protected void done() throws IOException {
        if (closeable instanceof Flushable)
            ((Flushable) closeable).flush();
        if (ignoreCloseExceptions)
            try {
                closeable.close();
            } catch (IOException e) {
                // Ignored
            }
        else
            closeable.close();
    }

}
