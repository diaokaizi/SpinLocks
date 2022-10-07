import java.util.concurrent.atomic.AtomicBoolean;

public class TTASLock {
    private AtomicBoolean state = new AtomicBoolean( false );
    public void lock() {
        while ( true ) {
            while (state.get()) {
            }
            if (!state.getAndSet( true )) {
                break ;
            }
        }
    }
    public void unlock() {
        state.set( false );
    }
}
