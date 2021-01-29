package finance.processors

import finance.domain.Account
import finance.domain.Category
import finance.domain.Transaction
import finance.helpers.AccountBuilder
import finance.helpers.CategoryBuilder
import javax.validation.ConstraintViolation

@SuppressWarnings("GroovyAccessibility")
class InsertTransactionProcessorSpec extends BaseProcessor {

    protected String jsonPayload = '''
        {"accountId":0,
        "accountType":"credit",
        "transactionDate":"2020-12-22",
        "guid":"4ea3be58-3993-46de-88a2-4ffc7f1d73bb",
        "accountNameOwner":"chase_brian",
        "description":"aliexpress.com",
        "category":"online",
        "amount":3.14,
        "transactionState":"cleared",
        "activeStatus":true,
        "reoccurring":false,
        "reoccurringType":"undefined",
        "notes":"my note to you"}
        '''

    void 'test -- InsertTransactionProcessor - empty transaction'() {
        given:
        Transaction transaction = mapper.readValue(jsonPayload, Transaction)
        Set<ConstraintViolation<Transaction>> constraintViolations = validator.validate(transaction)

        when:
        insertTransactionProcessor.process(mockExchange)

        then:
        1 * mockExchange.in >> mockMessage
        1 * mockMessage.getBody(String) >> jsonPayload
        1 * mockTransactionRepository.findByGuid(transaction.guid) >> Optional.of(transaction)
        1 * validatorMock.validate(transaction) >> constraintViolations
        1 * mockCategoryRepository.findByCategory(transaction.category) >> Optional.of(new Category())
        1 * mockTransactionRepository.saveAndFlush(transaction)
        1 * mockMessage.setBody(mapper.writeValueAsString(transaction))
        1 * meterRegistryMock.counter(_) >> counter
        1 * counter.increment()
        0 * _
    }

    void 'test -- InsertTransactionProcessor - happy path'() {
        given:
        Transaction transaction = mapper.readValue(jsonPayload, Transaction)
        Account account = AccountBuilder.builder().build()
        Category category = CategoryBuilder.builder().build()
        account.accountNameOwner = transaction.accountNameOwner
        category.category = transaction.category
        Set<ConstraintViolation<Transaction>> constraintViolations = validator.validate(transaction)
        Set<ConstraintViolation<Category>> constraintViolationsCategory = validator.validate(category)
        Set<ConstraintViolation<Account>> constraintViolationsAccount = validator.validate(account)

        when:
        insertTransactionProcessor.process(mockExchange)

        then:
        1 * mockExchange.in >> mockMessage
        1 * mockMessage.getBody(String) >> jsonPayload
        1 * mockTransactionRepository.findByGuid(transaction.guid) >> Optional.empty()
        1 * mockAccountRepository.findByAccountNameOwner(transaction.accountNameOwner) >> Optional.empty()
        2 * mockAccountRepository.findByAccountNameOwner(transaction.accountNameOwner) >> Optional.of(account)
        1 * mockCategoryRepository.findByCategory(transaction.category) >> Optional.empty()
        1 * mockCategoryRepository.saveAndFlush(category)
        1 * validatorMock.validate(transaction) >> constraintViolations
        1 * validatorMock.validate(account) >> constraintViolationsAccount
        1 * validatorMock.validate(category) >> constraintViolationsCategory
        1 * mockTransactionRepository.saveAndFlush(transaction)
        //1 * meterService.incrementTransactionSuccessfullyInsertedCounter(transaction.accountNameOwner)
        1 * meterRegistryMock.counter(_) >> counter
        1 * counter.increment()
        1 * mockMessage.setBody(transaction.toString())
        0 * _
    }
}
