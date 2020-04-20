package finance.domain

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.databind.ObjectMapper
import finance.utils.AccountTypeConverter
import finance.utils.Constants.ALPHA_UNDERSCORE_PATTERN
import finance.utils.Constants.ASCII_PATTERN
import finance.utils.Constants.MUST_BE_ALPHA_UNDERSCORE_MESSAGE
import finance.utils.Constants.MUST_BE_ASCII_MESSAGE
import finance.utils.Constants.MUST_BE_DOLLAR_MESSAGE
import finance.utils.Constants.MUST_BE_UUID_MESSAGE
import finance.utils.Constants.UUID_PATTERN
import org.hibernate.annotations.Proxy
import java.math.BigDecimal
import java.sql.Date
import java.sql.Timestamp
import javax.persistence.*
import javax.validation.constraints.*

@Entity(name = "TransactionEntity")
@Proxy(lazy = false)
@Table(name = "t_transaction")
open class Transaction constructor(_transactionId: Long = 0L, _guid: String = "",
                                   _accountId: Long = 0, _accountType: AccountType = AccountType.Credit,
                                   _accountNameOwner: String = "", _transactionDate: Date = Date(0),
                                   _description: String = "", _category: String = "",
                                   _amount: BigDecimal = BigDecimal(0.00), _cleared: Int = 0,
                                   _reoccurring: Boolean = false, _notes: String = "",
                                   _dateUpdated: Timestamp = Timestamp(0),
                                   _dateAdded: Timestamp = Timestamp(0),
                                   _sha256: String = ""
) {

    //TODO: the field activeStatus

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Min(value = 0L)
    @JsonProperty("transactionId")
    var transactionId = _transactionId

    @Column(unique = true)
    @JsonProperty
    @Pattern(regexp = UUID_PATTERN, message = MUST_BE_UUID_MESSAGE)
    var guid = _guid

    @JsonProperty("accountId")
    @Min(value = 0L)
    var accountId = _accountId

    //@Enumerated(EnumType.STRING)
    @Convert(converter = AccountTypeConverter::class)
    @Column(columnDefinition = "VARCHAR")
    @JsonProperty("accountType")
    var accountType = _accountType

    @Size(min = 3, max = 40)
    @JsonProperty
    @Pattern(regexp = ALPHA_UNDERSCORE_PATTERN, message = MUST_BE_ALPHA_UNDERSCORE_MESSAGE)
    var accountNameOwner = _accountNameOwner

    @Column(columnDefinition = "DATE")
    @JsonProperty("transactionDate")
    var transactionDate = _transactionDate

    @JsonGetter("transactionDate")
    fun jsonGetterTransactionDate(): Long {
        println("jsonGetterTransactionDate: ${this.transactionDate.time}")
        return (this.transactionDate.time)
    }

    @Size(min = 1, max = 75)
    @Pattern(regexp = ASCII_PATTERN, message = MUST_BE_ASCII_MESSAGE)
    @JsonProperty
    var description = _description

    @Size(max = 50)
    @JsonProperty
    @Pattern(regexp = ASCII_PATTERN, message = MUST_BE_ASCII_MESSAGE)
    var category = _category

    @JsonProperty
    @Digits(integer = 6, fraction = 2, message = MUST_BE_DOLLAR_MESSAGE)
    var amount = _amount

    @Min(value = -3)
    @Max(value = 1)
    @Column(name = "cleared")
    @JsonProperty
    var cleared = _cleared

    @JsonProperty
    var reoccurring = _reoccurring

    @Size(max = 100)
    @JsonProperty
    @Pattern(regexp = ASCII_PATTERN, message = MUST_BE_ASCII_MESSAGE)
    var notes = _notes

    @JsonProperty("dateUpdated")
    var dateUpdated = _dateUpdated

    @JsonGetter("dateUpdated")
    fun jsonGetterDateUpdated(): Long {
        return (this.dateUpdated.time / 1000)
    }

    //@JsonProperty("dateAdded")
    var dateAdded = _dateAdded

    @JsonGetter("dateAdded")
    fun jsonGetterDateAdded(): Long {
        return (this.dateAdded.time / 1000)
    }

    //TODO: remove this field
    @Size(max = 70)
    @JsonProperty
    var sha256 = _sha256

    //TODO: camel case or snake case?
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "accountId", nullable = true, insertable = false, updatable = false)
    @JsonIgnore
    var account: Account? = null

    //TODO: camel case or snake case?
    @ManyToMany
    @JoinTable(name = "t_transaction_categories",
            joinColumns = [JoinColumn(name = "transactionId")],
            inverseJoinColumns = [JoinColumn(name = "categoryId")])
    @JsonIgnore
    var categries = mutableListOf<Category>()

    override fun toString(): String = mapper.writeValueAsString(this)

    companion object {
        @JsonIgnore
        private val mapper = ObjectMapper()
    }
}
