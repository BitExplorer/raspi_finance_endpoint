package finance.services

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import finance.domain.Account
import finance.domain.TransactionState
import finance.helpers.AccountBuilder

import javax.validation.ConstraintViolation
import javax.validation.ValidationException
import java.math.RoundingMode

@SuppressWarnings("GroovyAccessibility")
class AccountServiceSpec extends BaseServiceSpec {
    protected AccountService accountService = new AccountService(accountRepositoryMock, transactionRepositoryMock)


    protected String validJsonPayload = '''
{
"accountNameOwner": "test_brian",
"accountType": "credit",
"activeStatus": "true",
"moniker": "0000",
"totals": 0.00,
"totalsBalanced": 0.00,
"dateClosed": 0
}
'''

    void setup() {
        accountService.validator = validatorMock
        accountService.meterService = meterService
    }

    void 'test findAllActiveAccounts empty'() {
        given:
        Account account = AccountBuilder.builder().build()
        List<Account> accounts = []
        accounts.add(account)

        when:
        List<Account> results = accountService.findByActiveStatusOrderByAccountNameOwner()

        then:
        results.size() == 1
        1 * accountRepositoryMock.findByActiveStatusOrderByAccountNameOwner(true) >> accounts
        0 * _
    }

    void 'test findAllActiveAccounts'() {
        given:
        Account account = AccountBuilder.builder().build()
        List<Account> accounts = [account, account, account, account]

        when:
        List<Account> results = accountService.findByActiveStatusOrderByAccountNameOwner()

        then:
        results.size() == 4
        1 * accountRepositoryMock.findByActiveStatusOrderByAccountNameOwner(true) >> accounts
        0 * _
    }

    void 'test insertAccount - attempt to insert a preexisting account'() {
        given:
        Account account = AccountBuilder.builder().build()
        Set<ConstraintViolation<Account>> constraintViolations = validator.validate(account)

        when:
        accountService.insertAccount(account)

        then:
        thrown(RuntimeException)
        constraintViolations.size() == 0
        1 * validatorMock.validate(account) >> constraintViolations
        1 * accountRepositoryMock.findByAccountNameOwner(account.accountNameOwner) >> Optional.of(account)
        0 * _
    }

    void 'test insertAccount - attempt to insert a empty accountNameOwner'() {
        given:
        Account account = AccountBuilder.builder().withAccountNameOwner('').build()
        Set<ConstraintViolation<Account>> constraintViolations = validator.validate(account)

        when:
        accountService.insertAccount(account)

        then:
        thrown(ValidationException)
        constraintViolations.size() == 2
        1 * validatorMock.validate(account) >> constraintViolations
        1 * accountRepositoryMock.findByAccountNameOwner(account.accountNameOwner) >> Optional.of(account)
        1 * meterRegistryMock.counter(validationExceptionThrownMeter) >> counter
        1 * counter.increment()
        0 * _
    }

    void 'test insertAccount - json inserted success'() {
        given:
        Account account = mapper.readValue(validJsonPayload, Account)
        Set<ConstraintViolation<Account>> constraintViolations = validator.validate(account)

        when:
        Account accountInserted = accountService.insertAccount(account)

        then:
        accountInserted.accountNameOwner == account.accountNameOwner
        1 * validatorMock.validate(account) >> constraintViolations
        1 * accountRepositoryMock.findByAccountNameOwner(account.accountNameOwner) >> Optional.empty()
        1 * accountRepositoryMock.saveAndFlush(account) >> account
        0 * _
    }

    void 'test insertAccount - invalid moniker'() {
        given:
        Account account = AccountBuilder.builder().withMoniker('12345').build()
        Set<ConstraintViolation<Account>> constraintViolations = validator.validate(account)

        when:
        accountService.insertAccount(account)

        then:
        constraintViolations.size() == 1
        ValidationException ex = thrown(ValidationException)
        ex.message.contains('Cannot insert record because of constraint violation(s)')
        1 * accountRepositoryMock.findByAccountNameOwner(account.accountNameOwner) >> Optional.of(account)
        1 * validatorMock.validate(account) >> constraintViolations
        1 * meterRegistryMock.counter(validationExceptionThrownMeter) >> counter
        1 * counter.increment()
        0 * _
    }

    void 'test insertAccount - bad json - accountType'() {
        given:
        String jsonPayload = "{\"accountId\":1001,\"accountNameOwner\":\"discover_brian\",\"accountType\":\"Credit\",\"activeStatus\":true,\"moniker\":\"1234\",\"totals\":0.01,\"totalsBalanced\":0.02,\"dateClosed\":0}"

        when:
        mapper.readValue(jsonPayload, Account)

        then:
        InvalidFormatException ex = thrown()
        ex.message.contains('not one of the values accepted for Enum class')
        0 * _
    }

    void 'computeTheGrandTotalForAllTransactions'() {
        given:
        BigDecimal desiredResult = new BigDecimal(5.75).setScale(2, RoundingMode.HALF_UP)
        when:
        BigDecimal result = accountService.sumOfAllTransactionsByTransactionState(TransactionState.Cleared)

        then:
        result == desiredResult
        1 * accountRepositoryMock.sumOfAllTransactionsByTransactionState(TransactionState.Cleared.toString()) >> desiredResult
        0 * _
    }

    void 'computeTheGrandTotalForAllClearedTransactions'() {
        given:
        BigDecimal desiredResult = new BigDecimal(8.92).setScale(2, RoundingMode.HALF_UP)
        when:
        BigDecimal result = accountService.sumOfAllTransactionsByTransactionState(TransactionState.Cleared)

        then:
        result == desiredResult
        1 * accountRepositoryMock.sumOfAllTransactionsByTransactionState(TransactionState.Cleared.toString()) >> desiredResult
        0 * _
    }

    void 'computeTheGrandTotalForAllClearedTransactions - 3 decimal'() {
        given:
        BigDecimal desiredResult = new BigDecimal(8.923).setScale(3, RoundingMode.HALF_UP)
        when:
        BigDecimal result = accountService.sumOfAllTransactionsByTransactionState(TransactionState.Cleared)

        then:
        result != desiredResult
        1 * accountRepositoryMock.sumOfAllTransactionsByTransactionState(TransactionState.Cleared.toString()) >> desiredResult
        0 * _
    }
}
