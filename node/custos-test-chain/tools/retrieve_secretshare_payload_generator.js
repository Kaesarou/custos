//npm init -y
//npm install ethers
//node .\signature_generator.js

import { Wallet } from 'ethers';

const wallet = new Wallet('0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d');

const secretId = '1';
const nonce = 'test-nonce-1234';

const message = `Custos retrieve secret share
secretId: ${secretId}
userAddress: ${wallet.address.toLowerCase()}
nonce: ${nonce}`;

async function main() {
  const sig = await wallet.signMessage(message);

  console.log(JSON.stringify({
    userAddress: wallet.address,
    walletSignature: sig,
    readerPublicKey: '0xREADER_PUBLIC_KEY_TEST',
    nonce
  }, null, 2));
}

main();