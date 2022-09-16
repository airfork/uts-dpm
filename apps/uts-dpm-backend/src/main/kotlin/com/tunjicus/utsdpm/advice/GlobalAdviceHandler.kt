package com.tunjicus.utsdpm.advice

import com.tunjicus.utsdpm.exceptions.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class GlobalAdviceHandler(private val request: HttpServletRequest) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GlobalAdviceHandler::class.java)
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ExceptionResponses> {
    LOGGER.info("Method argument not valid exception")
    val messages = ex.allErrors.filter { it.defaultMessage != null }.map { it.defaultMessage!! }
    return createExceptionResponse(HttpStatus.BAD_REQUEST, messages)
  }

  @ExceptionHandler(UserNameNotFoundException::class)
  fun handleUserNameNotFoundException(ex: UserNameNotFoundException): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.localizedMessage)
  }

  @ExceptionHandler(DpmNotFoundException::class)
  fun handleDpmNotFoundException(ex: DpmNotFoundException): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.NOT_FOUND, ex.localizedMessage)
  }

  @ExceptionHandler(InvalidDataGenDateException::class)
  fun handleInvalidDataGenDateException(ex: InvalidDataGenDateException): ResponseEntity<ExceptionResponse> {
    LOGGER.info(ex.message)
    return createExceptionResponse(HttpStatus.BAD_REQUEST, ex.localizedMessage)
  }

  private fun createExceptionResponse(status: HttpStatus, messages: List<String>): ResponseEntity<ExceptionResponses> {
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

  private fun createExceptionResponse(status: HttpStatus, message: String): ResponseEntity<ExceptionResponse> {
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
