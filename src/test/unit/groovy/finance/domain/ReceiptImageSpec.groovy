package finance.domain

import com.fasterxml.jackson.core.Base64Variants
import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification

import javax.validation.Validation
import javax.validation.Validator
import javax.validation.ValidatorFactory

class ReceiptImageSpec extends BaseDomainSpec {
//    String payload = """
//{"transactionId":1, "image":"data:image/jpeg;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mMMYfj/HwAEVwJUeAAUQgAAAABJRU5ErkJggg==", "activeStatus":true}
//"""
//    String payload = """
//{"transactionId":1, "image":"data:image/jpeg;base64,amFja3Nvbg==", "activeStatus":true}
//"""

    protected String payload = """
{"transactionId":1, "image":"amFja3Nvbg==", "activeStatus":true}
"""

    void 'test ReceiptImage to JSON'() {
        when:
        ReceiptImage receiptImageFromJson = mapper.readValue(payload, ReceiptImage)
        and:
        String result = new String(receiptImageFromJson.image)

        then:
        result == 'jackson'
        noExceptionThrown()
        0 * _
    }

}
