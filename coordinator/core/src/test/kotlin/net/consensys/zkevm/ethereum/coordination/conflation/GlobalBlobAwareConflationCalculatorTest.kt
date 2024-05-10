package net.consensys.zkevm.ethereum.coordination.conflation

import kotlinx.datetime.Instant
import net.consensys.FakeFixedClock
import net.consensys.linea.traces.fakeTracesCounters
import net.consensys.zkevm.domain.Blob
import net.consensys.zkevm.domain.BlockCounters
import net.consensys.zkevm.domain.ConflationCalculationResult
import net.consensys.zkevm.domain.ConflationTrigger
import net.consensys.zkevm.ethereum.coordination.blob.BlobCompressor
import net.consensys.zkevm.ethereum.coordination.blob.FakeBlobCompressor
import net.consensys.zkevm.ethereum.coordination.blockcreation.BlockHeaderSummary
import net.consensys.zkevm.ethereum.coordination.blockcreation.SafeBlockProvider
import org.apache.tuweni.bytes.Bytes32
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import tech.pegasys.teku.infrastructure.async.SafeFuture
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class GlobalBlobAwareConflationCalculatorTest {
  // NOTE: this breaks the test isolation, but adds some confidence that the integration works
  private lateinit var blobCompressor: BlobCompressor
  private lateinit var calculatorByDealine: ConflationCalculatorByTimeDeadline
  private lateinit var calculatorByDataCompressed: ConflationCalculatorByDataCompressed
  private lateinit var calculatorByTraces: ConflationCalculator
  private lateinit var globalCalculator: GlobalBlockConflationCalculator
  private lateinit var calculator: GlobalBlobAwareConflationCalculator
  private val lastBlockNumber: ULong = 0uL
  private lateinit var safeBlockProvider: SafeBlockProvider
  private lateinit var fakeClock: FakeFixedClock
  private val fakeClockTime = Instant.parse("2023-12-11T00:00:00.000Z")
  private val blockTime = 6.seconds
  private lateinit var conflations: MutableList<ConflationCalculationResult>
  private lateinit var blobs: MutableList<Blob>
  private val defaultBatchesLimit = 2U

  @BeforeEach
  fun beforeEach() {
    fakeClock = FakeFixedClock(fakeClockTime)
    safeBlockProvider = mock<SafeBlockProvider> {
      on { getLatestSafeBlock() }.thenReturn(
        SafeFuture.failedFuture(RuntimeException("getLatestSafeBlock should not be called"))
      )
      on { getLatestSafeBlockHeader() }.thenReturn(
        SafeFuture.failedFuture(RuntimeException("getLatestSafeBlockHeader not mocked yet"))
      )
    }
    calculatorByDealine = spy(
      ConflationCalculatorByTimeDeadline(
        config = ConflationCalculatorByTimeDeadline.Config(
          conflationDeadline = 2.seconds,
          conflationDeadlineLastBlockConfirmationDelay = 10.milliseconds
        ),
        lastBlockNumber = lastBlockNumber,
        latestBlockProvider = safeBlockProvider
      )
    )
    blobCompressor = spy<BlobCompressor>(FakeBlobCompressor(dataLimit = 100, fakeCompressionRatio = 1.0))
    calculatorByDataCompressed = ConflationCalculatorByDataCompressed(blobCompressor = blobCompressor)
    calculatorByTraces = ConflationCalculatorByExecutionTraces(
      tracesCountersLimit = fakeTracesCounters(100u)
    )
    globalCalculator = GlobalBlockConflationCalculator(
      lastBlockNumber = lastBlockNumber,
      syncCalculators = listOf(calculatorByTraces, calculatorByDataCompressed),
      deferredTriggerConflationCalculators = listOf(calculatorByDealine)
    )
    calculator = GlobalBlobAwareConflationCalculator(
      conflationCalculator = globalCalculator,
      blobCalculator = calculatorByDataCompressed,
      batchesLimit = defaultBatchesLimit
    )
    conflations = mutableListOf()
    blobs = mutableListOf()
    calculator.onConflatedBatch { trigger ->
      conflations.add(trigger)
      SafeFuture.completedFuture(Unit)
    }
    calculator.onBlobCreation { blob ->
      blobs.add(blob)
      SafeFuture.completedFuture(Unit)
    }
  }

  @Test
  fun `when compressor is full, it should emit conflation and blob events - happy path`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 10u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 10u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 10u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(83),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 10u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(44),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    // block 3 goes over data limit, so it should emit conflation and blob events
    calculator.newBlock(block3Counters)
    // block 4 goes over data limit, so it should emit conflation and blob events
    calculator.newBlock(block4Counters)
    assertThat(calculator.lastBlockNumber).isEqualTo(4uL)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 2uL,
          tracesCounters = fakeTracesCounters(20u),
          dataL1Size = 23u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 3uL,
          endBlockNumber = 3uL,
          tracesCounters = fakeTracesCounters(10u),
          dataL1Size = 83u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block2Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(1, 2))
    assertThat(blobs[1].startBlockTime).isEqualTo(block3Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block3Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full, it should emit conflation and blob events - traces oversized`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(90u)
    )
    // over sized block
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(200u)
    )
    // blob size is 0 bytes up to this point (fake compression, limit 100)
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // blob size is 15 bytes up to this point (fake compression, limit 100)
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(61),
      tracesCounters = fakeTracesCounters(10u)
    )
    // block 7 does not fit on top of 6, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)
    calculator.newBlock(block6Counters)
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 2uL,
          tracesCounters = fakeTracesCounters(20u),
          dataL1Size = 23u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 3uL,
          endBlockNumber = 3uL,
          tracesCounters = fakeTracesCounters(90u),
          dataL1Size = 13u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 4uL,
          endBlockNumber = 4uL,
          tracesCounters = fakeTracesCounters(200u),
          dataL1Size = 14u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 5uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(20u),
          dataL1Size = 76u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 2))
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block3Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(2, 4))
    assertThat(blobs[1].startBlockTime).isEqualTo(block4Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full right after time limit, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // blob size is 65 bytes up to this point (fake compression, limit 100)
    // block 6 does not fit, so it should emit conflation and blob events
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(61),
      tracesCounters = fakeTracesCounters(30u)
    )
    // block 7 does not fit on top of 6, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)

    // will trigger deadline overflow
    fakeClock.advanceBy(2.days)
    whenever(safeBlockProvider.getLatestSafeBlockHeader()).thenReturn(
      SafeFuture.completedFuture(
        BlockHeaderSummary(
          number = block5Counters.blockNumber,
          hash = Bytes32.random(),
          timestamp = block5Counters.blockTimestamp
        )
      )
    )
    calculatorByDealine.checkConflationDeadline()

    // will trigger blob compressed data limit overflow
    calculator.newBlock(block6Counters)
    // will trigger blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.TIME_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(30u),
          dataL1Size = 61u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].compressedData.size).isEqualTo(65) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block5Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(1, 2))
    assertThat(blobs[1].compressedData.size).isEqualTo(61) // sum of dataL1Size in conflations
    assertThat(blobs[1].startBlockTime).isEqualTo(block6Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full right after traces limit, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // traces limit will be triggered
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(16),
      tracesCounters = fakeTracesCounters(60u)
    )
    // blob size is 71 bytes up to this point (fake compression, limit 100)
    // block 7 does not fit, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block6Counters)
    // will trigger blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(60u),
          dataL1Size = 16u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(1)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 2))
    assertThat(blobs[0].compressedData.size).isEqualTo(81) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when blob batch limit is reached on traces limit, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(50u)
    )
    // traces limit will be triggered
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(100u)
    )
    // traces limit will be triggered
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(90u)
    )
    // traces limit will be triggered
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(100u)
    )
    // traces limit will be triggered
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(50u)
    )
    // traces limit will be triggered and blob batch limit will be triggered
    // as well since there are three pending batches now in the blob aware
    // calculator
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(16),
      tracesCounters = fakeTracesCounters(60u)
    )

    calculator.newBlock(block1Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block2Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block3Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block4Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block5Counters)
    // will trigger traces limit overflow
    calculator.newBlock(block6Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 1uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 11u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 2uL,
          endBlockNumber = 2uL,
          tracesCounters = fakeTracesCounters(100u),
          dataL1Size = 12u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 3uL,
          endBlockNumber = 3uL,
          tracesCounters = fakeTracesCounters(90u),
          dataL1Size = 13u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 4uL,
          endBlockNumber = 4uL,
          tracesCounters = fakeTracesCounters(100u),
          dataL1Size = 14u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 5uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 15u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 2))
    assertThat(blobs[0].compressedData.size).isEqualTo(23) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block2Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(2, 4))
    assertThat(blobs[1].compressedData.size).isEqualTo(27) // sum of dataL1Size in conflations
    assertThat(blobs[1].startBlockTime).isEqualTo(block3Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block4Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full right after over-sized traces limit, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // over-sized block traces limit will be triggered
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(16),
      tracesCounters = fakeTracesCounters(200u)
    )
    // blob size is 71 bytes up to this point (fake compression, limit 100)
    // block 7 does not fit, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)
    // will trigger single over-sized block traces limit overflow
    calculator.newBlock(block6Counters)
    // will trigger blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(200u),
          dataL1Size = 16u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(1)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 2))
    assertThat(blobs[0].compressedData.size).isEqualTo(81) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full with traces and data limit overflow, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // traces limit and data limit will be triggered
    // blob size is 55 bytes up to this point (fake compression, limit 100)
    // block 6 does not fit, so it should emit conflation and blob events
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(61),
      tracesCounters = fakeTracesCounters(60u)
    )
    // blob size is 61 bytes up to this point (fake compression, limit 100)
    // block 7 does not fit, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)
    // will trigger both traces and blob compressed data limit overflow
    calculator.newBlock(block6Counters)
    // will trigger another blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(60u),
          dataL1Size = 61u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].compressedData.size).isEqualTo(65) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block5Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(1, 2))
    assertThat(blobs[1].compressedData.size).isEqualTo(61) // sum of dataL1Size in conflations
    assertThat(blobs[1].startBlockTime).isEqualTo(block6Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full with over-sized traces and data overflow, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // over-sized traces limit and data limit will be triggered
    // blob size is 55 bytes up to this point (fake compression, limit 100)
    // block 6 does not fit, so it should emit conflation and blob events
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(61),
      tracesCounters = fakeTracesCounters(200u)
    )
    // blob size is 61 bytes up to this point (fake compression, limit 100)
    // block 7 does not fit, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)
    // will trigger both over-sized traces and blob compressed data limit overflow
    calculator.newBlock(block6Counters)
    // will trigger another blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(200u),
          dataL1Size = 61u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].compressedData.size).isEqualTo(65) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block5Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(1, 2))
    assertThat(blobs[1].compressedData.size).isEqualTo(61) // sum of dataL1Size in conflations
    assertThat(blobs[1].startBlockTime).isEqualTo(block6Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when compressor is full with multiple limit overflows, it should emit conflation and blob events`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block4Counters = BlockCounters(
      blockNumber = 4uL,
      l1DataSize = 41u,
      blockTimestamp = block3Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(14),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block5Counters = BlockCounters(
      blockNumber = 5uL,
      l1DataSize = 51u,
      blockTimestamp = block4Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(15),
      tracesCounters = fakeTracesCounters(10u)
    )
    // over-sized block traces limit and data limit will be triggered
    // blob size is 55 bytes up to this point (fake compression, limit 100)
    // block 6 does not fit, so it should emit conflation and blob events
    val block6Counters = BlockCounters(
      blockNumber = 6uL,
      l1DataSize = 61u,
      blockTimestamp = block5Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(61),
      tracesCounters = fakeTracesCounters(200u)
    )
    // blob size is 61 bytes up to this point (fake compression, limit 100)
    // block 7 does not fit, so it should emit conflation and blob events
    val block7Counters = BlockCounters(
      blockNumber = 7uL,
      l1DataSize = 71u,
      blockTimestamp = block6Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(71),
      tracesCounters = fakeTracesCounters(10u)
    )

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)
    calculator.newBlock(block4Counters)
    calculator.newBlock(block5Counters)

    // will trigger deadline overflow
    fakeClock.advanceBy(2.days)
    whenever(safeBlockProvider.getLatestSafeBlockHeader()).thenReturn(
      SafeFuture.completedFuture(
        BlockHeaderSummary(
          number = block5Counters.blockNumber,
          hash = Bytes32.random(),
          timestamp = block5Counters.blockTimestamp
        )
      )
    )
    calculatorByDealine.checkConflationDeadline()

    // will trigger both over-sized traces and blob compressed data limit overflow
    calculator.newBlock(block6Counters)
    // will trigger another blob compressed data limit overflow
    calculator.newBlock(block7Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 5uL,
          tracesCounters = fakeTracesCounters(50u),
          dataL1Size = 65u,
          conflationTrigger = ConflationTrigger.TIME_LIMIT
        ),
        ConflationCalculationResult(
          startBlockNumber = 6uL,
          endBlockNumber = 6uL,
          tracesCounters = fakeTracesCounters(200u),
          dataL1Size = 61u,
          conflationTrigger = ConflationTrigger.DATA_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(2)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].compressedData.size).isEqualTo(65) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block5Counters.blockTimestamp)
    assertThat(blobs[1].conflations).isEqualTo(conflations.subList(1, 2))
    assertThat(blobs[1].compressedData.size).isEqualTo(61) // sum of dataL1Size in conflations
    assertThat(blobs[1].startBlockTime).isEqualTo(block6Counters.blockTimestamp)
    assertThat(blobs[1].endBlockTime).isEqualTo(block6Counters.blockTimestamp)
  }

  @Test
  fun `when batch is triggered should check if current block fits into the blob`() {
    val block1Counters = BlockCounters(
      blockNumber = 1uL,
      l1DataSize = 11u,
      blockTimestamp = fakeClockTime,
      blockRLPEncoded = ByteArray(11),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block2Counters = BlockCounters(
      blockNumber = 2uL,
      l1DataSize = 21u,
      blockTimestamp = block1Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(12),
      tracesCounters = fakeTracesCounters(10u)
    )
    val block3Counters = BlockCounters(
      blockNumber = 3uL,
      l1DataSize = 31u,
      blockTimestamp = block2Counters.blockTimestamp.plus(blockTime),
      blockRLPEncoded = ByteArray(13),
      tracesCounters = fakeTracesCounters(90u)
    )

    whenever(blobCompressor.canAppendBlock(block3Counters.blockRLPEncoded))
      .thenReturn(true) // first check can be appended
      .thenReturn(false) // 2nd check cannot be appended after batch was triggered

    calculator.newBlock(block1Counters)
    calculator.newBlock(block2Counters)
    calculator.newBlock(block3Counters)

    assertThat(conflations).isEqualTo(
      listOf(
        ConflationCalculationResult(
          startBlockNumber = 1uL,
          endBlockNumber = 2uL,
          tracesCounters = fakeTracesCounters(20u),
          dataL1Size = 23u,
          conflationTrigger = ConflationTrigger.TRACES_LIMIT
        )
      )
    )

    assertThat(blobs).hasSize(1)
    assertThat(blobs[0].conflations).isEqualTo(conflations.subList(0, 1))
    assertThat(blobs[0].compressedData.size).isEqualTo(23) // sum of dataL1Size in conflations
    assertThat(blobs[0].startBlockTime).isEqualTo(block1Counters.blockTimestamp)
    assertThat(blobs[0].endBlockTime).isEqualTo(block2Counters.blockTimestamp)
  }
}
