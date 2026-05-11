// retrieve_secretshare_payload_generator.js
//
// Generates a valid Retrieve Secret Share payload signed by Alice.
//
// Usage:
//   node retrieve_secretshare_payload_generator.js
//
// Requires:
//   npm install ethers

//npm init -y
//npm install ethers
//node .\signature_generator.js

const { Wallet } = require("ethers");
const crypto = require("crypto");

const users = {
  alice: {
    address: "0x70997970C51812dc3A010C7d01b50e0d17dc79C8",
    privateKey:
      "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d",
  },
};

const reader = users.alice;

const secretId = "1";
const nonce = "test-nonce-1234";

const RAW_X25519_PUBLIC_KEY_LENGTH_BYTES = 32;

function base64UrlNoPadding(buffer) {
  return Buffer.from(buffer).toString("base64url");
}

function generateX25519ReaderKeyPair() {
  const keyPair = crypto.generateKeyPairSync("x25519");

  const publicKeyDer = keyPair.publicKey.export({
    type: "spki",
    format: "der",
  });

  const privateKeyDer = keyPair.privateKey.export({
    type: "pkcs8",
    format: "der",
  });

  // In Node.js, X25519 SPKI DER ends with the 32 raw public key bytes.
  const rawPublicKey = publicKeyDer.subarray(
    publicKeyDer.length - RAW_X25519_PUBLIC_KEY_LENGTH_BYTES
  );

  return {
    readerPublicKey: base64UrlNoPadding(rawPublicKey),
    readerPrivateKeyPkcs8Base64Url: base64UrlNoPadding(privateKeyDer),
    readerPublicKeySpkiBase64Url: base64UrlNoPadding(publicKeyDer),
  };
}

function buildRetrieveSecretShareMessage({
  secretId,
  userAddress,
  readerPublicKey,
  nonce,
}) {
  return [
    "Custos retrieve secret share",
    `secretId: ${secretId}`,
    `userAddress: ${userAddress.toLowerCase()}`,
    `readerPublicKey: ${readerPublicKey}`,
    `nonce: ${nonce}`,
  ].join("\n");
}

async function main() {
  console.log("privateKey length:", reader.privateKey.length);
  console.log(
    "privateKey valid format:",
    /^0x[0-9a-fA-F]{64}$/.test(reader.privateKey)
  );

  const wallet = new Wallet(reader.privateKey);

  if (wallet.address.toLowerCase() !== reader.address.toLowerCase()) {
    throw new Error(
      `Alice private key does not match Alice address. Expected ${reader.address}, got ${wallet.address}`
    );
  }

  const {
    readerPublicKey,
    readerPrivateKeyPkcs8Base64Url,
    readerPublicKeySpkiBase64Url,
  } = generateX25519ReaderKeyPair();

  const message = buildRetrieveSecretShareMessage({
    secretId,
    userAddress: reader.address,
    readerPublicKey,
    nonce,
  });

  const walletSignature = await wallet.signMessage(message);

  const payload = {
    userAddress: reader.address,
    walletSignature,
    readerPublicKey,
    nonce,
  };

  console.log("=== Reader X25519 keys ===");
  console.log("readerPublicKey raw 32 bytes base64url:", readerPublicKey);
  console.log("readerPublicKey SPKI DER base64url:", readerPublicKeySpkiBase64Url);
  console.log("readerPrivateKey PKCS8 DER base64url:", readerPrivateKeyPkcs8Base64Url);
  console.log();

  console.log("=== Message signed by Alice ===");
  console.log(message);
  console.log();

  console.log("=== Wallet signature ===");
  console.log(walletSignature);
  console.log();

  console.log("=== Retrieve secret share payload ===");
  console.log(JSON.stringify(payload, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});