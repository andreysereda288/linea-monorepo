package net.consensys.linea.transactionexclusion.app.api

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.vertx.core.json.JsonObject
import net.consensys.encodeHex
import net.consensys.linea.async.get
import net.consensys.linea.jsonrpc.JsonRpcErrorResponse
import net.consensys.linea.jsonrpc.JsonRpcRequestListParams
import net.consensys.linea.jsonrpc.JsonRpcRequestMapParams
import net.consensys.linea.jsonrpc.JsonRpcSuccessResponse
import net.consensys.linea.transactionexclusion.ErrorType
import net.consensys.linea.transactionexclusion.TransactionExclusionError
import net.consensys.linea.transactionexclusion.TransactionExclusionServiceV1
import net.consensys.linea.transactionexclusion.defaultRejectedTransaction
import net.consensys.toHexString
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import tech.pegasys.teku.infrastructure.async.SafeFuture

class RequestHandlersTest {
  private lateinit var transactionExclusionServiceMock: TransactionExclusionServiceV1

  private val request = JsonRpcRequestMapParams(
    "2.0",
    "1",
    "linea_saveRejectedTransactionV1",
    mapOf(
      "txRejectionStage" to "SEQUENCER",
      "timestamp" to "2024-09-05T09:22:52Z",
      "transactionRLP" to defaultRejectedTransaction.transactionRLP.encodeHex(),
      "reasonMessage" to defaultRejectedTransaction.reasonMessage,
      "overflows" to
        "[{\"module\":\"ADD\",\"count\":402,\"limit\":70},{\"module\":\"MUL\",\"count\":587,\"limit\":400}]"
    )
  )

  @BeforeEach
  fun beforeEach() {
    transactionExclusionServiceMock = mock<TransactionExclusionServiceV1>(
      defaultAnswer = Mockito.RETURNS_DEEP_STUBS
    )
  }

  @Test
  fun SaveRejectedTransactionRequestHandlerV1_invoke_acceptsValidRequestMap() {
    whenever(transactionExclusionServiceMock.saveRejectedTransaction(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Ok(TransactionExclusionServiceV1.SaveRejectedTransactionStatus.SAVED)
        )
      )

    val saveRequestHandlerV1 = SaveRejectedTransactionRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonObject()
      .put("status", TransactionExclusionServiceV1.SaveRejectedTransactionStatus.SAVED)
      .put("txHash", defaultRejectedTransaction.transactionInfo!!.hash.encodeHex())
      .let {
        JsonRpcSuccessResponse(request.id, it)
      }

    val result = saveRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.get())
  }

  @Test
  fun SaveRejectedTransactionRequestHandlerV1_invoke_acceptsValidRequestMap_without_blockNumber() {
    whenever(transactionExclusionServiceMock.saveRejectedTransaction(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Ok(TransactionExclusionServiceV1.SaveRejectedTransactionStatus.SAVED)
        )
      )

    val saveTxRequestHandlerV1 = SaveRejectedTransactionRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonObject()
      .put("status", TransactionExclusionServiceV1.SaveRejectedTransactionStatus.SAVED)
      .put("txHash", defaultRejectedTransaction.transactionInfo!!.hash.encodeHex())
      .let {
        JsonRpcSuccessResponse(request.id, it)
      }

    val result = saveTxRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.get())
  }

  @Test
  fun SaveRejectedTransactionRequestHandlerV1_invoke_return_success_result_with_duplicate_status() {
    whenever(transactionExclusionServiceMock.saveRejectedTransaction(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Ok(TransactionExclusionServiceV1.SaveRejectedTransactionStatus.DUPLICATE_ALREADY_SAVED_BEFORE)
        )
      )

    val saveTxRequestHandlerV1 = SaveRejectedTransactionRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonObject()
      .put("status", TransactionExclusionServiceV1.SaveRejectedTransactionStatus.DUPLICATE_ALREADY_SAVED_BEFORE)
      .put("txHash", defaultRejectedTransaction.transactionInfo!!.hash.encodeHex())
      .let {
        JsonRpcSuccessResponse(request.id, it)
      }

    val result = saveTxRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.get())
  }

  @Test
  fun SaveRejectedTransactionRequestHandlerV1_invoke_return_failure_result() {
    whenever(transactionExclusionServiceMock.saveRejectedTransaction(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Err(TransactionExclusionError(ErrorType.OTHER_ERROR, ""))
        )
      )

    val saveTxRequestHandlerV1 = SaveRejectedTransactionRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonRpcErrorResponse(
      request.id,
      jsonRpcError(
        TransactionExclusionError(
          ErrorType.OTHER_ERROR,
          ""
        )
      )
    )

    val result = saveTxRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.getError())
  }

  @Test
  fun GetTransactionExclusionStatusRequestHandlerV1_invoke_acceptsValidRequestList() {
    whenever(transactionExclusionServiceMock.getTransactionExclusionStatus(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Ok(defaultRejectedTransaction)
        )
      )

    val request = JsonRpcRequestListParams(
      "2.0",
      "1",
      "linea_getTransactionExclusionStatusV1",
      listOf(
        defaultRejectedTransaction.transactionInfo!!.hash.encodeHex()
      )
    )

    val getTxStatusRequestHandlerV1 = GetTransactionExclusionStatusRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonObject()
      .put("txHash", defaultRejectedTransaction.transactionInfo!!.hash.encodeHex())
      .put("from", defaultRejectedTransaction.transactionInfo!!.from.encodeHex())
      .put("nonce", defaultRejectedTransaction.transactionInfo!!.nonce.toHexString())
      .put("txRejectionStage", defaultRejectedTransaction.txRejectionStage.name)
      .put("reasonMessage", defaultRejectedTransaction.reasonMessage)
      .put("timestamp", defaultRejectedTransaction.timestamp.toString())
      .put("blockNumber", defaultRejectedTransaction.blockNumber!!.toHexString())
      .let {
        JsonRpcSuccessResponse(request.id, it)
      }

    val result = getTxStatusRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.get())
  }

  @Test
  fun GetTransactionExclusionStatusRequestHandlerV1_invoke_return_failure_result() {
    whenever(transactionExclusionServiceMock.getTransactionExclusionStatus(any()))
      .thenReturn(
        SafeFuture.completedFuture(
          Err(
            TransactionExclusionError(
              ErrorType.TRANSACTION_UNAVAILABLE,
              "Cannot find the rejected transaction"
            )
          )
        )
      )

    val request = JsonRpcRequestListParams(
      "2.0",
      "1",
      "linea_getTransactionExclusionStatusV1",
      listOf(
        defaultRejectedTransaction.transactionInfo!!.hash.encodeHex()
      )
    )

    val getTxStatusRequestHandlerV1 = GetTransactionExclusionStatusRequestHandlerV1(
      transactionExclusionServiceMock
    )

    val expectedResult = JsonRpcErrorResponse(
      request.id,
      jsonRpcError(
        TransactionExclusionError(
          ErrorType.TRANSACTION_UNAVAILABLE,
          "Cannot find the rejected transaction"
        )
      )
    )

    val result = getTxStatusRequestHandlerV1.invoke(
      user = null,
      request = request,
      requestJson = JsonObject()
    ).get()

    Assertions.assertEquals(expectedResult, result.getError())
  }
}
