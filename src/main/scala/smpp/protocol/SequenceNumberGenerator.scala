package smpp.protocol

import java.util.concurrent.atomic.AtomicInteger

import smpp.protocol.SmppTypes.SequenceNumber

trait SequenceNumberGenerator {
  def next: SequenceNumber
}

class AtomicIntegerSequenceNumberGenerator extends SequenceNumberGenerator {
  val ai = new AtomicInteger()

  def next = 1 + (ai.getAndIncrement % 0x7FFFFFFF)
}

