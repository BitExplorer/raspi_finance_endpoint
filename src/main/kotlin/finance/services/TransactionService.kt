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
import java.util.*

@Service
open class TransactionService @Autowired constructor(private var transactionRepository: TransactionRepository<Transaction>,
                                                     private var accountRepository: AccountRepository<Account>,
                                                     private var categoryRepository: CategoryRepository<Category>) {
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
        val transactionOptional: Optional<Transaction> = transactionRepository.findByGuid(transaction.guid)
        if( transactionOptional.isPresent ) {
            logger.info("duplicate found, no transaction data inserted.")
            return false
        }
        val accountOptional: Optional<Account> = accountRepository.findByAccountNameOwner(transaction.accountNameOwner)
        if (accountOptional.isPresent) {
            val account = accountOptional.get()
            transaction.accountId = account.accountId
            logger.info("accountOptional isPresent.")
            val optionalCategory: Optional<Category> = categoryRepository.findByCategory(transaction.category)
            if (optionalCategory.isPresent) {
                val category = optionalCategory.get()
                transaction.categries.add(category)
            }
        } else {
            logger.info("cannot find the accountNameOwner record " + transaction.accountNameOwner)
            return false
        }

        logger.info("insert - transaction.transactionDate: ${transaction.transactionDate}");
        logger.info("transaction payload: ${transaction.toString()}");
        transactionRepository.saveAndFlush(transaction)
        return true
    }

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
//            val t = Totals()

            result["totals"] = BigDecimal(totals)
            result["totalsCleared"] = BigDecimal(totalsCleared)
            return result
//            t.totals = BigDecimal(totals)
//            t.totalsCleared = BigDecimal(totalsCleared)
//
//            return t
//        } catch (ex: EmptyResultDataAccessException) {
//            logger.info(ex.message)
//        } catch (e:Exception) {
//            logger.info(e.message)
//        }
        //return Totals()
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
        if ( optionalTransaction.isPresent) {
            var updateFlag = false
            val fromDb = optionalTransaction.get()

            if( fromDb.accountNameOwner.trim() != transaction.accountNameOwner && transaction.accountNameOwner != "" ) {
                fromDb.accountNameOwner = transaction.accountNameOwner
                val accountOptional: Optional<Account> = accountRepository.findByAccountNameOwner(transaction.accountNameOwner)
                if (accountOptional.isPresent) {
                    val account = accountOptional.get()
                    logger.info("updates work with the new code")
                    fromDb.accountId = account.accountId
                }
                updateFlag = true
            }
            if( fromDb.accountType != transaction.accountType && transaction.accountType != AccountType.Undefined ) {
                fromDb.accountType = transaction.accountType
                updateFlag = true
            }
            if( fromDb.description != transaction.description && transaction.description != "" ) {
                fromDb.description = transaction.description
                updateFlag = true
            }
            if( fromDb.category != transaction.category && transaction.category != "" ) {
                fromDb.category = transaction.category
                updateFlag = true
            }
            if( transaction.notes != "" && fromDb.notes != transaction.notes && transaction.notes != "" ) {
                fromDb.notes = transaction.notes
                updateFlag = true
            }
            if( fromDb.cleared != transaction.cleared && transaction.cleared != 2 ) {
                fromDb.cleared = transaction.cleared
                updateFlag = true
            }
            if( transaction.amount != fromDb.amount && transaction.amount != BigDecimal(0.0) ) {
                fromDb.amount = transaction.amount
                updateFlag = true
            }
            if( transaction.transactionDate != Date(0) && transaction.transactionDate != fromDb.transactionDate ) {
                fromDb.transactionDate = transaction.transactionDate
                updateFlag = true
            }
            if( updateFlag ) {
                logger.info("Saved transaction as the data has changed")
                transactionRepository.save(fromDb)
            }
            return true
        } else {
            logger.warn("guid not found=" + transaction.guid)
            return false
        }
    }
}
