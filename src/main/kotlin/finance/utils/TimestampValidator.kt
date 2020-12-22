package finance.utils

import org.apache.logging.log4j.LogManager
import java.sql.Timestamp
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class TimestampValidator : ConstraintValidator<ValidTimestamp, Timestamp> {

    override fun initialize(constraintAnnotation: ValidTimestamp) {
    }

    override fun isValid(value: Timestamp, context: ConstraintValidatorContext): Boolean {
        logger.info("timestampToBeEvaluated: $value")
        return value > Timestamp(946684800000)
    }

    companion object {
        private val logger = LogManager.getLogger()
    }
}