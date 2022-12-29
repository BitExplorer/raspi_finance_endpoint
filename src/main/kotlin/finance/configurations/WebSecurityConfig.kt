package finance.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration

@Configuration
open class WebSecurityConfig()  {
    @Bean
    @Throws(Exception::class)
    open fun configure(http: HttpSecurity) : SecurityFilterChain {
        val corsConfiguration = CorsConfiguration()

        corsConfiguration.allowedOrigins = mutableListOf("https://hornsup:3000", "https://localhost:3000")
        //corsConfiguration.allowedOriginPatterns = mutableListOf("*")
        corsConfiguration.allowedMethods = mutableListOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        //corsConfiguration.allowedHeaders = mutableListOf("*")
        corsConfiguration.allowCredentials = true // this line is important it sends only specified domain instead of *
        corsConfiguration.exposedHeaders = mutableListOf("x-auth-token")
        corsConfiguration.allowedHeaders = mutableListOf("authorization", "content-type", "x-auth-token", "accept");

        // Entry points
//        http.authorizeRequests() //
//            .requestMatchers("/user/signin").permitAll()
//            .requestMatchers("/user/signup").permitAll()
//            // Disallow everything else
//            .anyRequest().authenticated()

        // TODO: bh enable csrf (cross site request forgery)
        // TODO: bh enable headers (not sure what happens when they are disabled) preflight
        //TODO: bh how to enable basic auth
        http
            .csrf().disable()
//            .authorizeHttpRequests()
//            .anyRequest()
//            .authenticated()
//            .and()
//            .httpBasic()
//            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // the server will not send a JSESSIONID cookie
            .and()
            .cors()
            .configurationSource { corsConfiguration }
            //.and()
            //.headers().disable()

        return http.build()
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
