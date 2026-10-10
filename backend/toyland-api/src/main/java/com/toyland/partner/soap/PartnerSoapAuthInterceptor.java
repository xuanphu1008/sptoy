package com.toyland.partner.soap;

import java.time.Instant;
import java.time.LocalDateTime;

import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;

import org.springframework.stereotype.Component;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.EndpointInterceptor;
import org.springframework.ws.soap.SoapHeader;
import org.springframework.ws.soap.SoapHeaderElement;
import org.springframework.ws.soap.SoapMessage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.toyland.partner.Partner;
import com.toyland.partner.PartnerRepository;
import com.toyland.partner.PartnerSigner;

/**
 * Xac thuc SOAP cho doi tac bang tieu de (header) {@code Auth}: apiKey + timestamp + signature.
 * signature = hex(HMAC-SHA256(secret, timestamp + "\n" + tenThaoTac + "\n" + sku)); lech gio qua 5 phut bi tu choi.
 * Loi tra ve la SOAP Fault, khong lo ly do cu the khi sai khoa/chu ky.
 */
@Component
public class PartnerSoapAuthInterceptor implements EndpointInterceptor {

	static final String NS = "http://toyland.vn/soap/partner";
	private static final long MAX_SKEW_SECONDS = 300;

	private final PartnerRepository partners;

	public PartnerSoapAuthInterceptor(PartnerRepository partners) {
		this.partners = partners;
	}

	@Override
	public boolean handleRequest(MessageContext ctx, Object endpoint) throws Exception {
		SoapMessage msg = (SoapMessage) ctx.getRequest();
		SoapHeader header = msg.getSoapHeader();
		SoapHeaderElement authEl = null;
		if (header != null) {
			var it = header.examineHeaderElements(new QName(NS, "Auth"));
			if (it.hasNext()) {
				authEl = it.next();
			}
		}
		if (authEl == null) {
			throw new PartnerSoapException("Thiếu tiêu đề Auth (apiKey, timestamp, signature)");
		}
		Element auth = toElement(authEl.getSource());
		String apiKey = text(auth, "apiKey");
		String ts = text(auth, "timestamp");
		String sig = text(auth, "signature");
		if (apiKey == null || ts == null || sig == null) {
			throw new PartnerSoapException("Tiêu đề Auth thiếu apiKey, timestamp hoặc signature");
		}
		long tsSec;
		try {
			tsSec = Long.parseLong(ts.trim());
		} catch (NumberFormatException e) {
			throw new PartnerSoapException("timestamp phải là số giây Unix");
		}
		if (Math.abs(Instant.now().getEpochSecond() - tsSec) > MAX_SKEW_SECONDS) {
			throw new PartnerSoapException("timestamp lệch quá 5 phút so với máy chủ");
		}
		Partner partner = partners.findByApiKey(apiKey.trim()).orElse(null);
		if (partner == null || !partner.isActive()) {
			throw new PartnerSoapException("Khóa API hoặc chữ ký không hợp lệ");
		}
		Element payload = toElement(((org.springframework.ws.soap.SoapBody) msg.getSoapBody()).getPayloadSource());
		String op = payload.getLocalName();
		String sku = text(payload, "sku");
		String expected = PartnerSigner.sign(partner.getSecret(), ts.trim() + "\n" + op + "\n" + (sku == null ? "" : sku.trim()));
		if (!PartnerSigner.constantTimeEquals(expected, sig.trim().toLowerCase())) {
			throw new PartnerSoapException("Khóa API hoặc chữ ký không hợp lệ");
		}
		partner.setLastUsedAt(LocalDateTime.now());
		partners.save(partner);
		return true;
	}

	private static Element toElement(javax.xml.transform.Source source) throws Exception {
		Transformer t = TransformerFactory.newInstance().newTransformer();
		DOMResult result = new DOMResult();
		t.transform(source, result);
		Node n = result.getNode();
		return n instanceof Document d ? d.getDocumentElement() : (Element) n.getFirstChild();
	}

	private static String text(Element parent, String local) {
		for (Node c = parent.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c instanceof Element e && local.equals(e.getLocalName())) {
				return e.getTextContent();
			}
		}
		return null;
	}

	@Override
	public boolean handleResponse(MessageContext ctx, Object endpoint) {
		return true;
	}

	@Override
	public boolean handleFault(MessageContext ctx, Object endpoint) {
		return true;
	}

	@Override
	public void afterCompletion(MessageContext ctx, Object endpoint, Exception ex) {
	}
}
