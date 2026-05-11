package io.custos.node.core.application.port.out;

import io.custos.node.core.domain.model.ProtectedShare;

public interface ShareProtectionService {
    ProtectedShare protect(String encryptedShare, String readerPublicKey);
}
