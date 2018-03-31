package utils;

import java.nio.ByteBuffer;
import java.util.concurrent.Future;

public class FutureBuffer {

    private Future<Integer> future;

    private ByteBuffer buffer;

    public FutureBuffer(ByteBuffer buffer, Future<Integer> future){
        this.buffer = buffer;
        this.future = future;
    }

    public Future<Integer> getFuture() {
        return future;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }
}
