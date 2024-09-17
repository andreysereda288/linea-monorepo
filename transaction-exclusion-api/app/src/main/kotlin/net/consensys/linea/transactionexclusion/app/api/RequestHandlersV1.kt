package net.consensys.linea.transactionexclusion.app.api

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.flatMap
import com.github.michaelbull.result.get
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.User
import net.consensys.encodeHex
import net.consensys.linea.async.toVertxFuture
import net.consensys.linea.jsonrpc.JsonRpcErrorResponse
import net.consensys.linea.jsonrpc.JsonRpcRequest
import net.consensys.linea.jsonrpc.JsonRpcRequestHandler
import net.consensys.linea.jsonrpc.JsonRpcRequestListParams
import net.consensys.linea.jsonrpc.JsonRpcRequestMapParams
import net.consensys.linea.jsonrpc.JsonRpcSuccessResponse
import net.consensys.linea.transactionexclusion.RejectedTransaction
import net.consensys.linea.transactionexclusion.TransactionExclusionServiceV1
import net.consensys.linea.transactionexclusion.dto.RejectedTransactionJsonDto
import net.consensys.toHexString

private fun validateParams(request: JsonRpcRequest): Result<JsonRpcRequest, JsonRpcErrorResponse> {
  if (request.params !is Map<*, *> && request.params !is List<*>) {
    return Err(
      JsonRpcErrorResponse.invalidParams(
        request.id,
        "params should be either an object or a list"
      )
    )
  }
  return try {
    if (request.params is Map<*, *>) {
      validateMapParams(request)
    } else if (request.params is List<*>) {
      validateListParams(request)
    }
    Ok(request)
  } catch (e: Exception) {
    Err(JsonRpcErrorResponse.invalidRequest())
  }
}

private fun validateMapParams(request: JsonRpcRequest): Result<JsonRpcRequest, JsonRpcErrorResponse> {
  if (request.params !is Map<*, *>) {
    return Err(
      JsonRpcErrorResponse.invalidParams(
        request.id,
        "params should be an object"
      )
    )
  }
  return try {
    if (request.params is Map<*, *>) {
      val jsonRpcRequest = request as JsonRpcRequestMapParams
      if (jsonRpcRequest.params.isEmpty()) {
        return Err(
          JsonRpcErrorResponse.invalidParams(
            request.id,
            "Parameters map is empty!"
          )
        )
      }
    }
    Ok(request)
  } catch (e: Exception) {
    Err(JsonRpcErrorResponse.invalidRequest())
  }
}

private fun validateListParams(request: JsonRpcRequest): Result<JsonRpcRequest, JsonRpcErrorResponse> {
  if (request.params !is List<*>) {
    return Err(
      JsonRpcErrorResponse.invalidParams(
        request.id,
        "params should be a list"
      )
    )
  }
  return try {
    if (request.params is List<*>) {
      val jsonRpcRequest = request as JsonRpcRequestListParams
      if (jsonRpcRequest.params.isEmpty()) {
        return Err(
          JsonRpcErrorResponse.invalidParams(
            request.id,
            "Parameters list is empty!"
          )
        )
      }
    }
    Ok(request)
  } catch (e: Exception) {
    Err(JsonRpcErrorResponse.invalidRequest())
  }
}

class SaveRejectedTransactionRequestHandlerV1(
  private val transactionExclusionService: TransactionExclusionServiceV1
) : JsonRpcRequestHandler {
  enum class RequestParams(val paramName: String) {
    TX_REJECTION_STAGE("txRejectionStage"),
    TIMESTAMP("timestamp"),
    REASON_MESSAGE("reasonMessage"),
    TRANSACTION_RLP("transactionRLP"),
    BLOCK_NUMBER("blockNumber"),
    OVERFLOWS("overflows")
  }

  private fun validateMapParamsPresence(requestMapParams: Map<*, *>) {
    RequestParams.entries
      .filter { requestParam ->
        requestParam != RequestParams.BLOCK_NUMBER && requestMapParams[requestParam.paramName] == null
      }
      .run {
        if (this.isNotEmpty()) {
          throw IllegalArgumentException(
            "Missing ${this.joinToString(",", "[", "]") { it.paramName }} " +
              "from the given request params"
          )
        }
      }
  }

  private fun parseMapParamsToRejectedTransaction(requestMapParams: Map<*, *>): RejectedTransaction {
    return validateMapParamsPresence(requestMapParams)
      .run {
        RejectedTransactionJsonDto(
          txRejectionStage = requestMapParams[RequestParams.TX_REJECTION_STAGE.paramName].toString(),
          timestamp = requestMapParams[RequestParams.TIMESTAMP.paramName].toString(),
          blockNumber = requestMapParams[RequestParams.BLOCK_NUMBER.paramName]?.toString(),
          transactionRLP = requestMapParams[RequestParams.TRANSACTION_RLP.paramName].toString(),
          reasonMessage = requestMapParams[RequestParams.REASON_MESSAGE.paramName].toString(),
          overflows = requestMapParams[RequestParams.OVERFLOWS.paramName]!!
        ).toDomainObject()
      }
  }

  private fun parseListParamsToRejectedTransaction(requestMapParams: List<Any?>): RejectedTransaction {
    if (requestMapParams.isEmpty() || requestMapParams[0] !is Map<*, *>) {
      throw IllegalArgumentException(
        "The size of the given request params list should not be empty " +
          "or the first param should be an object"
      )
    }
    return parseMapParamsToRejectedTransaction(requestMapParams[0] as Map<*, *>)
  }

  override fun invoke(
    user: User?,
    request: JsonRpcRequest,
    requestJson: JsonObject
  ): Future<Result<JsonRpcSuccessResponse, JsonRpcErrorResponse>> {
    val rejectedTransaction = try {
      val parsingResult = validateParams(request).flatMap { validatedRequest ->
        val parsedRejectedTransaction =
          when (validatedRequest) {
            is JsonRpcRequestMapParams -> parseMapParamsToRejectedTransaction(validatedRequest.params)
            is JsonRpcRequestListParams -> parseListParamsToRejectedTransaction(validatedRequest.params)
            else -> throw IllegalStateException(
              "JsonRpcRequest should be as JsonRpcRequestMapParams or JsonRpcRequestListParams"
            )
          }
        Ok(parsedRejectedTransaction)
      }
      if (parsingResult is Err) {
        return Future.succeededFuture(parsingResult)
      } else {
        parsingResult.get()!!
      }
    } catch (e: Exception) {
      return Future.succeededFuture(
        Err(
          JsonRpcErrorResponse.invalidParams(
            request.id,
            e.message
          )
        )
      )
    }

    return transactionExclusionService
      .saveRejectedTransaction(rejectedTransaction)
      .thenApply { result ->
        result.map {
          val rpcResult =
            JsonObject()
              .put("status", it.name)
              .put("txHash", rejectedTransaction.transactionInfo.hash.encodeHex())
          JsonRpcSuccessResponse(request.id, rpcResult)
        }.mapError { error ->
          JsonRpcErrorResponse(request.id, jsonRpcError(error))
        }
      }.toVertxFuture()
  }
}

class GetTransactionExclusionStatusRequestHandlerV1(
  private val transactionExclusionService: TransactionExclusionServiceV1
) : JsonRpcRequestHandler {
  private fun parseListParamsToTxHash(validatedRequest: JsonRpcRequestListParams): ByteArray {
    return ArgumentParser.getTxHashInRawBytes(validatedRequest.params[0].toString())
  }

  override fun invoke(
    user: User?,
    request: JsonRpcRequest,
    requestJson: JsonObject
  ): Future<Result<JsonRpcSuccessResponse, JsonRpcErrorResponse>> {
    val txHash = try {
      val parsingResult = validateListParams(request).flatMap { validatedRequest ->
        val parsedTxHash =
          when (validatedRequest) {
            is JsonRpcRequestListParams -> parseListParamsToTxHash(validatedRequest)
            else -> throw IllegalStateException("JsonRpcRequest should be as JsonRpcRequestListParams")
          }
        Ok(parsedTxHash)
      }
      if (parsingResult is Err) {
        return Future.succeededFuture(parsingResult)
      } else {
        parsingResult.get()!!
      }
    } catch (e: Exception) {
      return Future.succeededFuture(
        Err(
          JsonRpcErrorResponse.invalidParams(
            request.id,
            e.message
          )
        )
      )
    }

    return transactionExclusionService
      .getTransactionExclusionStatus(txHash)
      .thenApply { result ->
        result.map {
          val rpcResult = if (it == null) { null } else {
            JsonObject()
              .put("txHash", it.transactionInfo.hash.encodeHex())
              .put("from", it.transactionInfo.from.encodeHex())
              .put("nonce", it.transactionInfo.nonce.toHexString())
              .put("txRejectionStage", it.txRejectionStage.name)
              .put("reasonMessage", it.reasonMessage)
              .put("timestamp", it.timestamp.toString())
              .also { jsonObject ->
                if (it.blockNumber != null) {
                  jsonObject.put("blockNumber", it.blockNumber!!.toHexString())
                }
              }
          }
          JsonRpcSuccessResponse(request.id, rpcResult)
        }.mapError { error ->
          JsonRpcErrorResponse(request.id, jsonRpcError(error))
        }
      }.toVertxFuture()
  }
}
