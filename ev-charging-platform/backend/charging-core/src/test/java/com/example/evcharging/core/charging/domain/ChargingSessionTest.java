package com.example.evcharging.core.charging.domain;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChargingSessionTest {
    @Test
    void normalStateFlow() {
        var s = chargingSession();
        s.requestStop();
        s.deviceStopped(2500, Instant.now());
        assertThat(s.status()).isEqualTo(ChargingSessionStatus.CHARGE_FINISHED);
        assertThat(s.energyWh()).isEqualTo(1500);
    }

    @Test
    void duplicateStopRequestIsIdempotent() {
        var s = chargingSession();
        s.requestStop();
        s.requestStop();
        assertThat(s.status()).isEqualTo(ChargingSessionStatus.STOPPING);
    }

    @Test
    void duplicateDeviceStoppedIsIdempotent() {
        var s = chargingSession();
        s.deviceStopped(2000, Instant.now());
        s.deviceStopped(2000, Instant.now());
        assertThat(s.energyWh()).isEqualTo(1000);
    }

    @Test
    void meterRollbackMustFail() {
        var s = chargingSession();
        assertThatThrownBy(() -> s.deviceStopped(999, Instant.now())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void illegalTransitionMustFail() {
        var s = new ChargingSession(1, "CS1", 10);
        assertThatThrownBy(() -> s.startCharging(1, Instant.now())).isInstanceOf(IllegalStateException.class);
    }

    private static ChargingSession chargingSession() {
        var s = new ChargingSession(1, "CS1", 10);
        s.startRequested();
        s.prepare();
        s.startCharging(1000, Instant.now());
        return s;
    }
}
