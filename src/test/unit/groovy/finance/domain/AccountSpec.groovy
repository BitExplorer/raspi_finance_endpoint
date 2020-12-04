package finance.domain

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import finance.helpers.AccountBuilder
import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class AccountSpec extends Specification {

    protected ValidatorFactory validatorFactory
    protected Validator validator
    protected ObjectMapper mapper = new ObjectMapper()

    void setup() {
        validatorFactory = Validation.buildDefaultValidatorFactory()
        validator = validatorFactory.getValidator()
    }

    void cleanup() {
        validatorFactory.close()
    }

    private String jsonPayload = '''
{"accountNameOwner":"discover_brian","accountType":"credit","activeStatus":true,
"moniker":"1234","totals":0.01,"totalsBalanced":0.02,
"dateClosed":0}
'''

    private String jsonPayloadNonValidEnum = """
{"accountNameOwner":"discover_brian","accountType":"non-valid","activeStatus":true,
"moniker":"1234","totals":0.01,"totalsBalanced":0.02,
"dateClosed":0,"dateUpdated":1553645394000,"dateAdded":1553645394000}
"""

    void 'test JSON deserialization to Account'() {
        when:
        Account account = mapper.readValue(jsonPayload, Account)

        then:
        account.accountType == AccountType.Credit
        account.accountNameOwner == "discover_brian"
        0 * _
    }

    void 'test validation valid account'() {
        given:
        Account account = AccountBuilder.builder().build()

        when:
        Set<ConstraintViolation<Account>> violations = validator.validate(account)

        then:
        violations.empty
        0 * _
    }

    void 'test JSON deserialization to Account - non-valid enum'() {
        when:
        mapper.readValue(jsonPayloadNonValidEnum, Account)

        then:
        InvalidFormatException ex = thrown(InvalidFormatException)
        ex.message.contains('not one of the values accepted for Enum class')
        0 * _
    }

    void 'test validation valid account - invalid enum'() {
        when:
        AccountBuilder.builder().accountType(AccountType.valueOf("invalid")).build()

        then:
        IllegalArgumentException ex = thrown(IllegalArgumentException)
        ex.message.contains('No enum constant finance.domain.AccountType')
        0 * _
    }

    @Unroll
    void 'test validation invalid #invalidField has error #expectedError'() {
        given:
        Account account = new AccountBuilder()
                .accountType(accountType)
                .moniker(moniker)
                .accountNameOwner(accountNameOwner)
                .activeStatus(activeStatus)
                .totals(totals)
                .totalsBalanced(totalsBalanced)
                .build()

        when:
        Set<ConstraintViolation<Account>> violations = validator.validate(account)

        then:
        violations.size() == errorCount
        violations.message.contains(expectedError)
        violations.iterator().next().invalidValue == account.properties[invalidField]

        where:
        invalidField       | accountType        | accountNameOwner   | moniker | activeStatus | totals                 | totalsBalanced         | expectedError                              | errorCount
        'accountNameOwner' | AccountType.Debit  | 'blah_chase_brian' | '0000'  | true         | new BigDecimal('0.00') | new BigDecimal('0.00') | 'must be alpha separated by an underscore' | 1
        'accountNameOwner' | AccountType.Credit | '_b'               | '0000'  | true         | new BigDecimal('0.00') | new BigDecimal('0.00') | 'size must be between 3 and 40'            | 1
        'moniker'          | AccountType.Credit | 'chase_brian'      | 'abc'   | true         | new BigDecimal('0.00') | new BigDecimal('0.00') | 'Must be 4 digits.'                        | 1
        'moniker'          | AccountType.Credit | 'chase_brian'      | '00001' | true         | new BigDecimal('0.00') | new BigDecimal('0.00') | 'Must be 4 digits.'                        | 1
        //'accountType'     | AccountType.valueOf("invalid") | 'chase_brian'      | '00001' | true         | new BigDecimal("0.00") | new BigDecimal("0.00") | 'Must be 4 digits.'                        | 1
    }
}
