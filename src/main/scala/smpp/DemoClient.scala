package smpp


import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem, Terminated}
import akka.io.{IO, Tcp}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import smpp.actors.SmppClient.{Bind, Did, SendEnquireLink, SendMessage, SendMessageAck, SendPdu}
import smpp.actors.SmppServer.Disconnected
import smpp.actors._
import smpp.protocol._
import smpp.protocol.auth.{BindAuthenticator, BindRequest, BindResponse}
import akka.pattern.ask

import scala.concurrent.Future
import scala.concurrent.duration._

object DemoClient extends App {

  implicit val actorSystem = ActorSystem("demo")
  implicit val materializer = ActorMaterializer()
  implicit val ec = actorSystem.dispatcher
  val manager = IO(Tcp)

  val pduBuilder = new PduBuilder()

  implicit val t: Timeout = 5.seconds

  val myClient = SmppClient.connect(SmppClientConfig(new InetSocketAddress("localhost", 2775), 30.seconds, None, Some(SmppClient.Bind("hello", "bud"))), {
    case EnquireLink(sn) => EnquireLinkResp(sn)
  },
    "client", printlnPduLogger("client"))

  Thread.sleep(3000)
  println(myClient)
  val submitSmF = myClient ? SendPdu(pduBuilder.submitSm(sourceAddr = COctetString.ascii("+9779856034616"), destinationAddr = COctetString.ascii("+9779856034616"), shortMessage = OctetString.fromBytes(Array[Byte](0, 0, 0))))

  submitSmF.onComplete(println)

  def printlnPduLogger(prefix: String) = new PduLogger {
    override def logOutgoing(pdu: Pdu): Unit = println(s"$prefix OUT: $pdu")

    override def logIncoming(pdu: Pdu): Unit = println(s"$prefix IN : $pdu")
  }
}

