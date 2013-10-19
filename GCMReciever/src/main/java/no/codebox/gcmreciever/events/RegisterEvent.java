package no.codebox.gcmreciever.events;

public class RegisterEvent {
    public final String regId;
    public final Exception e;

    public RegisterEvent(String regId, Exception e) {
        this.regId = regId;
        this.e = e;
    }
}
