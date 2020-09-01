package finance.controllers

import finance.domain.Category
import finance.services.CategoryService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.annotation.*
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import java.util.*
import javax.validation.ConstraintViolationException

@CrossOrigin
@RestController
@RequestMapping("/category")
//@Validated
class CategoryController (private var categoryService: CategoryService) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    //curl --header "Content-Type: application/json" -X POST -d '{"category":"test"}' http://localhost:8080/category/insert
    @PostMapping(path = ["/insert"], produces = ["application/json"])
    fun insertCategory(@RequestBody category: Category): ResponseEntity<String> {
        categoryService.insertCategory(category)
        logger.info("insertCategory")
        return ResponseEntity.ok("category inserted")
    }

    @DeleteMapping(path = ["/delete/{categoryName}"], produces = ["application/json"])
    fun deleteByCategoryName(@PathVariable categoryName: String): ResponseEntity<String> {
        //val paymentOptional: Optional<Payment> = categoryService.findByPaymentId(paymentId)

        //logger.info("deleteByPaymentId controller - $paymentId")
        //if (paymentOptional.isPresent) {
            categoryService.deleteByCategoryName(categoryName)
            return ResponseEntity.ok("payment deleted")
        //}
        //throw EmptyAccountException("payment not deleted.")
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST) //400
    @ExceptionHandler(value = [ConstraintViolationException::class, NumberFormatException::class, MethodArgumentTypeMismatchException::class, HttpMessageNotReadableException::class])
    fun handleBadHttpRequests(throwable: Throwable): Map<String, String>? {
        val response: MutableMap<String, String> = HashMap()
        logger.error("Bad Request", throwable)
        response["response"] = "BAD_REQUEST: " + throwable.javaClass.simpleName + " , message: " + throwable.message
        return response
    }
}
