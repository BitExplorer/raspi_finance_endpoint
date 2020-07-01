package finance.domain

import com.fasterxml.jackson.databind.ObjectMapper
import finance.helpers.AccountBuilder
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory
import java.sql.Timestamp

class AccountSpec extends Specification {

    ValidatorFactory validatorFactory
    Validator validator
    private ObjectMapper mapper = new ObjectMapper()

    def setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
    }

    def cleanup() {
        validatorFactory.close()
    }

    def "test JSON serialization to Account object"() {

        given:
        //"accountId":1001,
        def jsonPayload = "{\"accountNameOwner\":\"discover_brian\",\"accountType\":\"credit\",\"activeStatus\":true,\"moniker\":\"1234\",\"totals\":0.01,\"totalsBalanced\":0.02,\"dateClosed\":0,\"dateUpdated\":1553645394000,\"dateAdded\":1553645394000}"

        when:
        Account account = mapper.readValue(jsonPayload, Account.class)

        then:
        account.accountType == AccountType.Credit
        account.accountNameOwner == "discover_brian"
        account.accountId == 0
    }

    def "test validation valid account"() {
        given:
        Account account = AccountBuilder.builder().build()

        when:
        Set<ConstraintViolation<Account>> violations = validator.validate(account)

        then:
        violations.isEmpty()
    }

    @Unroll
    def "test validation invalid #invalidField has error #expectedError"() {
        given:
        Account account = new AccountBuilder()
                .accountType(accountType)
                .moniker(moniker)
                .accountNameOwner(accountNameOwner)
                .activeStatus(activeStatus)
                .totals(totals)
                .totalsBalanced(totalsBalanced)
                .dateAdded(dateAdded)
                .dateUpdated(dateUpdated)
                .build()

        when:
        Set<ConstraintViolation<Account>> violations = validator.validate(account)

        then:
        println("message='${violations.message}'")
        println("size='${violations.size()}'")

        violations.size() == errorCount
        violations.message.contains(expectedError)
        violations.iterator().next().getInvalidValue() == account.getProperties()[invalidField]

        where:
        invalidField       | accountId | accountType        | accountNameOwner   | moniker | activeStatus | dateAdded                    | dateUpdated                  | totals                 | totalsBalanced         | expectedError                              | errorCount
        'accountNameOwner' | 123L      | AccountType.Debit  | 'blah_chase_brian' | '0000'  | true         | new Timestamp(1553645394000) | new Timestamp(1553645394000) | new BigDecimal("0.00") | new BigDecimal("0.00") | 'must be alpha separated by an underscore' | 1
        'accountNameOwner' | 123L      | AccountType.Credit | '_b'               | '0000'  | true         | new Timestamp(1553645394000) | new Timestamp(1553645394000) | new BigDecimal("0.00") | new BigDecimal("0.00") | 'size must be between 3 and 40'            | 1
        'moniker'          | 123L      | AccountType.Credit | 'chase_brian'      | 'abc'   | true         | new Timestamp(1553645394000) | new Timestamp(1553645394000) | new BigDecimal("0.00") | new BigDecimal("0.00") | 'Must be 4 digits.'                        | 1
        'moniker'          | 123L      | AccountType.Credit | 'chase_brian'      | '00001' | true         | new Timestamp(1553645394000) | new Timestamp(1553645394000) | new BigDecimal("0.00") | new BigDecimal("0.00") | 'Must be 4 digits.'                        | 1
        'dateAdded'        | 123L      | AccountType.Credit | 'chase_brian'      | '0001'  | true         | new Timestamp(1553645394)    | new Timestamp(1553645394000) | new BigDecimal("0.00") | new BigDecimal("0.00") | 'timestamp must be greater than 1/1/2000.' | 1
        'dateUpdated'      | 123L      | AccountType.Credit | 'chase_brian'      | '0001'  | true         | new Timestamp(1553645394000) | new Timestamp(1553645394)    | new BigDecimal("0.00") | new BigDecimal("0.00") | 'timestamp must be greater than 1/1/2000.' | 1
        //'activeStatus'          | 123L      | AccountType.Credit | 'chase_brian'      | '0000'   | null|null |null |         | 'Must be 4 digits.'                        | new BigDecimal("0.00") | new BigDecimal("0.00")| 1

    }
}
