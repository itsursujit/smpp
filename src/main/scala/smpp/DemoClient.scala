package smpp


import java.net.InetSocketAddress

import akka.actor.ActorSystem
import akka.io.{IO, Tcp}
import akka.util.Timeout
import smpp.actors.SmppClient.{SendPdu, Transmitter}
import smpp.actors._
import smpp.protocol._
import akka.pattern.ask

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

object DemoClient extends App {

  implicit val actorSystem: ActorSystem = ActorSystem("demo")
  implicit val ec: ExecutionContextExecutor = actorSystem.dispatcher
  val manager = IO(Tcp)

  implicit val t: Timeout = 50.seconds

  val myClient = SmppClient.connect(
    SmppClientConfig(
      new InetSocketAddress("dev-smsgateway.wholesalebulksms.com", 2775),
      30.seconds,
      None,
      Some(SmppClient.Bind("smppsim", "12345"))
    ), { case EnquireLink(sn) => EnquireLinkResp(sn)},
    "client", printlnPduLogger("client")
  )

  val submitSmF = myClient ? SendPdu(PduBuilder().submitSm(sourceAddr = COctetString.ascii("SMSto"), destinationAddr = COctetString.ascii("+9779856034616"), shortMessage = OctetString.fromBytes("This is test".getBytes)))

  submitSmF.onComplete(println)

  def printlnPduLogger(prefix: String) = new PduLogger {
    override def logOutgoing(pdu: Pdu): Unit = println(s"$prefix OUT: $pdu")

    override def logIncoming(pdu: Pdu): Unit = println(s"$prefix IN : $pdu")
  }
}

