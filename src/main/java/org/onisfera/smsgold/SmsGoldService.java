package org.onisfera.smsgold;

import com.hfx.keycloak.SmsException;
import com.hfx.keycloak.VerificationCodeRepresentation;
import com.hfx.keycloak.spi.SmsService;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.MessageFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;
import org.onisfera.smsgold.client.HttpClientFactory;
import org.onisfera.smsgold.config.SmsConfig;

public class SmsGoldService implements SmsService<Object> {

  private final KeycloakSession session;
  private final SmsConfig config;
  private final HttpClientFactory httpClientFactory;

  private static final Logger log = Logger.getLogger(SmsGoldService.class);

  public SmsGoldService(KeycloakSession session) {
    this.session = session;
    this.config = new SmsConfig();
    this.httpClientFactory = new HttpClientFactory();
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
    form.add(new BasicNameValuePair("pass",config.getSecret()));
    form.add(new BasicNameValuePair("action", "send"));
    form.add(new BasicNameValuePair("number", preparePhoneNumber(number)));
    form.add(new BasicNameValuePair("text", MessageFormat.format(config.getTemplate(), code)));

    HttpEntity entity = new UrlEncodedFormEntity(form, StandardCharsets.UTF_8);
    HttpPost post = new HttpPost(URI.create(config.getProviderUrl()));

    try /*(CloseableHttpResponse response = httpClientFactory.getClient().execute(post))*/ {
      //assert2xxStatusCode(response);
      throw new ClientProtocolException("test runtime exception");
    } catch (ClientProtocolException e) {
      throw new SmsException("Http protocol exception ", e);
    } catch (IOException e) {
      throw new SmsException("I/O exception ", e);
    }

    //return true;
  }

  private static String preparePhoneNumber(final String phoneNumber) {
    // remove extra characters
    String preparedPhoneNumber = removeChars(phoneNumber, '+', ' ', '(', ')');

    // replace '8' as leading character with '7'
    if (preparedPhoneNumber.charAt(0) == '8') {
      return "7" + preparedPhoneNumber.substring(1);
    }

    return preparedPhoneNumber;
  }

  private static String removeChars(String source, char... chars) {
    StringBuilder sb = new StringBuilder();
    CharacterIterator it = new StringCharacterIterator(source);
    for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
      switch (c) {
        case '+':
        case ' ':
        case '(':
        case ')':
          break;
        default:
          sb.append(c);
      }
    }
    return sb.toString();
  }

  @Override
  public void close() {
    // none
  }

}
