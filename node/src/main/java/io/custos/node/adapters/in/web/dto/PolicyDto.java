package io.custos.node.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PolicyDto(
        @NotBlank String type,
        @Positive long chainId,
        @NotBlank String contractAddress,
        @NotBlank String policyData
) {
}
