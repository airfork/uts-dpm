package com.tunjicus.utsdpm.advice

import com.tunjicus.utsdpm.exceptions.*
import javax.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalAdviceHandler(private val request: HttpServletRequest) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GlobalAdviceHandler::class.java)
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
    ex: MethodArgumentNotValidException
  ): ResponseEntity<ExceptionResponses> {
    LOGGER.info("Method argument not valid exception")
    val messages = ex.allErrors.filter { it.defaultMessage != null }.map { it.defaultMessage!! }
    return createExceptionResponse(messages)
  }

  @ExceptionHandler(NameNotFoundException::class)
  fun handleUserNameNotFoundException(
    ex: NameNotFoundException
  ): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.localizedMessage)
  }

  @ExceptionHandler(DpmNotFoundException::class, UserNotFoundException::class)
  fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.NOT_FOUND, ex.localizedMessage)
  }

  @ExceptionHandler(
    InvalidDataGenDateException::class,
    UserRoleNotFoundException::class,
    ManagerNotFoundException::class
  )
  fun handleBadRequestExceptions(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.BAD_REQUEST, ex.localizedMessage)
  }

  @ExceptionHandler(AutogenException::class)
  fun handleAutogenException(ex: AutogenException): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.localizedMessage)
  }

  @ExceptionHandler(AutoSubmitAlreadyCalledException::class)
  fun handleAutoSubmitAlreadyCalledException(
    ex: AutoSubmitAlreadyCalledException
  ): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.CONFLICT, ex.localizedMessage)
  }

  private fun createExceptionResponse(messages: List<String>): ResponseEntity<ExceptionResponses> {
    val status = HttpStatus.BAD_REQUEST
    return ResponseEntity.status(status)
      .body(
        ExceptionResponses(
          status = status.value(),
          error = status.reasonPhrase,
          messages = messages,
          path = request.requestURI
        )
      )
  }

  private fun createExceptionResponse(
    status: HttpStatus,
    message: String
  ): ResponseEntity<ExceptionResponse> {
    return ResponseEntity.status(status)
      .body(
        ExceptionResponse(
          status = status.value(),
          error = status.reasonPhrase,
          message = message,
          path = request.requestURI
        )
      )
  }
}
