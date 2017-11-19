import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        install(Routing) {
            get("/") {
                call.respondText("Ktor Working!", ContentType.Text.Html)
            }
            /*get("/api") {
                val authHeader = call.request.parseAuthorizationHeader()

                    if (!(authHeader == null || authHeader !is HttpAuthHeader.Single || authHeader.authScheme != "Bearer")) {
                        try {
                            val jwt = verifyToken(authHeader.blob)
                            context.authentication.principal = UserIdPrincipal(jwt.subject ?: jwt.getClaim("client_id").asString())
                        } catch (e: Exception) {
                            // ignore invalid token
                        }
                    }

                    if (call.principal<UserIdPrincipal>() != null) {
                        call.respondText("Hello ${call.principal<UserIdPrincipal>()?.name}!")
                    } else {
                        call.respond(UnauthorizedResponse(HttpAuthHeader.Parameterized("Bearer", mapOf("realm" to "api1"))))
                    }
            }*/
            route("/api", HttpMethod.Get) {
                authentication {
                    bearerAuthentication("api1")
                }
                handle {
                    call.respondText("Hello ${call.principal<UserIdPrincipal>()?.name}")
                }
            }
        }
    }

    server.start()
}