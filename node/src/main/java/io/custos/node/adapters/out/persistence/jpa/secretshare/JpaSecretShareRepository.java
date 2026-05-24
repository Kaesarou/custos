package io.custos.node.adapters.out.persistence.jpa.secretshare;

import io.custos.node.core.application.port.out.SecretShareRepository;
import io.custos.node.core.domain.model.StoredSecretShare;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaSecretShareRepository implements SecretShareRepository {

    private final SpringDataSecretShareRepository repository;

    public JpaSecretShareRepository(SpringDataSecretShareRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(StoredSecretShare storedSecretShare) {
        repository.save(SecretShareJpaMapper.toEntity(storedSecretShare));
    }

    @Override
    public Optional<StoredSecretShare> findBySecretId(String secretId) {
        return repository.findById(secretId)
                .map(SecretShareJpaMapper::toDomain);
    }

    @Override
    public boolean existsBySecretId(String secretId) {
        return repository.existsById(secretId);
    }
}