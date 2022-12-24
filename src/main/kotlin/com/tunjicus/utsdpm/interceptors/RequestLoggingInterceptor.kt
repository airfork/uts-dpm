package com.tunjicus.utsdpm.interceptors

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.lang.Exception
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse

@Component
class RequestLoggingInterceptor : HandlerInterceptor {
  companion object {
    private val LOGGER = LoggerFactory.getLogger(RequestLoggingInterceptor::class.java)
  }

  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    request.setAttribute("startTime", System.currentTimeMillis())
    return true
  }

  override fun afterCompletion(
    request: HttpServletRequest,
    response: HttpServletResponse,
    handler: Any,
    ex: Exception?
  ) {
    val startTime = request.getAttribute("startTime") as Long
    val timeTaken = System.currentTimeMillis() - startTime
    LOGGER.info("method=${request.method} path=${request.requestURI} time=${timeTaken}ms")
  }
}
