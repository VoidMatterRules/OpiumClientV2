package we.devs.opium.client.events;

import we.devs.opium.api.manager.event.EventArgument;
import we.devs.opium.api.manager.event.EventListener;

public class EventKey extends EventArgument {
    private final int keyCode;
    private final int scanCode;

    public EventKey(int keyCode, int scanCode) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
    }

    public int getKeyCode() {
        return this.keyCode;
    }

    public int getScanCode() {
        return this.scanCode;
    }

    @Override
    public void call(EventListener listener) {
        listener.onKey(this);
    }
}
