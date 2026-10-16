package me.thanhtnt2.fluidcompact.api.event;

import me.thanhtnt2.fluidcompact.api.FluidIntakeRequest;
import me.thanhtnt2.fluidcompact.api.FluidIntakeResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Posted on {@code NeoForge.EVENT_BUS} (the game bus) around every managed fluid intake.
 * Server side only.
 */
public abstract class FluidIntakeEvent extends Event {

    private final FluidIntakeRequest request;

    protected FluidIntakeEvent(FluidIntakeRequest request) {
        this.request = request;
    }

    public FluidIntakeRequest getRequest() {
        return request;
    }

    /**
     * Fired before anything is drained.
     *
     * <p>Cancelling it exempts this intake from FluidCompact: the machine keeps its original
     * (possibly infinite) behaviour. Use {@link #setRequestedLevels(int)} to make it cheaper or costlier.
     */
    public static class Pre extends FluidIntakeEvent implements ICancellableEvent {

        private int requestedLevels;

        public Pre(FluidIntakeRequest request) {
            super(request);
            this.requestedLevels = request.requestedLevels();
        }

        public int getRequestedLevels() {
            return requestedLevels;
        }

        public void setRequestedLevels(int requestedLevels) {
            this.requestedLevels = Math.max(0, requestedLevels);
        }
    }

    /** Fired after the world has been drained. Informational only. */
    public static class Post extends FluidIntakeEvent {

        private final FluidIntakeResult result;

        public Post(FluidIntakeRequest request, FluidIntakeResult result) {
            super(request);
            this.result = result;
        }

        public FluidIntakeResult getResult() {
            return result;
        }
    }
}
