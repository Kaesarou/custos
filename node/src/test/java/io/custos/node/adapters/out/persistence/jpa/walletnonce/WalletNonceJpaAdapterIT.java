package io.custos.node.adapters.out.persistence.jpa.walletnonce;

import io.custos.node.core.application.exception.WalletNonceAlreadyUsedException;
import io.custos.node.core.domain.model.UsedWalletNonce;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WalletNonceJpaAdapterIT {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-05-04T10:15:30Z"),
            ZoneOffset.UTC
    );

    private static final String USER_ADDRESS =
            "0x70997970C51812dc3A010C7d01b50e0d17dc79C8";

    private static final String SECRET_ID_1 = "1";
    private static final String SECRET_ID_2 = "2";

    private static final String NONCE_1 = "test-nonce-1234";
    private static final String NONCE_2 = "another-nonce";

    @Autowired
    private WalletNonceJpaAdapter adapter;

    @Autowired
    private SpringDataWalletNonceRepository repository;

    @Test
    @Order(1)
    void shouldStartWithEmptyWalletNonceTable() {
        repository.deleteAll();

        assertEquals(0, repository.count());
    }

    @Test
    @Order(2)
    void shouldMarkFirstNonceAsUsed() {
        adapter.markAsUsed(nonce(USER_ADDRESS, SECRET_ID_1, NONCE_1));

        assertEquals(1, repository.count());
    }

    @Test
    @Order(3)
    void shouldRejectAlreadyUsedNonceForSameUserAndSecret() {
        assertThrows(WalletNonceAlreadyUsedException.class, () ->
                adapter.markAsUsed(nonce(USER_ADDRESS, SECRET_ID_1, NONCE_1))
        );

        assertEquals(1, repository.count());
    }

    @Test
    @Order(4)
    void shouldAllowSameNonceForDifferentSecret() {
        adapter.markAsUsed(nonce(USER_ADDRESS, SECRET_ID_2, NONCE_1));

        assertEquals(2, repository.count());
    }

    @Test
    @Order(5)
    void shouldAllowDifferentNonceForSameUserAndSecret() {
        adapter.markAsUsed(nonce(USER_ADDRESS, SECRET_ID_1, NONCE_2));

        assertEquals(3, repository.count());
    }

    private static UsedWalletNonce nonce(
            String userAddress,
            String secretId,
            String nonce
    ) {
        return UsedWalletNonce.of(
                CLOCK,
                userAddress,
                secretId,
                nonce
        );
    }
}