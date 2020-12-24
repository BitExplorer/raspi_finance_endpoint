package finance.services

import com.fasterxml.jackson.databind.ObjectMapper
import finance.domain.Account
import finance.repositories.AccountRepository
import org.apache.logging.log4j.LogManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.stereotype.Service
import java.sql.Timestamp
import java.util.*
import javax.validation.ConstraintViolation
import javax.validation.ValidationException
import javax.validation.Validator

@Service
class AccountService @Autowired constructor(private var accountRepository: AccountRepository,
                                            private val validator: Validator,
                                            private var meterService: MeterService) {

    fun findByAccountNameOwner(accountNameOwner: String): Optional<Account> {
        return accountRepository.findByAccountNameOwner(accountNameOwner)
    }

    fun findByActiveStatusOrderByAccountNameOwner(): List<Account> {
        val accounts = accountRepository.findByActiveStatusOrderByAccountNameOwner(true)
        if (accounts.isEmpty()) {
            logger.warn("findAllActiveAccounts - no accounts found.")
        } else {
            logger.info("findAllActiveAccounts - found accounts.")
        }
        return accounts
    }

    fun findAccountsThatRequirePayment(): List<String> {
        return accountRepository.findAccountsThatRequirePayment()
    }

    fun computeTheGrandTotalForAllTransactions(): Double {
        val totals: Double
        try {
            totals = accountRepository.computeTheGrandTotalForAllTransactions()
        } catch (e: Exception) {
            return 0.0
        }
        return totals
    }

    fun computeTheGrandTotalForAllClearedTransactions(): Double {
        val totals: Double
        try {
            totals = accountRepository.computeTheGrandTotalForAllClearedTransactions()
        } catch (e: Exception) {
            return 0.0
        }
        return totals
    }

    fun insertAccount(account: Account): Boolean {
        val accountOptional = findByAccountNameOwner(account.accountNameOwner)
        val constraintViolations: Set<ConstraintViolation<Account>> = validator.validate(account)
        if (constraintViolations.isNotEmpty()) {
            logger.error("Cannot insert account as there is a constraint violation on the data.")
            throw ValidationException("Cannot insert account as there is a constraint violation on the data.")
        }

        if (!accountOptional.isPresent) {
            account.dateAdded = Timestamp(Calendar.getInstance().time.time)
            account.dateUpdated = Timestamp(Calendar.getInstance().time.time)
            accountRepository.saveAndFlush(account)
            logger.info("inserted account successfully.")
        } else {
            logger.error("account not inserted as the account already exists ${account.accountNameOwner}.")
            return false
        }

        return true
    }

    fun deleteByAccountNameOwner(accountNameOwner: String) {
        accountRepository.deleteByAccountNameOwner(accountNameOwner)
    }

    // TODO: set the update timestamp
    fun updateTheGrandTotalForAllClearedTransactions() {
        try {
            logger.info("updateAccountGrandTotals")
            accountRepository.updateTheGrandTotalForAllClearedTransactions()
            logger.info("updateAccountClearedTotals")
            accountRepository.updateTheGrandTotalForAllTransactions()
            logger.info("updateAccountTotals")

        } catch (sqlGrammarException: InvalidDataAccessResourceUsageException) {
            logger.error("empty database.")
        }
    }

    //TODO: Complete the function
    fun updateAccount(account: Account): Boolean {
        val optionalAccount = accountRepository.findByAccountNameOwner(account.accountNameOwner)
        if (optionalAccount.isPresent) {

            //account.dateUpdated = Timestamp(Calendar.getInstance().time.time)
            logger.info("patch the account.")
            //var updateFlag = false
            //val fromDb = optionalAccount.get()
        }

        return false
    }

    companion object {
        private val mapper = ObjectMapper()
        private val logger = LogManager.getLogger()
    }
}