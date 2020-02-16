package smpp.ssl

import java.net.InetSocketAddress
import java.security.{KeyStore, SecureRandom}

import com.typesafe.config.ConfigFactory
import javax.net.ssl.{KeyManagerFactory, SSLContext, SSLParameters, TrustManagerFactory}

class SslUtil {

  import scala.jdk.CollectionConverters._

  private val config = ConfigFactory.load()
  val preferredProtocols = config.getStringList("smpp.tls.enabled-protocols").asScala.toSet
  val PreferredCiphersSuites = config.getStringList("smpp.tls.enabled-cipher-suites").asScala.toSet
  val VerifyHostname = config.getBoolean("smpp.tls.verify-hostname")

  def sslContext(keyStore: KeyStore, password: String): SSLContext = {
    val keyManagerFactory = KeyManagerFactory.getInstance("SunX509")
    keyManagerFactory.init(keyStore, password.toCharArray)
    val trustManagerFactory = TrustManagerFactory.getInstance("SunX509")
    trustManagerFactory.init(keyStore)
    val context = SSLContext.getInstance("TLS")
    context.init(keyManagerFactory.getKeyManagers,
      trustManagerFactory.getTrustManagers,
      new SecureRandom)
    context
  }

  def sslEngine(
                 sslContext: SSLContext, remote: InetSocketAddress, client: Boolean) = {
    val engine = sslContext.createSSLEngine(
      remote.getAddress.getHostAddress, remote.getPort)
    //engine.setEnabledCipherSuites(Array("TLS_RSA_WITH_AES_256_CBC_SHA"))
    val params = new SSLParameters()
    if (VerifyHostname) params.setEndpointIdentificationAlgorithm("HTTPS")
    engine.setUseClientMode(client)
    val enabledCipherSuites =
      (engine.getSupportedCipherSuites.toSet intersect PreferredCiphersSuites).toArray
    val enabledProtocols =
      (engine.getSupportedProtocols.toSet intersect preferredProtocols).toArray
    engine.setEnabledProtocols(enabledProtocols)
    engine.setEnabledCipherSuites(enabledCipherSuites)
    engine
  }
}
