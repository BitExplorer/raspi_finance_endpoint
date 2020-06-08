package finance.services

import finance.domain.Account
import finance.domain.Category
import finance.domain.Transaction
import finance.domain.AccountType
import finance.repositories.AccountRepository
import finance.repositories.CategoryRepository
import finance.repositories.TransactionRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*
import javax.validation.ConstraintViolation

@Service
open class TransactionService @Autowired constructor(private var transactionRepository: TransactionRepository<Transaction>,
                                                     private var accountService: AccountService,
                                                     private var categoryService: CategoryService) {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    fun findAllTransactions(pageable: Pageable) : List<Transaction> {
        return transactionRepository.findAll(pageable).content
    }

    fun findAllTransactions() : List<Transaction> {
        return transactionRepository.findAll()
    }

//    fun findTransactionsByAccountNameOwnerPageable(pageable: Pageable, accountNameOwner: String) : Page<Transaction> {
//        return transactionRepository.findByAccountNameOwnerIgnoreCaseOrderByTransactionDate(pageable, accountNameOwner)
//    }

    fun deleteByGuid(guid: String): Boolean {
        val transactionOptional: Optional<Transaction> = transactionRepository.findByGuid(guid)
        if( transactionOptional.isPresent) {
            transactionRepository.deleteByGuid(guid)
            return true
        }
        return false
    }

    fun insertTransaction(transaction: Transaction): Boolean {
        logger.info("*** insert transaction ***")
        val transactionOptional = findByGuid(transaction.guid)

        if (transactionOptional.isPresent) {
            val transactionDb = transactionOptional.get()
            logger.info("*** update transaction ***")
            return updateTransaction(transactionDb, transaction)
        }

        processAccount(transaction)
        processCategory(transaction)
        transactionRepository.saveAndFlush(transaction)
        logger.info("*** inserted transaction ***")
        return true
    }


    private fun processAccount(transaction: Transaction) {
        var accountOptional = accountService.findByAccountNameOwner(transaction.accountNameOwner)
        if (accountOptional.isPresent) {
            logger.info("METRIC_ACCOUNT_ALREADY_EXISTS_COUNTER")
            transaction.accountId = accountOptional.get().accountId
        } else {
            logger.info("METRIC_ACCOUNT_NOT_FOUND_COUNTER")
            val account = createDefaultAccount(transaction.accountNameOwner, transaction.accountType)
            logger.debug("will insertAccount")
            accountService.insertAccount(account)
            logger.debug("called insertAccount")
            accountOptional = accountService.findByAccountNameOwner(transaction.accountNameOwner)
            transaction.accountId = accountOptional.get().accountId
            //meterRegistry.counter(METRIC_ACCOUNT_NOT_FOUND_COUNTER).increment()
        }
    }

    private fun processCategory(transaction: Transaction) {
        when {
            transaction.category != "" -> {
                val optionalCategory = categoryService.findByCategory(transaction.category)
                if (optionalCategory.isPresent) {
                    transaction.categries.add(optionalCategory.get())
                } else {
                    val category = createDefaultCategory(transaction.category)
                    categoryService.insertCategory(category)
                    transaction.categries.add(category)
                }
            }
        }
    }

    private fun updateTransaction(transactionDb: Transaction, transaction: Transaction): Boolean {
        if(transactionDb.accountNameOwner.trim() == transaction.accountNameOwner) {

            if( transactionDb.amount != transaction.amount ) {
                logger.info("discrepancy in the amount for <${transactionDb.guid}>")
                //TODO: metric for this
                transactionRepository.setAmountByGuid(transaction.amount, transaction.guid)
                return true
            }

            if( transactionDb.cleared != transaction.cleared ) {
                logger.info("discrepancy in the cleared value for <${transactionDb.guid}>")
                //TODO: metric for this
                transactionRepository.setClearedByGuid(transaction.cleared, transaction.guid)
                return true
            }
        }

        logger.info("transaction already exists, no transaction data inserted.")

        return false
    }


    private fun createDefaultCategory(categoryName: String): Category {
        val category = Category()

        category.category = categoryName
        return category
    }

    private fun createDefaultAccount(accountNameOwner: String, accountType: AccountType): Account {
        val account = Account()

        account.accountNameOwner = accountNameOwner
        account.moniker = "0000"
        account.accountType = accountType
        account.activeStatus = true
        account.dateAdded = Timestamp(System.currentTimeMillis())
        account.dateUpdated = Timestamp(System.currentTimeMillis())
        return account
    }

//    fun insertTransaction(transaction: Transaction): Boolean {
//        val transactionOptional: Optional<Transaction> = transactionRepository.findByGuid(transaction.guid)
//        if( transactionOptional.isPresent ) {
//            logger.info("duplicate found, no transaction data inserted.")
//            return false
//        }
//        val accountOptional: Optional<Account> = accountRepository.findByAccountNameOwner(transaction.accountNameOwner)
//        if (accountOptional.isPresent) {
//            val account = accountOptional.get()
//            transaction.accountId = account.accountId
//            logger.info("accountOptional isPresent.")
//            val optionalCategory: Optional<Category> = categoryRepository.findByCategory(transaction.category)
//            if (optionalCategory.isPresent) {
//                val category = optionalCategory.get()
//                transaction.categries.add(category)
//            }
//        } else {
//            logger.info("cannot find the accountNameOwner record " + transaction.accountNameOwner)
//            return false
//        }
//
//        logger.info("insert - transaction.transactionDate: ${transaction.transactionDate}");
//        logger.info("transaction payload: ${transaction.toString()}");
//        transactionRepository.saveAndFlush(transaction)
//        return true
//    }

    fun findByGuid(guid: String): Optional<Transaction> {
        logger.info("call findByGuid")
        val transactionOptional: Optional<Transaction> = transactionRepository.findByGuid(guid)
        if( transactionOptional.isPresent ) {
            return transactionOptional
        }
        return Optional.empty()
    }

    fun getTotalsByAccountNameOwner( accountNameOwner: String) : Map<String, BigDecimal> {

            val result: MutableMap<String, BigDecimal> = HashMap()
            val totalsCleared: Double = transactionRepository.getTotalsByAccountNameOwnerCleared(accountNameOwner)
            val totals: Double = transactionRepository.getTotalsByAccountNameOwner(accountNameOwner)

            result["totals"] = BigDecimal(totals)
            result["totalsCleared"] = BigDecimal(totalsCleared)
            return result
    }

    fun findByAccountNameOwnerIgnoreCaseOrderByTransactionDate(accountNameOwner: String): List<Transaction> {
        val transactions: List<Transaction> = transactionRepository.findByAccountNameOwnerIgnoreCaseOrderByTransactionDateDesc(accountNameOwner)
        if( transactions.isEmpty() ) {
            logger.error("an empty list of AccountNameOwner.")
            //return something
        }
        return transactions
    }


    fun patchTransaction( transaction: Transaction ): Boolean {
        val optionalTransaction = transactionRepository.findByGuid(transaction.guid)
        //TODO: add logic for patch
        if ( optionalTransaction.isPresent ) {
            val fromDb = optionalTransaction.get()
            if( fromDb.description != transaction.description) {

            }
        } else {
            logger.warn("cannot patch a transaction without a valid GUID.")
            return false
        }
        return true
    }

//    fun patchTransaction( transaction: Transaction ): Boolean {
//        val optionalTransaction = transactionRepository.findByGuid(transaction.guid)
//        if ( optionalTransaction.isPresent) {
//            var updateFlag = false
//            val fromDb = optionalTransaction.get()
//
//            if( fromDb.accountNameOwner.trim() != transaction.accountNameOwner && transaction.accountNameOwner != "" ) {
//                fromDb.accountNameOwner = transaction.accountNameOwner
//                val accountOptional: Optional<Account> = accountRepository.findByAccountNameOwner(transaction.accountNameOwner)
//                if (accountOptional.isPresent) {
//                    val account = accountOptional.get()
//                    logger.info("updates work with the new code")
//                    fromDb.accountId = account.accountId
//                }
//                updateFlag = true
//            }
//            if( fromDb.accountType != transaction.accountType && transaction.accountType != AccountType.Undefined ) {
//                fromDb.accountType = transaction.accountType
//                updateFlag = true
//            }
//            if( fromDb.description != transaction.description && transaction.description != "" ) {
//                fromDb.description = transaction.description
//                updateFlag = true
//            }
//            if( fromDb.category != transaction.category && transaction.category != "" ) {
//                fromDb.category = transaction.category
//                updateFlag = true
//            }
//            if( transaction.notes != "" && fromDb.notes != transaction.notes && transaction.notes != "" ) {
//                fromDb.notes = transaction.notes
//                updateFlag = true
//            }
//            if( fromDb.cleared != transaction.cleared && transaction.cleared != 2 ) {
//                fromDb.cleared = transaction.cleared
//                updateFlag = true
//            }
//            if( transaction.amount != fromDb.amount && transaction.amount != BigDecimal(0.0) ) {
//                fromDb.amount = transaction.amount
//                updateFlag = true
//            }
//            if( transaction.transactionDate != Date(0) && transaction.transactionDate != fromDb.transactionDate ) {
//                fromDb.transactionDate = transaction.transactionDate
//                updateFlag = true
//            }
//            if( updateFlag ) {
//                logger.info("Saved transaction as the data has changed")
//                transactionRepository.save(fromDb)
//            }
//            return true
//        } else {
//            logger.warn("guid not found=" + transaction.guid)
//            return false
//        }
//    }
}
