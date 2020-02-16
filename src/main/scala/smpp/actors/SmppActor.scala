package smpp.actors

import akka.actor.{Actor, ActorRef}
import smpp.protocol.SequenceNumberGenerator
import smpp.protocol.SmppTypes.SequenceNumber

trait SmppActor extends Actor {
  def sequenceNumberGen: SequenceNumberGenerator

  def window: Map[SequenceNumber, ActorRef]
}
