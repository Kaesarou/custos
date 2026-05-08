// store_secretshare_payload_generator.js
//
// Generates a valid Store Secret Share payload signed by Bob.
//
// Usage:
//   node store_secretshare_payload_generator.js
//
// Requires:
//   npm install ethers

const { Wallet, keccak256, toUtf8Bytes } = require("ethers");

const users = {
  bob: {
    address: "0x3C44CdDdB6a900fa2b585dd299e03d12FA4293BC",
    privateKey:
      "0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a",
  },
};

const publisher = users.bob;

const secretId = "1";
const encryptedShare = "encrypted-share";

const policyDataObject = {
  tokenId: "1",
  minBalance: "1",
};

// Important:
// This JSON string must be exactly the same string sent in the API payload,
// because Java AccessPolicy#getCanonical() hashes policyData as a raw String.
const policyData = JSON.stringify(policyDataObject);

const policy = {
  type: "EVM_ERC1155_BALANCE",
  chainId: 31337,
  contractAddress: "0xe7f1725E7734CE288F8367e1Bb143E90bb3F0512",
  policyData,
};

function hashLikeJava(value) {
  return keccak256(toUtf8Bytes(value));
}

function canonicalAccessPolicy(policy) {
  return [
    policy.type,
    String(policy.chainId),
    policy.contractAddress.toLowerCase(),
    policy.policyData,
  ].join("|");
}

function buildStoreSecretShareMessage({
  secretId,
  publisherAddress,
  encryptedShareHash,
  policyHash,
}) {
  return [
    "Custos store secret share",
    `secretId: ${secretId}`,
    `publisherAddress: ${publisherAddress.toLowerCase()}`,
    `encryptedShareHash: ${encryptedShareHash}`,
    `policyHash: ${policyHash}`,
  ].join("\n");
}

async function main() {
  console.log(publisher.privateKey.length);
  console.log(/^0x[0-9a-fA-F]{64}$/.test(publisher.privateKey));

  const wallet = new Wallet(publisher.privateKey);

  if (wallet.address.toLowerCase() !== publisher.address.toLowerCase()) {
    throw new Error(
      `Bob private key does not match Bob address. Expected ${publisher.address}, got ${wallet.address}`
    );
  }

  const canonicalPolicy = canonicalAccessPolicy(policy);
  const encryptedShareHash = hashLikeJava(encryptedShare);
  const policyHash = hashLikeJava(canonicalPolicy);

  const message = buildStoreSecretShareMessage({
    secretId,
    publisherAddress: publisher.address,
    encryptedShareHash,
    policyHash,
  });

  const publisherSignature = await wallet.signMessage(message);

  const payload = {
    secretId,
    encryptedShare,
    policy: {
      type: policy.type,
      chainId: policy.chainId,
      contractAddress: policy.contractAddress,
      policyData: policy.policyData,
    },
    publisherAddress: publisher.address,
    publisherSignature,
  };

  console.log("=== Canonical AccessPolicy ===");
  console.log(canonicalPolicy);
  console.log();

  console.log("=== Hashes ===");
  console.log("encryptedShareHash:", encryptedShareHash);
  console.log("policyHash:", policyHash);
  console.log();

  console.log("=== Message signed by Bob ===");
  console.log(message);
  console.log();

  console.log("=== Publisher signature ===");
  console.log(publisherSignature);
  console.log();

  console.log("=== Store secret share payload ===");
  console.log(JSON.stringify(payload, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});