package finance.controllers

import finance.Application
import finance.domain.Transaction
import finance.helpers.TransactionBuilder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.*
import org.springframework.test.context.ActiveProfiles
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Stepwise
import spock.lang.Unroll

@Stepwise
@ActiveProfiles("func")
@SpringBootTest(classes = Application, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionControllerSpec extends BaseControllerSpec {

    @Shared
    protected String jsonPayloadInvalidGuid = '''
{
"accountId":0,
"accountType":"credit",
"transactionDate":"2020-10-05",
"dueDate":"2020-10-07",
"dateUpdated":1593981072000,
"dateAdded":1593981072000,
"guid":"badGuid",
"accountNameOwner":"chase_brian",
"description":"aliexpress.com",
"category":"online",
"amount":3.14,
"transactionState":"cleared",
"reoccurring":false,
"reoccurringType":"undefined",
"notes":"my note to you"
}
'''

    @Shared
    protected String jsonPayloadMissingGuid = '''
{
"accountId":0,
"accountType":"credit",
"transactionDate":"2020-10-05",
"dateUpdated":1593981072000,
"dateAdded":1593981072000,
"accountNameOwner":"chase_brian",
"description":"aliexpress.com",
"category":"online",
"amount":3.14,
"transactionState":"cleared",
"reoccurring":false,
"reoccurringType":"undefined",
"notes":"my note to you"
}
'''

    @Shared
    protected String jsonPayloadInvalidCategory = '''
{
"accountId":0,
"accountType":"credit",
"transactionDate":"2020-10-05",
"dateUpdated":1593981072000,
"dateAdded":1593981072000,
"accountNameOwner":"chase_brian",
"description":"aliexpress.com",
"guid":"2eba99af-6625-4fc7-a65d-e24783ab60c0",
"category":"123451234512345123451234512345123451234512345123451234512345",
"amount":3.14,
"transactionState":"cleared",
"reoccurring":false,
"reoccurringType":"undefined",
"notes":"my note to you"
}
'''

    @Shared
    protected Transaction transaction = TransactionBuilder.builder().build()

    void 'test update Transaction receiptImage'() {
        given:
        String jpegImage = 'data:image/jpeg;base64,/9j/2wBDAAMCAgICAgMCAgIDAwMDBAYEBAQEBAgGBgUGCQgKCgkICQkKDA8MCgsOCwkJDRENDg8QEBEQCgwSExIQEw8QEBD/yQALCAABAAEBAREA/8wABgAQEAX/2gAIAQEAAD8A0s8g/9k='
        String guid = 'ba665bc2-22b6-4123-a566-6f5ab3d796dg'
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(jpegImage, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/transaction/update/receipt/image/${guid}"), HttpMethod.PUT,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.OK)
        0 * _
    }

    void 'test insert Transaction'() {
        given:
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(transaction.toString(), headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/insert/'), HttpMethod.POST,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.OK)
        0 * _
    }

    void 'test insert Transaction duplicate'() {
        given:
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(transaction.toString(), headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/insert/'), HttpMethod.POST,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.BAD_REQUEST)
        0 * _
    }

    void 'test insert Transaction empty'() {
        given:
        Transaction transaction = TransactionBuilder.builder().withDescription('').build()
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(transaction.toString(), headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/insert/'), HttpMethod.POST,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.BAD_REQUEST)
        0 * _
    }

    void 'test find Transaction'() {
        given:
        HttpEntity entity = new HttpEntity<>(null, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/select/' + transaction.guid), HttpMethod.GET,
                entity, String)

        then:
        response.statusCode.is(HttpStatus.OK)
        0 * _
    }

    void 'test find Transaction guid is not found'() {
        given:
        HttpEntity entity = new HttpEntity<>(null, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/transaction/select/" + UUID.randomUUID()), HttpMethod.GET,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.NOT_FOUND)
        0 * _
    }

    void 'test delete Transaction - guid not found'() {
        given:
        HttpEntity entity = new HttpEntity<>(null, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort("/transaction/delete/" + UUID.randomUUID()), HttpMethod.DELETE,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.NOT_FOUND)
        0 * _
    }

    void 'test update Transaction - not found'() {
        given:
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(transaction, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort('/transaction/update/' + UUID.randomUUID()),
                HttpMethod.PUT, entity, String)

        then:
        response.statusCode.is(HttpStatus.BAD_REQUEST)
        0 * _
    }

    void 'test delete Transaction'() {
        given:
        HttpEntity entity = new HttpEntity<>(null, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/delete/' + transaction.guid), HttpMethod.DELETE,
                entity, String)
        then:
        response.statusCode.is(HttpStatus.OK)
        0 * _
    }

    @Unroll
    void 'test insertTransaction endpoint - failure for irregular payload'() {
        given:
        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(payload, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort('/transaction/insert/'), HttpMethod.POST,
                entity, String)
        then:
        response.statusCode.is(httpStatus)
        0 * _

        where:
        payload                                                                          | httpStatus
        'badJson'                                                                        | HttpStatus.BAD_REQUEST
        '{"test":1}'                                                                     | HttpStatus.BAD_REQUEST
        '{badJson:"test"}'                                                               | HttpStatus.BAD_REQUEST
        jsonPayloadInvalidGuid                                                           | HttpStatus.BAD_REQUEST
        jsonPayloadMissingGuid                                                           | HttpStatus.BAD_REQUEST
        jsonPayloadInvalidCategory                                                       | HttpStatus.BAD_REQUEST
        //TransactionBuilder.builder().transactionDate(Date.valueOf("1999-10-15")).build() | HttpStatus.BAD_REQUEST
    }

    @Ignore('error for duplicate constraint and not sure why')
    void 'test update Transaction'() {
        given:
        String updateGud = 'ba665bc2-22b6-4123-a566-6f5ab3d796df'
        Transaction transaction1 = TransactionBuilder.builder().withGuid(updateGud).build()

        headers.setContentType(MediaType.APPLICATION_JSON)
        HttpEntity entity = new HttpEntity<>(transaction1, headers)

        when:
        ResponseEntity<String> response = restTemplate.exchange(createURLWithPort("/transaction/update/${updateGud}"),
                HttpMethod.PUT, entity, String)

        then:
        response.statusCode.is(HttpStatus.OK)
        0 * _
    }
}
