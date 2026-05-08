package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.WalletNonceStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.*;

class WalletNonceServiceTest {

    @Test
    void shouldMarkNonceAsUsedWithNormalizedAddressAndFixedTime() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-05-04T10:15:30Z"),
                ZoneOffset.UTC
        );

        WalletNonceStore nonceStore = mock(WalletNonceStore.class);
        WalletNonceService service = new WalletNonceService(clock, nonceStore);

        service.markNonceAsUsed(
                "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
                "1",
                "test-nonce-1234"
        );

        verify(nonceStore).markAsUsed(argThat(nonce ->
                nonce.userAddress().equals("0x70997970c51812dc3a010c7d01b50e0d17dc79c8")
                        && nonce.secretId().equals("1")
                        && nonce.nonce().equals("test-nonce-1234")
                        && nonce.usedAt().equals(Instant.parse("2026-05-04T10:15:30Z"))
        ));
    }
}