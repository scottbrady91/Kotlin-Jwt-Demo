import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpPost
import java.net.URL
import java.security.interfaces.RSAPublicKey

fun main(args: Array<String>) {
    val response = "http://localhost:5000/connect/token".httpPost(listOf(
            "grant_type" to "client_credentials"))
            .authenticate("kotlin_oauth", "client_password")
            .responseString()

    val parser = Parser()
    val json = parser.parse(StringBuilder(response.third.get())) as JsonObject

    verifyToken(json["access_token"].toString())
}

fun verifyToken(token: String) {
    val jwkProvider = UrlJwkProvider(URL("http://localhost:5000/.well-known/openid-configuration/jwks"))

    val jwt = JWT.decode(token)
    val jwk = jwkProvider.get(jwt.keyId)

    //val publicKey: RSAPublicKey = jwk.publicKey as RSAPublicKey // unsafe
    val publicKey = jwk.publicKey as? RSAPublicKey ?: throw Exception("Invalid key type") // safe

    val algorithm = when (jwk.algorithm) {
        "RS256" -> Algorithm.RSA256(publicKey, null)
        else -> throw Exception("Unsupported algorithm")
    }

    val verifier = JWT.require(algorithm)
            .withIssuer("http://localhost:5000") // iss
            .withAudience("api1") // aud
            .build()

    val verifiedToken = verifier.verify(token)
    verifiedToken.claims.forEach { type, claim ->
        when {
            claim.asString() != null -> println("$type: ${claim.asString()}")
            claim.asDate() != null -> println("$type: ${claim.asDate()}")
        }
    }
}