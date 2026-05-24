package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.StoredSecretShare;

import java.util.Optional;

public interface SecretShareRepository {
    void save(StoredSecretShare storedSecretShare);

    Optional<StoredSecretShare> findBySecretId(String secretId);

    boolean existsBySecretId(String secretId);
}
