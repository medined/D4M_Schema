package com.codebits.d4m.rest.response;

import com.codebits.d4m.D4MException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;

public class D4MResponse {

    @Getter
    private static final AtomicLong lamportClockTracker = new AtomicLong();

    @Getter
    private String hostName = null;
    
    @Getter
    private String hostIp = null;

    @Setter
    @Getter
    private String message = null;

    @Setter
    @Getter
    private Throwable throwable = null;

    @Getter
    private final long wallClock = new Date().getTime();

    public D4MResponse() {
        lamportClockTracker.incrementAndGet();
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            hostIp = InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException ex) {
            throw new D4MException("Unable to get hostname.");
        }
    }

    public D4MResponse(final String message) {
        lamportClockTracker.incrementAndGet();
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            hostIp = InetAddress.getByName(hostName).getHostAddress();
        } catch (UnknownHostException ex) {
            throw new D4MException("Unable to get hostname.");
        }
        this.message = message;
    }

    public long getLamportClock() {
        return lamportClockTracker.get();
    }
    
}
