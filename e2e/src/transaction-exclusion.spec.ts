import { beforeAll, describe, expect, it } from "@jest/globals";
import { TransactionExclusionClient, wait } from "./utils/utils";
import { Wallet } from "ethers";
import { getAndIncreaseFeeData } from "./utils/helpers";

const transactionExclusionTestSuite = (title: string) => {
  describe(title, () => {
    let transactionExclusionClient: TransactionExclusionClient;

    beforeAll(async () => {
      if (TRANSACTION_EXCLUSION_ENDPOINT != null) {
        transactionExclusionClient = new TransactionExclusionClient(TRANSACTION_EXCLUSION_ENDPOINT);
      }
    });

    it("Should get the status of the rejected transaction reported from Besu P2P node", async () => {
      if (transactionExclusionClient == null) {
        // Skip this test for dev and uat environments
        return;
      }

      const account = new Wallet(L2_ACCOUNT_2_PRIVATE_KEY, l2BesuNodeProvider);

      const [nonce, feeData] = await Promise.all([
        l2Provider.getTransactionCount(account.address),
        l2Provider.getFeeData(),
      ]);

      const [maxPriorityFeePerGas, maxFeePerGas] = getAndIncreaseFeeData(feeData);

      // This shall be rejected by the Besu node due to traces module limit overflow (as reduced traces limits)
      let rejectedTxHash = "";
      try {
        await testContract.connect(account).testAddmod(13000, 31, {
          nonce,
          maxPriorityFeePerGas,
          maxFeePerGas,
        });
      } catch (err) {
        // This shall return SERVER_ERROR with traces limit overflow
        rejectedTxHash = (err as any).transactionHash;
        console.log(`rejectedTxHash: ${JSON.stringify(rejectedTxHash)}`);
      }

      let getResponse;
      do {
        await wait(5_000);
        getResponse = await transactionExclusionClient.getTransactionExclusionStatusV1(rejectedTxHash);
      } while (!getResponse?.result);

      expect(getResponse.result.txHash).toStrictEqual(rejectedTxHash);
      expect(getResponse.result.txRejectionStage).toStrictEqual("P2P");
      expect(getResponse.result.from.toLowerCase()).toStrictEqual(account.address.toLowerCase());
    }, 120_000);

    it("Should get the status of the rejected transaction reported from Besu SEQUENCER node", async () => {
      if (transactionExclusionClient == null) {
        // Skip this test for dev and uat environments
        return;
      }

      const account = new Wallet(L2_ACCOUNT_2_PRIVATE_KEY, sequencerProvider);

      const [nonce, feeData] = await Promise.all([
        l2Provider.getTransactionCount(account.address),
        l2Provider.getFeeData(),
      ]);

      const [maxPriorityFeePerGas, maxFeePerGas] = getAndIncreaseFeeData(feeData);

      // This shall be rejected by sequencer due to traces module limit overflow (as reduced traces limits)
      const tx = await testContract.connect(account).testAddmod(13000, 31, {
        nonce,
        maxPriorityFeePerGas,
        maxFeePerGas,
      });

      const rejectedTxHash = tx.hash;
      console.log(`rejectedTxHash: ${rejectedTxHash}`);

      let getResponse;
      do {
        await wait(5_000);
        getResponse = await transactionExclusionClient.getTransactionExclusionStatusV1(rejectedTxHash);
      } while (!getResponse?.result || getResponse.result.txRejectionStage === "P2P");

      expect(getResponse.result.txHash).toStrictEqual(rejectedTxHash);
      expect(getResponse.result.txRejectionStage).toStrictEqual("SEQUENCER");
      expect(getResponse.result.from.toLowerCase()).toStrictEqual(account.address.toLowerCase());
    }, 120_000);
  });
};

export default transactionExclusionTestSuite;
