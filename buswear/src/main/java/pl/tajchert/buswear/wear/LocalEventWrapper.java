package pl.tajchert.buswear.wear;

/**
 * TODO: Add a class header comment!
 */
public class LocalEventWrapper {

    private final Object event;

    public LocalEventWrapper(Object event) {
        this.event = event;
    }

    public Object getEvent() {
        return event;
    }
}
