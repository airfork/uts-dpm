package com.tunjicus.utsdpm.exceptions

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class GlobalAdviceHandler(private val request: HttpServletRequest) {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(GlobalAdviceHandler::class.java)
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
      ex: MethodArgumentNotValidException
  ): ResponseEntity<ExceptionResponses> {
    val messages = ex.allErrors.filter { it.defaultMessage != null }.map { it.defaultMessage!! }
    LOGGER.warn(messages.toString())
    return createExceptionResponse(messages)
  }

  @ExceptionHandler(
      NameNotFoundException::class,
      UserAlreadyExistsException::class,
      PasswordChangeException::class)
  fun handleUnprocessableExceptions(ex: RuntimeException): ResponseEntity<ExceptionResponse> {
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
      ManagerNotFoundException::class,
      SelfDeleteException::class,
      InvalidDpmGroupUpdateException::class)
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

  @ExceptionHandler(UserNotAuthorizedException::class)
  fun handleUserNotAuthorizedException(
      ex: UserNotAuthorizedException
  ): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.FORBIDDEN, ex.localizedMessage)
  }

  @ExceptionHandler(AuthenticationException::class)
  fun handleAuthenticationException(
      ex: AuthenticationException
  ): ResponseEntity<ExceptionResponse> {
    LOGGER.warn(ex.message)
    return createExceptionResponse(HttpStatus.UNAUTHORIZED, ex.localizedMessage)
  }

  // New handler for general 404s (no handler found)
  @ExceptionHandler(NoHandlerFoundException::class)
  fun handleNoHandlerFoundException(
      ex: NoHandlerFoundException
  ): ResponseEntity<ExceptionResponse> {
    LOGGER.warn("No handler found for {} {}", ex.httpMethod, ex.requestURL)
    val message = "No handler found for ${ex.httpMethod} ${ex.requestURL}"
    return createExceptionResponse(HttpStatus.NOT_FOUND, message)
  }

  private fun createExceptionResponse(messages: List<String>): ResponseEntity<ExceptionResponses> {
    val status = HttpStatus.BAD_REQUEST
    return ResponseEntity.status(status)
        .body(
            ExceptionResponses(
                status = status.value(),
                error = status.reasonPhrase,
                messages = messages,
                path = request.requestURI))
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
                path = request.requestURI))
  }
}
