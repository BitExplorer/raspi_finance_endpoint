import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.core.util.FileSize
import org.springframework.boot.logging.logback.ColorConverter
import ch.qos.logback.classic.encoder.PatternLayoutEncoder

def env = System.getenv()
String appName = env['APPNAME'] ?: 'raspi-finance-endpoint'
String springProfile = env['SPRING_PROFILES_ACTIVE'] ?: 'unknown'
String logFilePath = env['LOGS'] ?: 'logs'

String logFileName = "${logFilePath}/${appName}-${springProfile}.log"
String hibernateFileName = "${logFilePath}/${appName}-${springProfile}-hibernate.log"
String errorFileName = "${logFilePath}/${appName}-${springProfile}-error.log"
String logArchiveFileName = "${logFilePath}/archive/${appName}-${springProfile}.%d{yyyy-MM-dd}.gz"
String hibernateArchiveFileName = "${logFilePath}/archive/${appName}-hibernate.%d{yyyy-MM-dd}.gz"
String errorArchiveFileName = "${logFilePath}/archive/${appName}-error.%d{yyyy-MM-dd}.gz"

conversionRule("clr", ColorConverter)

appender("fileAppender", RollingFileAppender) {
    file = logFileName
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        totalSizeCap = "1MB"
        maxHistory = 20
        fileNamePattern = logArchiveFileName
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = FileSize.valueOf('10MB')
    }
}

appender("hibernateFileAppender", RollingFileAppender) {
    file = hibernateFileName
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        totalSizeCap = "1MB"
        maxHistory = 20
        fileNamePattern = hibernateArchiveFileName
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = FileSize.valueOf('10MB')
    }
}

appender("errorFileAppender", RollingFileAppender) {
    file = errorFileName
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        totalSizeCap = "1MB"
        maxHistory = 20
        fileNamePattern = errorArchiveFileName
    }
    triggeringPolicy(SizeBasedTriggeringPolicy) {
        maxFileSize = FileSize.valueOf('10MB')
    }
}

appender("consoleAppender", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = '%clr(%d{yyyy-MM-dd HH:mm:ss}){blue} %clr([%thread]){green} - %clr(%-5level){yellow} %clr(%-36.36logger{36}){cyan} - %clr(%msg){magenta}%n'
    }
}

//appender("asyncAppender", AsyncAppender) {
//    queueSize = 500
//    discardingThreshold = 0
//    includeCallerData = true
//    appenderRef('fileAppender')
//}

//***********************************
// Standard Appender
//***********************************
//def createStandardAppender(String appenderName, String fileName) {
//    def dir = logDirectory
//    def format = logFormat
//    println "Adding appender ${appenderName} with file name ${fileName} in directory ${dir}"
//    appender(appenderName, RollingFileAppender) {
//        file = "${dir}/${fileName}.log"
//        encoder(PatternLayoutEncoder) {
//            pattern = "$format"
//        }
//        rollingPolicy(FixedWindowRollingPolicy) {
//            maxIndex = 4
//            fileNamePattern = "${LOGS:-logs}/archive/${APPNAME:-app-test}.%d{yyyy-MM-dd}.%i.log.gz"
//        }
//        triggeringPolicy(SizeBasedTriggeringPolicy) {
//            maxFileSize = FileSize.valueOf('10MB')
//        }
//    }
//}


logger('org.hibernate.SQL', TRACE, ['hibernateFileAppender'])
logger('org', ERROR, ['errorFileAppender'])
//logger('org.apache.http', INFO)
//logger('finance', INFO)

//root(WARN, ['errorFileAppender'])
root(INFO, ['consoleAppender', 'fileAppender'])
