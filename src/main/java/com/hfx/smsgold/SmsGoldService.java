package com.hfx.smsgold;

import com.hfx.keycloak.SmsException;
import com.hfx.keycloak.VerificationCodeRepresentation;
import com.hfx.keycloak.spi.SmsService;
import com.hfx.smsgold.client.HttpClientFactory;
import com.hfx.smsgold.config.SmsConfig;
import com.hfx.smsgold.util.PhoneNumberUtils;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SmsGoldService implements SmsService<Object> {

  private final KeycloakSession session;
  private final SmsConfig config;
  private final HttpClientFactory httpClientFactory;
  private final DocumentBuilderFactory documentBuilderFactory;

  private static final Logger log = Logger.getLogger(SmsGoldService.class);

  public SmsGoldService(KeycloakSession session) {
    this.session = session;
    this.config = new SmsConfig();
    this.httpClientFactory = new HttpClientFactory();
    documentBuilderFactory = DocumentBuilderFactory.newInstance();

    try {
      documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    } catch (final ParserConfigurationException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public boolean send(String phoneNumber, Map<String, ? super Object> map) throws SmsException {
    // no real usage in keycloak-phone-authenticator project
    log.info("Sending message to phone number [" + phoneNumber + "] with parameters " + map);

    return true;
  }

  @Override
  public boolean sendVerificationCode(VerificationCodeRepresentation verificationCodeRepresentation,
      Map<String, ? super Object> map) throws SmsException {
    String code = verificationCodeRepresentation.getCode();
    String number = verificationCodeRepresentation.getPhoneNumber();

    List<BasicNameValuePair> form = new ArrayList<>(5);
    form.add(new BasicNameValuePair("user", config.getUsername()));
    form.add(new BasicNameValuePair("pass", config.getSecret()));
    form.add(new BasicNameValuePair("action", "send"));
    form.add(new BasicNameValuePair("number", PhoneNumberUtils.preparePhoneNumber(number)));
    form.add(new BasicNameValuePair("text", MessageFormat.format(config.getTemplate(), code)));

    HttpEntity entity = new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);
    HttpPost post = new HttpPost(URI.create(config.getProviderUrl()));
    post.setEntity(entity);

    log.debug("Sending verification code [" + code + "] to phone number [" + number + "]");

    try (CloseableHttpResponse response = httpClientFactory.getClient().execute(post)) {
      assert2xxStatusCode(response);

      Document xmlDocument = documentBuilderFactory.newDocumentBuilder()
          .parse(response.getEntity().getContent());
      xmlDocument.normalizeDocument();

      NodeList smsTag = xmlDocument.getElementsByTagName("sms");
      if (smsTag.getLength() < 1) {
        throw new SmsException("Incorrect provider response \n" + toString(xmlDocument));
      }

      String error = smsTag.item(0).getAttributes().getNamedItem("err").getNodeValue();

      if (!isValidResponse(error)) {
        String smsId = smsTag.item(0).getAttributes().getNamedItem("sms_id").getNodeValue();
        throw new SmsException("Incorrect sms response: error=" + error + " smsId=" + smsId);
      }

    } catch (ParserConfigurationException | SAXException e) {
      throw new SmsException("Failed to parse xml response", e);
    } catch (ClientProtocolException e) {
      throw new SmsException("Http protocol exception", e);
    } catch (IOException e) {
      throw new SmsException("I/O exception", e);
    }

    return true;
  }

  private static boolean isValidResponse(final String errorText) {
    return errorText == null || "".equals(errorText.trim()) || "0".equals(errorText.trim());
  }

  private static void assert2xxStatusCode(final CloseableHttpResponse response)
      throws HttpResponseException {
    assertStatusCodeWithinAcceptedRange(response, 200, 201, 204);
  }

  public static void assertStatusCodeWithinAcceptedRange(final CloseableHttpResponse response,
      int... acceptedCodes) throws HttpResponseException {
    int statusCode = response.getStatusLine().getStatusCode();
    if (IntStream.of(acceptedCodes).noneMatch(i -> i == statusCode)) {
      throw new HttpResponseException(statusCode, "Unexpected response status code: " + statusCode);
    }
  }

  public static String toString(Document doc) {
    try {
      StringWriter sw = new StringWriter();
      TransformerFactory tf = TransformerFactory.newInstance();
      Transformer transformer = tf.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      transformer.transform(new DOMSource(doc), new StreamResult(sw));
      return sw.toString();
    } catch (Exception ex) {
      throw new RuntimeException("Error converting to String", ex);
    }
  }

  @Override
  public void close() {
    // none
  }

}
