package io.custos.node.adapters.out.persistence.jpa.walletnonce;

import io.custos.node.core.application.exception.WalletNonceAlreadyUsedException;
import io.custos.node.core.application.port.out.WalletNonceStore;
import io.custos.node.core.domain.model.UsedWalletNonce;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class WalletNonceJpaAdapter implements WalletNonceStore {

    private final SpringDataWalletNonceRepository repository;

    public WalletNonceJpaAdapter(SpringDataWalletNonceRepository repository) {
        this.repository = repository;
    }

    @Override
    public void markAsUsed(UsedWalletNonce nonce) {
        try {
            repository.saveAndFlush(WalletNonceJpaMapper.toEntity(nonce));
        } catch (DataIntegrityViolationException e) {
            throw new WalletNonceAlreadyUsedException(
                    nonce.userAddress(),
                    nonce.secretId(),
                    nonce.nonce()
            );
        }
    }
}
