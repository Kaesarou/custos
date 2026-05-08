package io.custos.node.core.application.service;

import io.custos.node.core.application.port.out.AccessPolicyValidator;
import io.custos.node.core.domain.PolicyValidationResult;
import io.custos.node.core.domain.model.AccessPolicy;
import io.custos.node.core.domain.model.PolicyType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class PolicyValidationServiceTest {

    @Test
    void shouldDelegateToValidatorMatchingPolicyType() {
        AccessPolicy policy = new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );

        AccessPolicyValidator validator = mock(AccessPolicyValidator.class);
        when(validator.supportedType()).thenReturn(PolicyType.EVM_ERC1155_BALANCE);
        when(validator.validate(policy, "0xUser")).thenReturn(PolicyValidationResult.valid());

        PolicyValidationService service = new PolicyValidationService(List.of(validator));

        PolicyValidationResult result = service.validate(policy, "0xUser");

        assertTrue(result.isValid());
        verify(validator).validate(policy, "0xUser");
    }

    @Test
    void shouldReturnInvalidWhenPolicyIsNull() {
        PolicyValidationService service = new PolicyValidationService(List.of());

        PolicyValidationResult result = service.validate(null, "0xUser");

        assertFalse(result.isValid());
    }

    @Test
    void shouldReturnInvalidWhenNoValidatorSupportsPolicyType() {
        AccessPolicy policy = new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );

        PolicyValidationService service = new PolicyValidationService(List.of());

        PolicyValidationResult result = service.validate(policy, "0xUser");

        assertFalse(result.isValid());
    }
}