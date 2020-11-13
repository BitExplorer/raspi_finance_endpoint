package finance.domain

import com.fasterxml.jackson.databind.ObjectMapper
import finance.helpers.PaymentBuilder
import finance.utils.Constants

import java.sql.Date
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class PaymentSpec extends Specification {
    ValidatorFactory validatorFactory
    Validator validator
    private ObjectMapper mapper = new ObjectMapper()

    def jsonPayload = """
{"accountNameOwner":"foo","amount":5.12, "guidSource":"abc", "guidDestination":"def", "transactionDate":"2020-11-12"}
"""

    void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
    }

    void cleanup() {
        validatorFactory.close()
    }

    def "test -- JSON serialization to Payment"() {
        when:
        Payment payment = mapper.readValue(jsonPayload, Payment.class)

        then:
        payment.accountNameOwner == 'foo'
        payment.amount == 5.12
        payment.guidSource == 'abc'
        payment.guidDestination == 'def'
        0 * _
    }

    def "test validation valid payment"() {
        given:
        Payment payment = new PaymentBuilder().builder().accountNameOwner("new_brian").build()

        when:
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment)

        then:
        violations.isEmpty()
    }

    @Unroll
    def "test validation invalid #invalidField has error expectedError"() {
        given:
        Payment payment = new PaymentBuilder().builder()
                .accountNameOwner(accountNameOwner)
                .transactionDate(transactionDate)
                .amount(amount)
                .guidDestination(guidDestination)
                .guidSource(guidSource)
                .build()

        when:
        Set<ConstraintViolation<Payment>> violations = validator.validate(payment)

        then:
        violations.size() == errorCount
        violations.message.contains(expectedError)
        violations.iterator().next().getInvalidValue() == payment.getProperties()[invalidField]

        where:
        invalidField       | accountNameOwner | transactionDate      | amount | guidDestination   | guidSource        | expectedError                   | errorCount
        'accountNameOwner' | 'a_'             | new Date(1553645394) | 0.0    | UUID.randomUUID() | UUID.randomUUID() | 'size must be between 3 and 40' | 1
        'guidDestination'  | 'a_b'            | new Date(1553645394) | 0.0    | 'invalid'         | UUID.randomUUID() | Constants.MUST_BE_UUID_MESSAGE  | 1
        'guidSource'       | 'a_b'            | new Date(1553645394) | 0.0    | UUID.randomUUID() | 'invalid'         | Constants.MUST_BE_UUID_MESSAGE  | 1
    }
}