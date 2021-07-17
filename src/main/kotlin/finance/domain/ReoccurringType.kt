package finance.domain

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty

@JsonFormat
enum class ReoccurringType(val label: String) {
    @JsonProperty("monthly")
    Monthly("monthly"),

    @JsonProperty("annually")
    Annually("annually"),

    @JsonProperty("bi_annually")
    BiAnnually("bi_annually"),

    @JsonProperty("fortnightly")
    FortNightly("fortnightly"),

    @JsonProperty("quarterly")
    Quarterly("quarterly"),

    @JsonProperty("onetime")
    Onetime("onetime"),

    @JsonProperty("undefined")
    Undefined("undefined");

    fun value(): String = label
    override fun toString(): String = name.lowercase()

    companion object {
        //private val VALUES = values();
        //fun getByValue(type: String) = VALUES.firstOrNull { it.type == type }
    }
}