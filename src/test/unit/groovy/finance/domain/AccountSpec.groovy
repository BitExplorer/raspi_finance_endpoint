package finance.domain

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import finance.helpers.AccountBuilder
import spock.lang.Shared
import spock.lang.Unroll

import jakarta.validation.ConstraintViolation

import static finance.utils.Constants.*

class AccountSpec extends BaseDomainSpec {

    @Shared
    protected String jsonPayload = '''
{"accountNameOwner":"discover_brian","accountType":"credit","activeStatus":true,
"moniker":"1234","future":0.01,"outstanding":0.02,"cleared":0.02,
"dateClosed":0}
'''

    @Shared
    protected String jsonPayloadInvalidAccountType = '''
{"accountNameOwner":"discover_brian","accountType":"non-valid","activeStatus":true,
"moniker":"1234","future":0.01,"outstanding":0.02,"cleared":0.02,
"dateClosed":0,"dateUpdated":1553645394000,"dateAdded":1553645394000}
'''

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

    @Unroll
    void 'test -- JSON deserialize to Account with invalid payload'() {
        when:
        mapper.readValue(payload, Account)

        then:
        Exception ex = thrown(exceptionThrown)
        ex.message.contains(message)
        0 * _

        where:
        payload                       | exceptionThrown          | message
        'non-jsonPayload'             | JsonParseException       | 'Unrecognized token'
        '[]'                          | MismatchedInputException | 'Cannot deserialize value of type'
        '{accountNameOwner: "test"}'  | JsonParseException       | 'was expecting double-quote to start field name'
        '{"activeStatus": "abc"}'     | InvalidFormatException   | 'Cannot deserialize value of type'
        jsonPayloadInvalidAccountType | InvalidFormatException   | 'Cannot deserialize value of type'
    }

    @Unroll
    void 'test validation invalid #invalidField has error #expectedError'() {
        given:
        Account account = new AccountBuilder()
                .withAccountType(accountType)
                .withMoniker(moniker)
                .withAccountNameOwner(accountNameOwner)
                .withActiveStatus(activeStatus)
                .withFuture(future)
                .withOutstanding(outstanding)
                .withCleared(cleared)
                .build()

        when:
        Set<ConstraintViolation<Account>> violations = validator.validate(account)

        then:
        violations.size() == errorCount
        violations.message.contains(expectedError)
        violations.iterator().next().invalidValue == account.properties[invalidField]

        where:
        invalidField       | accountType        | accountNameOwner   | moniker | activeStatus | future | outstanding | cleared | expectedError                                       | errorCount
        'accountNameOwner' | AccountType.Debit  | 'blah_chase_brian' | '0000'  | true         | 0.00G  | 0.00G       | 0.00G   | FIELD_MUST_BE_ALPHA_SEPARATED_BY_UNDERSCORE_MESSAGE | 1
        'accountNameOwner' | AccountType.Credit | '_b'               | '0000'  | true         | 0.00G  | 0.00G       | 0.00G   | FILED_MUST_BE_BETWEEN_THREE_AND_FORTY_MESSAGE       | 1
        'moniker'          | AccountType.Credit | 'chase_brian'      | 'abc'   | true         | 0.00G  | 0.00G       | 0.00G   | FIELD_MUST_BE_FOUR_DIGITS_MESSAGE                   | 1
        'moniker'          | AccountType.Credit | 'chase_brian'      | '00001' | true         | 0.00G  | 0.00G       | 0.00G   | FIELD_MUST_BE_FOUR_DIGITS_MESSAGE                   | 1
        //'accountType'      | AccountType.valueOf("invalid") | 'chase_brian'      | '0001' | true         | 0.00G  | 0.00G       | 0.00G   | 'Must be 4 digits.'                        | 1
    }
}
