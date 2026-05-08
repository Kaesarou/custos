import { network } from "hardhat";
import fs from "node:fs";

async function main() {
  const connection = await network.connect();
  const ethers = connection.ethers;

  if (!ethers) {
    throw new Error(
      "Hardhat ethers plugin is not loaded. Check hardhat.config.ts plugins config."
    );
  }

  const [deployer, alice, bob] = await ethers.getSigners();

  const HARDHAT_PRIVATE_KEYS = {
    deployer: "0xac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80",
    alice: "0x59c6995e998f97a5a0044966f0945389dc9e86dae88c7a8412f4603b6b78690d",
    bob: "0x5de4111afa1a4b94908f83103eb1f1706367c2e68ca870fc3fb9a804cdab365a"
  };

  console.log("[Deploy] Deployer:", deployer.address);
  console.log("[Deploy] Alice:", alice.address);
  console.log("[Deploy] Bob:", bob.address);

  const TestERC721 = await ethers.getContractFactory("TestERC721");
  const testERC721 = await TestERC721.deploy();
  await testERC721.waitForDeployment();

  const TestERC1155 = await ethers.getContractFactory("TestERC1155");
  const testERC1155 = await TestERC1155.deploy();
  await testERC1155.waitForDeployment();

  const TestERC20 = await ethers.getContractFactory("TestERC20");
  const testERC20 = await TestERC20.deploy();
  await testERC20.waitForDeployment();

  const erc721Address = await testERC721.getAddress();
  const erc1155Address = await testERC1155.getAddress();
  const erc20Address = await testERC20.getAddress();

  console.log("[Deploy] TestERC721:", erc721Address);
  console.log("[Deploy] TestERC1155:", erc1155Address);
  console.log("[Deploy] TestERC20:", erc20Address);

  const erc721Tx = await testERC721.mint(alice.address);
  await erc721Tx.wait();

  const erc1155Tx1 = await testERC1155.mint(alice.address, 1, 10);
  await erc1155Tx1.wait();

  const erc1155Tx2 = await testERC1155.mint(bob.address, 2, 1);
  await erc1155Tx2.wait();

  const erc20Tx1 = await testERC20.mint(
    alice.address,
    ethers.parseEther("1000")
  );
  await erc20Tx1.wait();

  const erc20Tx2 = await testERC20.mint(
    bob.address,
    ethers.parseEther("250")
  );
  await erc20Tx2.wait();

  const deployments = {
    chainId: 31337,
    rpcUrl: "http://localhost:8545",
    accounts: {
        deployer: {
            address: deployer.address,
            privateKey: HARDHAT_PRIVATE_KEYS.deployer
          },
          alice: {
            address: alice.address,
            privateKey: HARDHAT_PRIVATE_KEYS.alice
          },
          bob: {
            address: bob.address,
            privateKey: HARDHAT_PRIVATE_KEYS.bob
          }
    },
    contracts: {
      testERC721: erc721Address,
      testERC1155: erc1155Address,
      testERC20: erc20Address
    },
    seededData: {
      erc721: [
        {
          owner: alice.address,
          tokenId: "1"
        }
      ],
      erc1155: [
        {
          owner: alice.address,
          tokenId: "1",
          amount: "10"
        },
        {
          owner: bob.address,
          tokenId: "2",
          amount: "1"
        }
      ],
      erc20: [
        {
          owner: alice.address,
          amount: ethers.parseEther("1000").toString()
        },
        {
          owner: bob.address,
          amount: ethers.parseEther("250").toString()
        }
      ]
    },
    examplePolicies: {
      erc1155Balance: {
        type: "EVM_ERC1155_BALANCE",
        chainId: 31337,
        contractAddress: erc1155Address,
        tokenId: "1",
        minAmount: "1"
      },
      erc721Ownership: {
        type: "EVM_ERC721_OWNERSHIP",
        chainId: 31337,
        contractAddress: erc721Address,
        tokenId: "1"
      },
      erc20Balance: {
        type: "EVM_ERC20_BALANCE",
        chainId: 31337,
        contractAddress: erc20Address,
        minAmount: ethers.parseEther("100").toString()
      }
    }
  };

  fs.mkdirSync("deployments", { recursive: true });

  fs.writeFileSync(
    "deployments/deployments.json",
    JSON.stringify(deployments, null, 2)
  );

  console.log("[Deploy] Seed completed.");
  console.log("[Deploy] deployments/deployments.json written.");
  console.log(JSON.stringify(deployments, null, 2));
}

main().catch((error) => {
  console.error("[Deploy] Failed:");
  console.error(error);
  process.exitCode = 1;
});