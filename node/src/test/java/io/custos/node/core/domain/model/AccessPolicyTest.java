package io.custos.node.core.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccessPolicyTest {

    @Test
    void shouldBuildCanonicalPolicyWithLowercaseContractAddress() {
        AccessPolicy policy = new AccessPolicy(
                PolicyType.EVM_ERC1155_BALANCE,
                31337L,
                "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
        );

        assertEquals(
                "EVM_ERC1155_BALANCE|31337|0xe7f1725e7734ce288f8367e1bb143e90bb3f0512|{\"tokenId\":\"1\",\"minBalance\":\"1\"}",
                policy.getCanonical()
        );
    }

    @Test
    void shouldRejectNonPositiveChainId() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccessPolicy(
                        PolicyType.EVM_ERC1155_BALANCE,
                        0L,
                        "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                        "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
                )
        );

        assertEquals("chainId must be positive", exception.getMessage());
    }

    @Test
    void shouldRejectBlankContractAddress() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccessPolicy(
                        PolicyType.EVM_ERC1155_BALANCE,
                        31337L,
                        " ",
                        "{\"tokenId\":\"1\",\"minBalance\":\"1\"}"
                )
        );

        assertEquals("contractAddress is required", exception.getMessage());
    }

    @Test
    void shouldRejectBlankPolicyData() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new AccessPolicy(
                        PolicyType.EVM_ERC1155_BALANCE,
                        31337L,
                        "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
                        " "
                )
        );

        assertEquals("policyData is required", exception.getMessage());
    }
}