package finance.services

import finance.domain.Account
import finance.repositories.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
open class AccountService @Autowired constructor(private var accountRepository: AccountRepository<Account>) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    //TODO: check the signature of accountRepository.findAllByOrderByAccountNameOwner
    fun findAllOrderByAccountNameOwner(): List<Account> {
        return accountRepository.findAllByOrderByAccountNameOwner()
    }

    fun findAllActiveAccounts(): List<Account> {
        val accounts = accountRepository.findByActiveStatusOrderByAccountNameOwner(true)
        if (accounts.isEmpty()) {
            logger.warn("findAllActiveAccounts() - no accounts found.")
        } else {
            logger.info("findAllActiveAccounts() - found accounts.")
        }
        return accounts
    }

    fun selectTotals() : Double {
        return accountRepository.selectTotals()
    }

    fun selectTotalsCleared() : Double {
        return accountRepository.selectTotalsCleared()
    }

    fun findByAccountNameOwner(accountNameOwner: String): Optional<Account> {
        return accountRepository.findByAccountNameOwner(accountNameOwner)
    }

    fun insertAccount(account: Account): Boolean {
        //TODO: Should saveAndFlush be in a try catch block?
        logger.info("INFO: transactionRepository.saveAndFlush call.")
        accountRepository.saveAndFlush(account)
        logger.info("INFO: transactionRepository.saveAndFlush success.")
        return true
    }

    fun deleteByAccountNameOwner(accountNameOwner: String) {
        accountRepository.deleteByAccountNameOwner(accountNameOwner)
    }

    fun updateAccountTotals() {
        logger.info("updateAccountGrandTotals")
        accountRepository.updateAccountGrandTotals()
        logger.info("updateAccountClearedTotals")
        accountRepository.updateAccountClearedTotals()
        logger.info("updateAccountTotals")
    }

    //TODO: Complete the function
    fun patchAccount(account: Account): Boolean {
        val optionalAccount = accountRepository.findByAccountNameOwner(account.accountNameOwner)
        if (optionalAccount.isPresent) {
            logger.info("patch the account.")
            //var updateFlag = false
            //val fromDb = optionalAccount.get()
        }

        return false
    }
}