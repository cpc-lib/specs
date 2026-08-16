package com.example.evcharging.iot.gateway;

import io.netty.channel.Channel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeviceChannelRegistryTest {
    @Test
    void staleDisconnectMustNotRemoveNewConnection() {
        DeviceChannelRegistry registry = new DeviceChannelRegistry();
        Channel oldChannel = mock(Channel.class);
        Channel newChannel = mock(Channel.class);
        when(oldChannel.isActive()).thenReturn(true);
        when(newChannel.isActive()).thenReturn(true);

        registry.register(1L, "CP001", oldChannel);
        registry.register(1L, "CP001", newChannel);

        verify(oldChannel).close();
        assertThat(registry.unregister(1L, "CP001", oldChannel)).isFalse();
        assertThat(registry.find(1L, "CP001")).contains(newChannel);
    }
}
