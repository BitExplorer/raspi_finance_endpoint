package finance.services

import com.fasterxml.jackson.databind.ObjectMapper
import finance.domain.Description
import finance.repositories.DescriptionRepository
import org.apache.logging.log4j.LogManager
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import javax.validation.ConstraintViolation
import javax.validation.ValidationException
import javax.validation.Validator

@Service
open class DescriptionService(
    private var descriptionRepository: DescriptionRepository,
    private val validator: Validator,
    private var meterService: MeterService
) {

    fun insertDescription(description: Description): Boolean {
        val constraintViolations: Set<ConstraintViolation<Description>> = validator.validate(description)
        if (constraintViolations.isNotEmpty()) {
            constraintViolations.forEach { constraintViolation -> logger.error(constraintViolation.message) }
            logger.error("Cannot insert description as there is a constraint violation on the data.")
            throw ValidationException("Cannot insert description as there is a constraint violation on the data.")
        }
        description.dateAdded = Timestamp(Calendar.getInstance().time.time)
        description.dateUpdated = Timestamp(Calendar.getInstance().time.time)
        descriptionRepository.saveAndFlush(description)
        return true
    }

    fun deleteByDescriptionName(description: String): Boolean {
        descriptionRepository.deleteByDescription(description)
        return true
    }

    fun fetchAllDescriptions(): List<Description> {
        return descriptionRepository.findByActiveStatusOrderByDescription(true)
    }

    fun findByDescriptionName(descriptionName: String): Optional<Description> {
       return descriptionRepository.findByDescription(descriptionName)
    }

    companion object {
        private val mapper = ObjectMapper()
        private val logger = LogManager.getLogger()
    }
}