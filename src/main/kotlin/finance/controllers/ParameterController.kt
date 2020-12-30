package finance.controllers

import com.fasterxml.jackson.databind.ObjectMapper
import finance.domain.Parameter
import finance.services.ParameterService
import org.apache.logging.log4j.LogManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import java.util.*
import javax.validation.ConstraintViolationException
import javax.validation.ValidationException

@CrossOrigin
@RestController
@RequestMapping("/parm")
class ParameterController(private var parameterService: ParameterService) {

    //https://hornsup:8080/parm/select/payment_account
    @GetMapping(path = ["/select/{parameterName}"], produces = ["application/json"])
    fun selectParameter(@PathVariable parameterName: String): ResponseEntity<Parameter> {
        val parameterOptional: Optional<Parameter> = parameterService.findByParameter(parameterName)
        if (!parameterOptional.isPresent) {
            logger.error("no parameter found.")
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "could not find the parm.")
        }
        return ResponseEntity.ok(parameterOptional.get())
    }

    //curl --header "Content-Type: application/json" -X POST -d '{"parm":"test"}' http://localhost:8080/parm/insert
    @PostMapping(path = ["/insert"], produces = ["application/json"])
    fun insertParameter(@RequestBody parameter: Parameter): ResponseEntity<String> {
        parameterService.insertParameter(parameter)
        logger.debug("insertParameter")
        return ResponseEntity.ok("parameter inserted")
    }

    @DeleteMapping(path = ["/delete/{parameterName}"], produces = ["application/json"])
    fun deleteByParameterName(@PathVariable parameterName: String): ResponseEntity<String> {
        parameterService.deleteByParameterName(parameterName)
        return ResponseEntity.ok("parameter deleted")
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    @ExceptionHandler(
        value = [ConstraintViolationException::class, NumberFormatException::class,
            MethodArgumentTypeMismatchException::class, HttpMessageNotReadableException::class, ValidationException::class]
    )
    fun handleBadHttpRequests(throwable: Throwable): Map<String, String> {
        val response: MutableMap<String, String> = HashMap()
        logger.error("Bad Request", throwable)
        response["response"] = "BAD_REQUEST: " + throwable.javaClass.simpleName + " , message: " + throwable.message
        return response
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [Exception::class])
    fun handleHttpInternalError(throwable: Throwable): Map<String, String> {
        val response: MutableMap<String, String> = HashMap()
        logger.error("internal server error: ", throwable)
        response["response"] =
            "INTERNAL_SERVER_ERROR: " + throwable.javaClass.simpleName + " , message: " + throwable.message
        logger.info("response: $response")
        return response
    }

    companion object {
        private val mapper = ObjectMapper()
        private val logger = LogManager.getLogger()
    }
}
