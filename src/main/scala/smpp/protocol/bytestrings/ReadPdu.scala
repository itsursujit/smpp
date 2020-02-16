package smpp.protocol.bytestrings


import akka.util.ByteIterator
import smpp.protocol.CommandStatus.CommandStatus
import smpp.protocol.NumericPlanIndicator.NumericPlanIndicator
import smpp.protocol.TypeOfNumber.TypeOfNumber
import smpp.protocol.bytestrings.SmppByteString.Iterator
import smpp.protocol.{OctetString, AlertNotification, BindReceiver, BindReceiverResp, BindTransceiver, BindTransceiverResp, BindTransmitter, BindTransmitterResp, COctetString, CancelSm, CancelSmResp, CommandId, DataSm, DataSmResp, DeliverSm, DeliverSmResp, EnquireLink, EnquireLinkResp, GenericNack, Outbind, Pdu, QuerySm, QuerySmResp, ReplaceSm, ReplaceSmResp, SubmitMulti, SubmitMultiResp, SubmitSm, SubmitSmResp, Unbind, UnbindResp}

/**
 * Parse PDU from bytestrings
 */
object ReadPdu {

  implicit val bo = java.nio.ByteOrder.BIG_ENDIAN

  def readPdu(bi: ByteIterator): Pdu = {
    bi.getInt // size, don't need
    val pduType = bi.getCommandId
    val status = bi.getCommandStatus
    val sequenceNumber = bi.getInt

    pduType match {
      case CommandId.alert_notification =>
        AlertNotification(sequenceNumber, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString, if (bi.isEmpty) None else Some(bi.getTlv))
      case CommandId.bind_receiver =>
        BindReceiver(sequenceNumber, bi.getCOctetString, bi.getCOctetString, bi.getCOctetString, bi.getByte,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString)
      case CommandId.bind_receiver_resp =>
        BindReceiverResp(status, sequenceNumber, bi.getCOctetStringMaybe, bi.getTlvs.headOption)
      case CommandId.bind_transceiver =>
        BindTransceiver(sequenceNumber, bi.getCOctetString, bi.getCOctetString, bi.getCOctetString, bi.getByte,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString)
      case CommandId.bind_transceiver_resp =>
        BindTransceiverResp(status, sequenceNumber, bi.getCOctetStringMaybe, bi.getTlvs.headOption)
      case CommandId.bind_transmitter =>
        BindTransmitter(sequenceNumber, bi.getCOctetString, bi.getCOctetString, bi.getCOctetString, bi.getByte,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString)
      case CommandId.bind_transmitter_resp =>
        BindTransmitterResp(status, sequenceNumber, bi.getCOctetStringMaybe, bi.getTlvs.headOption)
      case CommandId.cancel_sm =>
        CancelSm(sequenceNumber, bi.getServiceType, bi.getCOctetString, bi.getTypeOfNumber, bi.getNumericPlanIndicator,
          bi.getCOctetString, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString)
      case CommandId.cancel_sm_resp =>
        CancelSmResp(status, sequenceNumber)
      case CommandId.data_sm =>
        DataSm(sequenceNumber, bi.getServiceType, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString, bi.getEsmClass, bi.getRegisteredDelivery,
          bi.getDataCodingScheme, bi.getTlvs)
      case CommandId.data_sm_resp =>
        DataSmResp(status, sequenceNumber, bi.getCOctetString, bi.getTlvs)
      case CommandId.deliver_sm =>
        val partial = DeliverSm(sequenceNumber, bi.getServiceType, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString, bi.getEsmClass, bi.getByte, bi.getPriority,
          bi.getTime, bi.getTime, bi.getRegisteredDelivery, bi.getByte != 0, bi.getDataCodingScheme, bi.getByte,
          bi.getByte, OctetString.empty, Nil) // placeholders, which we fix in the next line
        partial.copy(shortMessage = bi.getOctetString(partial.smLength), tlvs = bi.getTlvs)
      case CommandId.deliver_sm_resp =>
        DeliverSmResp(status, sequenceNumber, bi.getCOctetStringMaybe)
      case CommandId.enquire_link =>
        EnquireLink(sequenceNumber)
      case CommandId.enquire_link_resp =>
        EnquireLinkResp(sequenceNumber)
      case CommandId.generic_nack =>
        GenericNack(status, sequenceNumber)
      case CommandId.outbind =>
        Outbind(sequenceNumber, bi.getCOctetString, bi.getCOctetString)
      case CommandId.query_sm =>
        QuerySm(sequenceNumber, bi.getCOctetString, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString)
      case CommandId.query_sm_resp =>
        QuerySmResp(status, sequenceNumber, bi.getCOctetString, bi.getTime, bi.getMessageState, bi.getInt)
      case CommandId.replace_sm =>
        val partial = ReplaceSm(sequenceNumber, bi.getCOctetString, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          bi.getTime, bi.getTime, bi.getRegisteredDelivery, bi.getByte, bi.getByte, OctetString.empty)
        partial.copy(shortMessage = bi.getOctetString(partial.smLength))
      case CommandId.replace_sm_resp =>
        ReplaceSmResp(status, sequenceNumber)
      case CommandId.submit_multi =>
        val partial = SubmitMulti(sequenceNumber, bi.getServiceType, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          0, getDestAddresses(bi), bi.getEsmClass, bi.getByte, bi.getPriority, bi.getTime, bi.getTime, bi.getRegisteredDelivery,
          bi.getByte != 0, bi.getDataCodingScheme, bi.getByte, bi.getByte, OctetString.empty, Nil)
        partial.copy(shortMessage = bi.getOctetString(partial.smLength), tlvs = bi.getTlvs, numberOfDests = partial.destAddresses.length)
      case CommandId.submit_multi_resp =>
        type UnsuccessSme = (TypeOfNumber, NumericPlanIndicator, COctetString, CommandStatus)

        def getUnsuccessSmes(): List[UnsuccessSme] = {
          if (bi.isEmpty) Nil
          else (bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString, bi.getCommandStatus) :: getUnsuccessSmes
        }

        SubmitMultiResp(status, sequenceNumber, bi.getCOctetString, bi.getByte, getUnsuccessSmes())

      case CommandId.submit_sm =>
        val partial = SubmitSm(sequenceNumber, bi.getServiceType, bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString,
          bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString, bi.getEsmClass, bi.getByte, bi.getPriority,
          bi.getTime, bi.getTime, bi.getRegisteredDelivery, bi.getByte != 0, bi.getDataCodingScheme, bi.getByte,
          bi.getByte, OctetString.empty, Nil)
        partial.copy(shortMessage = bi.getOctetString(partial.smLength), tlvs = bi.getTlvs)
      case CommandId.submit_sm_resp =>
        SubmitSmResp(status, sequenceNumber, bi.getCOctetStringMaybe)

      case CommandId.unbind =>
        Unbind(sequenceNumber)
      case CommandId.unbind_resp =>
        UnbindResp(status, sequenceNumber)
    }
  }

  type DestAddress = (TypeOfNumber, NumericPlanIndicator, COctetString)

  /**
   * Consumes dest addresses for SubmitMulti. Also consumes the previous integer which has the count of addresses.
   */
  private def getDestAddresses(bi: ByteIterator): List[DestAddress] = {
    def loop(n: Int): List[DestAddress] = {
      if (n == 0) Nil
      else (bi.getTypeOfNumber, bi.getNumericPlanIndicator, bi.getCOctetString) :: loop(n - 1)
    }

    loop(bi.getInt)
  }

}

