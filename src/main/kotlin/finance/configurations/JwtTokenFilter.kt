//package finance.configurations
//
//import jakarta.servlet.FilterChain
//import jakarta.servlet.ServletException
//import jakarta.servlet.http.HttpServletRequest
//import jakarta.servlet.http.HttpServletResponse
//import org.springframework.security.core.Authentication
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.web.filter.OncePerRequestFilter
//import java.io.IOException
//
//
//// We should use OncePerRequestFilter since we are doing a database call, there is no point in doing this more than once
//class JwtTokenFilter(jwtTokenProvider: JwtTokenProvider) : OncePerRequestFilter() {
//    private val jwtTokenProvider: JwtTokenProvider
//
//    //@Throws(ServletException::class, IOException::class)
//    //bh 12/10/2022 - removed @Throws and fixed exeptions
//    override fun doFilterInternal(
//            httpServletRequest: HttpServletRequest,
//            httpServletResponse: HttpServletResponse,
//            filterChain: FilterChain
//    ) {
//        val token: String? = jwtTokenProvider.resolveToken(httpServletRequest)
//        try {
//            if ( jwtTokenProvider.validateToken(token)) {
//                val auth: Authentication = jwtTokenProvider.getAuthentication(token)
//                SecurityContextHolder.getContext().authentication = auth
//            }
//        } catch (ex: RuntimeException) {
//            //this is very important, since it guarantees the user is not authenticated at all
//            SecurityContextHolder.clearContext()
//            //TODO: bh fix this
//            //httpServletResponse.sendError(HTTPResp, ex.getMessage());
//            return
//        }
//        filterChain.doFilter(httpServletRequest, httpServletResponse)
//    }
//
//    init {
//        this.jwtTokenProvider = jwtTokenProvider
//    }
//}