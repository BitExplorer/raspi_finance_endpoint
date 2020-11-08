package finance.routes

import finance.configurations.CamelProperties
import finance.processors.ExceptionProcessor
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@ConditionalOnProperty(name = ["camel.enabled"], havingValue = "true", matchIfMissing = true)
@Component
class JsonFileWriterRouteBuilder @Autowired constructor(
        private var camelProperties: CamelProperties, private var exceptionProcessor: ExceptionProcessor
) : RouteBuilder() {

    @Throws(Exception::class)
    override fun configure() {
        println(camelProperties.savedFileEndpoint)

        onException(RuntimeException::class.java)
                .log(LoggingLevel.INFO, "filename issue :: \${exception.message}")
                .process(exceptionProcessor)
                .handled(true)
                .end()

        from(camelProperties.jsonFileWriterRoute)
                .autoStartup(camelProperties.autoStartRoute)
                .routeId(camelProperties.jsonFileWriterRouteId)
                .setHeader(Exchange.FILE_NAME, header("guid"))
                .choice()
                .`when`(header("CamelFileName").isNotNull)
                .log(LoggingLevel.INFO, "wrote processed data to file.")
                .to(camelProperties.savedFileEndpoint)
                .log(LoggingLevel.INFO, "message saved to file.")
                .otherwise()
                .throwException(RuntimeException("filename cannot be set to null."))
                .endChoice()
                .end()
    }
}