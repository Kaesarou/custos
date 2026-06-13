package io.custos.node.adapters.out.network;

import io.custos.node.adapters.out.network.dto.RemoteNodeCapabilitiesDto;
import io.custos.node.adapters.out.network.dto.RemoteNodeIdentityDto;
import io.custos.node.adapters.out.network.dto.RemoteNodeStatusDto;
import io.custos.node.core.application.port.out.PeerClient;
import io.custos.node.core.domain.model.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

@Component
public class HttpPeerClient implements PeerClient {

    private static final String PEER_UNREACHABLE = "PEER_UNREACHABLE";

    private final RestClient.Builder restClientBuilder;

    public HttpPeerClient(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    @Override
    public PeerNodeView inspectPeer(String baseUrl) {
        try {
            RestClient restClient = restClientBuilder
                    .baseUrl(baseUrl)
                    .build();

            NodeIdentity identity = fetchIdentity(restClient);
            NodeStatus status = fetchStatus(restClient);
            NodeCapabilities capabilities = fetchCapabilities(restClient);

            return PeerNodeView.reachable(
                    baseUrl,
                    identity,
                    status,
                    capabilities
            );

        } catch (RestClientException | IllegalArgumentException e) {
            return PeerNodeView.unreachable(
                    baseUrl,
                    PEER_UNREACHABLE
            );
        }
    }

    private NodeIdentity fetchIdentity(RestClient restClient) {
        RemoteNodeIdentityDto response = restClient.get()
                .uri("/api/v1/node/id")
                .retrieve()
                .body(RemoteNodeIdentityDto.class);

        if (response == null) {
            throw new IllegalArgumentException("Empty identity response");
        }

        return new NodeIdentity(
                response.nodeId(),
                response.nodeAddress(),
                response.rewardAddress(),
                response.publicBaseUrl(),
                response.signatureAlgorithm()
        );
    }

    private NodeStatus fetchStatus(RestClient restClient) {
        RemoteNodeStatusDto response = restClient.get()
                .uri("/api/v1/node/status")
                .retrieve()
                .body(RemoteNodeStatusDto.class);

        if (response == null) {
            throw new IllegalArgumentException("Empty status response");
        }

        return new NodeStatus(
                response.nodeId(),
                response.status(),
                Instant.parse(response.startedAt()),
                Instant.parse(response.currentTime()),
                response.uptimeSeconds()
        );
    }

    private NodeCapabilities fetchCapabilities(RestClient restClient) {
        RemoteNodeCapabilitiesDto response = restClient.get()
                .uri("/api/v1/node/capabilities")
                .retrieve()
                .body(RemoteNodeCapabilitiesDto.class);

        if (response == null) {
            throw new IllegalArgumentException("Empty capabilities response");
        }

        return new NodeCapabilities(
                response.nodeId(),
                response.supportedPolicyTypes()
                        .stream()
                        .map(PolicyType::valueOf)
                        .toList(),
                response.supportedShareProtectionAlgorithms(),
                response.signatureAlgorithm(),
                response.supportedChains()
                        .stream()
                        .map(chain -> new NodeCapabilities.SupportedChain(chain.chainId()))
                        .toList()
        );
    }
}