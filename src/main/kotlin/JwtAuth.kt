import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.response.respond

val JWTAuthKey: String = "JwtAuth"
fun AuthenticationPipeline.bearerAuthentication(realm: String) {
    intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        // parse token
        val authHeader = call.request.parseAuthorizationHeader()
        val jwt: DecodedJWT? =
                if (authHeader?.authScheme == "Bearer" && authHeader is HttpAuthHeader.Single) {
                    try {
                        verifyToken(authHeader.blob)
                    } catch (e: Exception) {
                        null
                    }
                } else null

        // transform token to principal
        val principal = jwt?.let { UserIdPrincipal(jwt.subject ?: jwt.getClaim("client_id").asString()) }

        // set principal if success
        if (principal != null) {
            context.principal(principal)
        } else {
            // otherwise, challenge with WWW-Authenticate header & realm
            context.challenge(JWTAuthKey, NotAuthenticatedCause.InvalidCredentials) {
                it.success()
                call.respond(UnauthorizedResponse(HttpAuthHeader.bearerAuthChallenge(realm)))
            }
        }
    }
}

private fun HttpAuthHeader.Companion.bearerAuthChallenge(realm: String): HttpAuthHeader = HttpAuthHeader.Parameterized("Bearer", mapOf(HttpAuthHeader.Parameters.Realm to realm))