import { ethers } from "hardhat";
import { generateKeccak256 } from "./helpers";

export const HASH_ZERO = ethers.ZeroHash;
export const ADDRESS_ZERO = ethers.ZeroAddress;
export const HASH_WITHOUT_ZERO_FIRST_BYTE = "0xf887bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f";

export const LINEA_ROLLUP_INITIALIZE_SIGNATURE =
  "initialize((bytes32,uint256,uint256,address,uint256,uint256,(address,bytes32)[],(uint8,bytes32)[],(uint8,bytes32)[],address))";

// Linea XP Token roles
export const MINTER_ROLE = generateKeccak256(["string"], ["MINTER_ROLE"], true);
export const TRANSFER_ROLE = generateKeccak256(["string"], ["TRANSFER_ROLE"], true);

// TimeLock roles
export const TIMELOCK_ADMIN_ROLE = generateKeccak256(["string"], ["TIMELOCK_ADMIN_ROLE"], true);
export const PROPOSER_ROLE = generateKeccak256(["string"], ["PROPOSER_ROLE"], true);
export const EXECUTOR_ROLE = generateKeccak256(["string"], ["EXECUTOR_ROLE"], true);
export const CANCELLER_ROLE = generateKeccak256(["string"], ["CANCELLER_ROLE"], true);

// Roles hashes
export const DEFAULT_ADMIN_ROLE = HASH_ZERO;
export const FUNCTION_EXECUTOR_ROLE = generateKeccak256(["string"], ["FUNCTION_EXECUTOR_ROLE"], true);
export const RATE_LIMIT_SETTER_ROLE = generateKeccak256(["string"], ["RATE_LIMIT_SETTER_ROLE"], true);
export const USED_RATE_LIMIT_RESETTER_ROLE = generateKeccak256(["string"], ["USED_RATE_LIMIT_RESETTER_ROLE"], true);
export const L1_L2_MESSAGE_SETTER_ROLE = generateKeccak256(["string"], ["L1_L2_MESSAGE_SETTER_ROLE"], true);
export const PAUSE_ALL_ROLE = generateKeccak256(["string"], ["PAUSE_ALL_ROLE"], true);
export const UNPAUSE_ALL_ROLE = generateKeccak256(["string"], ["UNPAUSE_ALL_ROLE"], true);
export const PAUSE_L1_L2_ROLE = generateKeccak256(["string"], ["PAUSE_L1_L2_ROLE"], true);
export const UNPAUSE_L1_L2_ROLE = generateKeccak256(["string"], ["UNPAUSE_L1_L2_ROLE"], true);
export const PAUSE_L2_L1_ROLE = generateKeccak256(["string"], ["PAUSE_L2_L1_ROLE"], true);
export const UNPAUSE_L2_L1_ROLE = generateKeccak256(["string"], ["UNPAUSE_L2_L1_ROLE"], true);
export const PAUSE_L2_BLOB_SUBMISSION_ROLE = generateKeccak256(["string"], ["PAUSE_L2_BLOB_SUBMISSION_ROLE"], true);
export const UNPAUSE_L2_BLOB_SUBMISSION_ROLE = generateKeccak256(["string"], ["UNPAUSE_L2_BLOB_SUBMISSION_ROLE"], true);
export const PAUSE_FINALIZE_WITHPROOF_ROLE = generateKeccak256(["string"], ["PAUSE_FINALIZE_WITHPROOF_ROLE"], true);
export const UNPAUSE_FINALIZE_WITHPROOF_ROLE = generateKeccak256(["string"], ["UNPAUSE_FINALIZE_WITHPROOF_ROLE"], true);
export const MINIMUM_FEE_SETTER_ROLE = generateKeccak256(["string"], ["MINIMUM_FEE_SETTER_ROLE"], true);
export const OPERATOR_ROLE = generateKeccak256(["string"], ["OPERATOR_ROLE"], true);
export const VERIFIER_SETTER_ROLE = generateKeccak256(["string"], ["VERIFIER_SETTER_ROLE"], true);
export const VERIFIER_UNSETTER_ROLE = generateKeccak256(["string"], ["VERIFIER_UNSETTER_ROLE"], true);
export const L1_MERKLE_ROOTS_SETTER_ROLE = generateKeccak256(["string"], ["L1_MERKLE_ROOTS_SETTER_ROLE"], true);
export const L2_MERKLE_ROOTS_SETTER_ROLE = generateKeccak256(["string"], ["L2_MERKLE_ROOTS_SETTER_ROLE"], true);
export const FINALIZE_WITHOUT_PROOF_ROLE = generateKeccak256(["string"], ["FINALIZE_WITHOUT_PROOF_ROLE"], true);
export const BAD_STARTING_HASH = generateKeccak256(["string"], ["BAD_STARTING_HASH"], true);
export const PAUSE_INITIATE_TOKEN_BRIDGING_ROLE = generateKeccak256(
  ["string"],
  ["PAUSE_INITIATE_TOKEN_BRIDGING_ROLE"],
  true,
);
export const PAUSE_COMPLETE_TOKEN_BRIDGING_ROLE = generateKeccak256(
  ["string"],
  ["PAUSE_COMPLETE_TOKEN_BRIDGING_ROLE"],
  true,
);
export const UNPAUSE_INITIATE_TOKEN_BRIDGING_ROLE = generateKeccak256(
  ["string"],
  ["UNPAUSE_INITIATE_TOKEN_BRIDGING_ROLE"],
  true,
);
export const UNPAUSE_COMPLETE_TOKEN_BRIDGING_ROLE = generateKeccak256(
  ["string"],
  ["UNPAUSE_COMPLETE_TOKEN_BRIDGING_ROLE"],
  true,
);
export const SET_REMOTE_TOKENBRIDGE_ROLE = generateKeccak256(["string"], ["SET_REMOTE_TOKENBRIDGE_ROLE"], true);
export const SET_RESERVED_TOKEN_ROLE = generateKeccak256(["string"], ["SET_RESERVED_TOKEN_ROLE"], true);
export const REMOVE_RESERVED_TOKEN_ROLE = generateKeccak256(["string"], ["REMOVE_RESERVED_TOKEN_ROLE"], true);
export const SET_CUSTOM_CONTRACT_ROLE = generateKeccak256(["string"], ["SET_CUSTOM_CONTRACT_ROLE"], true);
export const SET_MESSAGE_SERVICE_ROLE = generateKeccak256(["string"], ["SET_MESSAGE_SERVICE_ROLE"], true);

export const GENERAL_PAUSE_TYPE = 1;
export const L1_L2_PAUSE_TYPE = 2;
export const L2_L1_PAUSE_TYPE = 3;
export const BLOB_SUBMISSION_PAUSE_TYPE = 4;
export const CALLDATA_SUBMISSION_PAUSE_TYPE = 5;
export const FINALIZATION_PAUSE_TYPE = 6;
export const INITIATE_TOKEN_BRIDGING_PAUSE_TYPE = 7;
export const COMPLETE_TOKEN_BRIDGING_PAUSE_TYPE = 8;

export const pauseTypeRoles = [
  { pauseType: GENERAL_PAUSE_TYPE, role: PAUSE_ALL_ROLE },
  { pauseType: L1_L2_PAUSE_TYPE, role: PAUSE_L1_L2_ROLE },
  { pauseType: L2_L1_PAUSE_TYPE, role: PAUSE_L2_L1_ROLE },
  { pauseType: BLOB_SUBMISSION_PAUSE_TYPE, role: PAUSE_L2_BLOB_SUBMISSION_ROLE },
  { pauseType: CALLDATA_SUBMISSION_PAUSE_TYPE, role: PAUSE_L2_BLOB_SUBMISSION_ROLE },
  { pauseType: FINALIZATION_PAUSE_TYPE, role: PAUSE_FINALIZE_WITHPROOF_ROLE },
  { pauseType: INITIATE_TOKEN_BRIDGING_PAUSE_TYPE, role: PAUSE_INITIATE_TOKEN_BRIDGING_ROLE },
  { pauseType: COMPLETE_TOKEN_BRIDGING_PAUSE_TYPE, role: PAUSE_COMPLETE_TOKEN_BRIDGING_ROLE },
];

export const unpauseTypeRoles = [
  { pauseType: GENERAL_PAUSE_TYPE, role: UNPAUSE_ALL_ROLE },
  { pauseType: L1_L2_PAUSE_TYPE, role: UNPAUSE_L1_L2_ROLE },
  { pauseType: L2_L1_PAUSE_TYPE, role: UNPAUSE_L2_L1_ROLE },
  { pauseType: BLOB_SUBMISSION_PAUSE_TYPE, role: UNPAUSE_L2_BLOB_SUBMISSION_ROLE },
  { pauseType: CALLDATA_SUBMISSION_PAUSE_TYPE, role: UNPAUSE_L2_BLOB_SUBMISSION_ROLE },
  { pauseType: FINALIZATION_PAUSE_TYPE, role: UNPAUSE_FINALIZE_WITHPROOF_ROLE },
  { pauseType: INITIATE_TOKEN_BRIDGING_PAUSE_TYPE, role: UNPAUSE_INITIATE_TOKEN_BRIDGING_ROLE },
  { pauseType: COMPLETE_TOKEN_BRIDGING_PAUSE_TYPE, role: UNPAUSE_COMPLETE_TOKEN_BRIDGING_ROLE },
];

// Message statuses
export const INBOX_STATUS_UNKNOWN = 0;
export const INBOX_STATUS_RECEIVED = 1;
export const INBOX_STATUS_CLAIMED = 2;

export const OUTBOX_STATUS_UNKNOWN = 0;
export const OUTBOX_STATUS_SENT = 1;
export const OUTBOX_STATUS_RECEIVED = 2;

export const INITIAL_MIGRATION_BLOCK = 0;
export const ONE_DAY_IN_SECONDS = 86_400;
export const INITIAL_WITHDRAW_LIMIT = ethers.parseEther("5");
export const GENESIS_L2_TIMESTAMP = 0;
export const DEFAULT_LAST_FINALIZED_TIMESTAMP = 1683325137n;
export const SIX_MONTHS_IN_SECONDS = (365 / 2) * 24 * 60 * 60;
export const TEST_PUBLIC_VERIFIER_INDEX = 0;

export const MESSAGE_VALUE_1ETH = ethers.parseEther("1");
export const ZERO_VALUE = 0;
export const MESSAGE_FEE = ethers.parseEther("0.05");
export const LOW_NO_REFUND_MESSAGE_FEE = ethers.parseEther("0.00001");
export const MINIMUM_FEE = ethers.parseEther("0.1");
export const DEFAULT_MESSAGE_NONCE = ethers.parseEther("123456789");
export const SAMPLE_FUNCTION_CALLDATA = generateKeccak256(["string"], ["callThisFunction()"], true).substring(0, 10); //0x + 4bytes
export const EMPTY_CALLDATA = "0x";
export const BLOCK_COINBASE = "0xc014ba5ec014ba5ec014ba5ec014ba5ec014ba5e";

export const INITIALIZED_ERROR_MESSAGE = "Initializable: contract is not initializing";
export const INITIALIZED_ALREADY_MESSAGE = "Initializable: contract is already initialized";

export const DEFAULT_SUBMISSION_DATA = {
  dataParentHash: HASH_ZERO,
  compressedData: "0x",
  finalBlockInData: 0n,
  firstBlockInData: 0n,
  parentStateRootHash: HASH_ZERO,
  finalStateRootHash: HASH_ZERO,
  snarkHash: HASH_ZERO,
};

export const BLS_CURVE_MODULUS = 52435875175126190479447740508185965837690552500527637822603658699938581184513n;

export const VALID_MERKLE_PROOF = {
  //proof length 32
  proof: [
    "0x0000000000000000000000000000000000000000000000000000000000000000",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0xb4c11951957c6f8f642c4af61cd6b24640fec6dc7fc607ee8206a99e92410d30",
    "0x21ddb9a356815c3fac1026b6dec5df3124afbadb485c9ba5a3e3398a04b7ba85",
    "0xe58769b32a1beaf1ea27375a44095a0d1fb664ce2dd358e7fcbfb78c26a19344",
    "0x0eb01ebfc9ed27500cd4dfc979272d1f0913cc9f66540d7e8005811109e1cf2d",
    "0x887c22bd8750d34016ac3c66b5ff102dacdd73f6b014e710b51e8022af9a1968",
    "0xffd70157e48063fc33c97a050f7f640233bf646cc98d9524c6b92bcf3ab56f83",
    "0x9867cc5f7f196b93bae1e27e6320742445d290f2263827498b54fec539f756af",
    "0xcefad4e508c098b9a7e1d8feb19955fb02ba9675585078710969d3440f5054e0",
    "0xf9dc3e7fe016e050eff260334f18a5d4fe391d82092319f5964f2e2eb7c1c3a5",
    "0xf8b13a49e282f609c317a833fb8d976d11517c571d1221a265d25af778ecf892",
    "0x3490c6ceeb450aecdc82e28293031d10c7d73bf85e57bf041a97360aa2c5d99c",
    "0xc1df82d9c4b87413eae2ef048f94b4d3554cea73d92b0f7af96e0271c691e2bb",
    "0x5c67add7c6caf302256adedf7ab114da0acfe870d449a3a489f781d659e8becc",
    "0xda7bce9f4e8618b6bd2f4132ce798cdc7a60e7e1460a7299e3c6342a579626d2",
    "0x2733e50f526ec2fa19a22b31e8ed50f23cd1fdf94c9154ed3a7609a2f1ff981f",
    "0xe1d3b5c807b281e4683cc6d6315cf95b9ade8641defcb32372f1c126e398ef7a",
    "0x5a2dce0a8a7f68bb74560f8f71837c2c2ebbcbf7fffb42ae1896f13f7c7479a0",
    "0xb46a28b6f55540f89444f63de0378e3d121be09e06cc9ded1c20e65876d36aa0",
    "0xc65e9645644786b620e2dd2ad648ddfcbf4a7e5b1a3a4ecfe7f64667a3f0b7e2",
    "0xf4418588ed35a2458cffeb39b93d26f18d2ab13bdce6aee58e7b99359ec2dfd9",
    "0x5a9c16dc00d6ef18b7933a6f8dc65ccb55667138776f7dea101070dc8796e377",
    "0x4df84f40ae0c8229d0d6069e5c8f39a7c299677a09d367fc7b05e3bc380ee652",
    "0xcdc72595f74c7b1043d0e1ffbab734648c838dfb0527d971b602bc216c9619ef",
    "0x0abf5ac974a1ed57f4050aa510dd9c74f508277b39d7973bb2dfccc5eeb0618d",
    "0xb8cd74046ff337f0a7bf2c8e03e10f642c1886798d71806ab1e888d9e5ee87d0",
    "0x838c5655cb21c6cb83313b5a631175dff4963772cce9108188b34ac87c81c41e",
    "0x662ee4dd2dd7b2bc707961b1e646c4047669dcb6584f0d8d770daf5d7e7deb2e",
    "0x388ab20e2573d171a88108e79d820e98f26c0b84aa8b2f4aa4968dbb818ea322",
    "0x93237c50ba75ee485f4c22adf2f741400bdf8d6a9cc7df7ecae576221665d735",
    "0x8448818bb4ae4562849e949e17ac16e0be16688e156b5cf15e098c627c0056a9",
  ],
  merkleRoot: "0x54e37f6a8efe3497d1b721d8a5a19786e78d16edabdefdfa94173ef104b132cb",
  index: 0,
};

export const INVALID_MERKLE_PROOF = {
  merkleRoot: "0xfbe8939cea4bb333e59120d35f318e3e7c88cdd0e70e66ae98b64efb9a5716ec",
  index: 0,
  proof: [
    "0x0000000000000000000000000000000000000000000000000000000000000000",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0xb4c11951957c6f8f642c4af61cd6b24640fec6dc7fc607ee8206a99e92410d30",
    "0x21ddb9a356815c3fac1026b6dec5df3124afbadb485c9ba5a3e3398a04b7ba85",
    "0xe58769b32a1beaf1ea27375a44095a0d1fb664ce2dd358e7fcbfb78c26a19344",
    "0x0eb01ebfc9ed27500cd4dfc979272d1f0913cc9f66540d7e8005811109e1cf2d",
    "0x887c22bd8750d34016ac3c66b5ff102dacdd73f6b014e710b51e8022af9a1968",
    "0xffd70157e48063fc33c97a050f7f640233bf646cc98d9524c6b92bcf3ab56f83",
    "0x9867cc5f7f196b93bae1e27e6320742445d290f2263827498b54fec539f756af",
    "0xcefad4e508c098b9a7e1d8feb19955fb02ba9675585078710969d3440f5054e0",
    "0xf9dc3e7fe016e050eff260334f18a5d4fe391d82092319f5964f2e2eb7c1c3a5",
    "0xf8b13a49e282f609c317a833fb8d976d11517c571d1221a265d25af778ecf892",
    "0x3490c6ceeb450aecdc82e28293031d10c7d73bf85e57bf041a97360aa2c5d99c",
    "0xc1df82d9c4b87413eae2ef048f94b4d3554cea73d92b0f7af96e0271c691e2bb",
    "0x5c67add7c6caf302256adedf7ab114da0acfe870d449a3a489f781d659e8becc",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0x2733e50f526ec2fa19a22b31e8ed50f23cd1fdf94c9154ed3a7609a2f1ff981f",
    "0xe1d3b5c807b281e4683cc6d6315cf95b9ade8641defcb32372f1c126e398ef7a",
    "0x5a2dce0a8a7f68bb74560f8f71837c2c2ebbcbf7fffb42ae1896f13f7c7479a0",
    "0xb46a28b6f55540f89444f63de0378e3d121be09e06cc9ded1c20e65876d36aa0",
    "0xc65e9645644786b620e2dd2ad648ddfcbf4a7e5b1a3a4ecfe7f64667a3f0b7e2",
    "0xf4418588ed35a2458cffeb39b93d26f18d2ab13bdce6aee58e7b99359ec2dfd9",
    "0x5a9c16dc00d6ef18b7933a6f8dc65ccb55667138776f7dea101070dc8796e377",
    "0x4df84f40ae0c8229d0d6069e5c8f39a7c299677a09d367fc7b05e3bc380ee652",
    "0xcdc72595f74c7b1043d0e1ffbab734648c838dfb0527d971b602bc216c9619ef",
    "0x0abf5ac974a1ed57f4050aa510dd9c74f508277b39d7973bb2dfccc5eeb0618d",
    "0xb8cd74046ff337f0a7bf2c8e03e10f642c1886798d71806ab1e888d9e5ee87d0",
    "0x838c5655cb21c6cb83313b5a631175dff4963772cce9108188b34ac87c81c41e",
    "0x662ee4dd2dd7b2bc707961b1e646c4047669dcb6584f0d8d770daf5d7e7deb2e",
    "0x388ab20e2573d171a88108e79d820e98f26c0b84aa8b2f4aa4968dbb818ea322",
    "0x93237c50ba75ee485f4c22adf2f741400bdf8d6a9cc7df7ecae576221665d735",
    "0x8448818bb4ae4562849e949e17ac16e0be16688e156b5cf15e098c627c0056a9",
  ],
};

export const INVALID_MERKLE_PROOF_REVERT = {
  proof: [
    "0x0000000000000000000000000000000000000000000000000000000000000000",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0xb4c11951957c6f8f642c4af61cd6b24640fec6dc7fc607ee8206a99e92410d30",
    "0x21ddb9a356815c3fac1026b6dec5df3124afbadb485c9ba5a3e3398a04b7ba85",
    "0xe58769b32a1beaf1ea27375a44095a0d1fb664ce2dd358e7fcbfb78c26a19344",
    "0x0eb01ebfc9ed27500cd4dfc979272d1f0913cc9f66540d7e8005811109e1cf2d",
    "0x887c22bd8750d34016ac3c66b5ff102dacdd73f6b014e710b51e8022af9a1968",
    "0xffd70157e48063fc33c97a050f7f640233bf646cc98d9524c6b92bcf3ab56f83",
    "0x9867cc5f7f196b93bae1e27e6320742445d290f2263827498b54fec539f756af",
    "0xcefad4e508c098b9a7e1d8feb19955fb02ba9675585078710969d3440f5054e0",
    "0xf9dc3e7fe016e050eff260334f18a5d4fe391d82092319f5964f2e2eb7c1c3a5",
    "0xf8b13a49e282f609c317a833fb8d976d11517c571d1221a265d25af778ecf892",
    "0x3490c6ceeb450aecdc82e28293031d10c7d73bf85e57bf041a97360aa2c5d99c",
    "0xc1df82d9c4b87413eae2ef048f94b4d3554cea73d92b0f7af96e0271c691e2bb",
    "0x5c67add7c6caf302256adedf7ab114da0acfe870d449a3a489f781d659e8becc",
    "0xda7bce9f4e8618b6bd2f4132ce798cdc7a60e7e1460a7299e3c6342a579626d2",
    "0x2733e50f526ec2fa19a22b31e8ed50f23cd1fdf94c9154ed3a7609a2f1ff981f",
    "0xe1d3b5c807b281e4683cc6d6315cf95b9ade8641defcb32372f1c126e398ef7a",
    "0x5a2dce0a8a7f68bb74560f8f71837c2c2ebbcbf7fffb42ae1896f13f7c7479a0",
    "0xb46a28b6f55540f89444f63de0378e3d121be09e06cc9ded1c20e65876d36aa0",
    "0xc65e9645644786b620e2dd2ad648ddfcbf4a7e5b1a3a4ecfe7f64667a3f0b7e2",
    "0xf4418588ed35a2458cffeb39b93d26f18d2ab13bdce6aee58e7b99359ec2dfd9",
    "0x5a9c16dc00d6ef18b7933a6f8dc65ccb55667138776f7dea101070dc8796e377",
    "0x4df84f40ae0c8229d0d6069e5c8f39a7c299677a09d367fc7b05e3bc380ee652",
    "0xcdc72595f74c7b1043d0e1ffbab734648c838dfb0527d971b602bc216c9619ef",
    "0x0abf5ac974a1ed57f4050aa510dd9c74f508277b39d7973bb2dfccc5eeb0618d",
    "0xb8cd74046ff337f0a7bf2c8e03e10f642c1886798d71806ab1e888d9e5ee87d0",
    "0x838c5655cb21c6cb83313b5a631175dff4963772cce9108188b34ac87c81c41e",
    "0x662ee4dd2dd7b2bc707961b1e646c4047669dcb6584f0d8d770daf5d7e7deb2e",
    "0x388ab20e2573d171a88108e79d820e98f26c0b84aa8b2f4aa4968dbb818ea322",
    "0x93237c50ba75ee485f4c22adf2f741400bdf8d6a9cc7df7ecae576221665d735",
    "0x8448818bb4ae4562849e949e17ac16e0be16688e156b5cf15e098c627c0056a9",
  ],
  merkleRoot: "0xd1eb21c855a643efa2b5f6e45c6e19784aeca4d4edfed71d16f1a4235a259aa1",
  index: 0,
};

export const MERKLE_PROOF_FALLBACK = {
  proof: [
    "0x0000000000000000000000000000000000000000000000000000000000000000",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0xb4c11951957c6f8f642c4af61cd6b24640fec6dc7fc607ee8206a99e92410d30",
    "0x21ddb9a356815c3fac1026b6dec5df3124afbadb485c9ba5a3e3398a04b7ba85",
    "0xe58769b32a1beaf1ea27375a44095a0d1fb664ce2dd358e7fcbfb78c26a19344",
    "0x0eb01ebfc9ed27500cd4dfc979272d1f0913cc9f66540d7e8005811109e1cf2d",
    "0x887c22bd8750d34016ac3c66b5ff102dacdd73f6b014e710b51e8022af9a1968",
    "0xffd70157e48063fc33c97a050f7f640233bf646cc98d9524c6b92bcf3ab56f83",
  ],
  merkleRoot: "0xcb9b5496c90542ac03009b37acf6ef8e8867856e2333e0eb9954290b9ce69272",
  index: 0,
};

export const MERKLE_PROOF_REENTRY = {
  proof: [
    "0x0000000000000000000000000000000000000000000000000000000000000000",
    "0xad3228b676f7d3cd4284a5443f17f1962b36e491b30a40b2405849e597ba5fb5",
    "0xb4c11951957c6f8f642c4af61cd6b24640fec6dc7fc607ee8206a99e92410d30",
    "0x21ddb9a356815c3fac1026b6dec5df3124afbadb485c9ba5a3e3398a04b7ba85",
    "0xe58769b32a1beaf1ea27375a44095a0d1fb664ce2dd358e7fcbfb78c26a19344",
    "0x0eb01ebfc9ed27500cd4dfc979272d1f0913cc9f66540d7e8005811109e1cf2d",
    "0x887c22bd8750d34016ac3c66b5ff102dacdd73f6b014e710b51e8022af9a1968",
    "0xffd70157e48063fc33c97a050f7f640233bf646cc98d9524c6b92bcf3ab56f83",
  ],
  merkleRoot: "0x494aab847e375519445b672ca9f5b4adac13ceb233c371978a18ab437483521b",
  index: 0,
};

// TODO CLEANUP TO MAKE THIS DYNAMIC AND NOT CONSTANT
export const Add_L1L2_Message_Hashes_Calldata_With_Empty_Array =
  "0xf4b476e10000000000000000000000000000000000000000000000000000000000000000";
export const Add_L1L2_Message_Hashes_Calldata_With_One_Hash =
  "0xf4b476e100000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000001f57d2ce8b7fc0df7ae7cbddaa706242a118bd8b649abccfecfb3f8e419729ca9";
export const Single_Item_L1L2_HashArray = ["0xf57d2ce8b7fc0df7ae7cbddaa706242a118bd8b649abccfecfb3f8e419729ca9"];

// TODO CLEANUP TO MAKE THIS DYNAMIC AND NOT CONSTANT
export const Add_L1L2_Message_Hashes_Calldata_With_Five_Hashes =
  "0xf4b476e100000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000005f887bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7fdd87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7faa87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7fcc87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f1187bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f";
export const L1L2_FiveHashes = [
  "0xf887bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f",
  "0xdd87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f",
  "0xaa87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f",
  "0xcc87bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f",
  "0x1187bbc07b0e849fb625aafadf4cb6b65b98e492fbb689705312bf1db98ead7f",
];
