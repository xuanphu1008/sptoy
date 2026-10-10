package com.toyland.partner.soap;

import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

/** Loi dich vu SOAP: duoc chuyen thanh SOAP Fault (faultcode Client) kem faultstring tieng Viet. */
@SoapFault(faultCode = FaultCode.CLIENT)
public class PartnerSoapException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public PartnerSoapException(String message) {
		super(message);
	}
}
