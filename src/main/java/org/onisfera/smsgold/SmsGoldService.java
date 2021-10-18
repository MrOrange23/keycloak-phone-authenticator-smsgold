package org.onisfera.smsgold;

import com.hfx.keycloak.SmsException;
import com.hfx.keycloak.VerificationCodeRepresentation;
import com.hfx.keycloak.spi.SmsService;
import java.util.Map;
import org.jboss.logging.Logger;
import org.keycloak.models.KeycloakSession;

public class SmsGoldService implements SmsService<Object> {

  private final KeycloakSession session;

  private static final Logger log = Logger.getLogger(SmsGoldService.class);

  public SmsGoldService(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public boolean send(String phoneNumber, Map<String, ? super Object> map) throws SmsException {
    log.info("Sending message to phone number [" + phoneNumber + "] with parameters " + map);

    return true;
  }

  @Override
  public boolean sendVerificationCode(VerificationCodeRepresentation verificationCodeRepresentation,
      Map<String, ? super Object> map) throws SmsException {
    String code = verificationCodeRepresentation.getCode();
    String number = verificationCodeRepresentation.getPhoneNumber();
    log.info(
        "Sending verification code [" + code + "] to phone number [" + number + "] with parameters "
            + map);

    return true;
  }

  @Override
  public void close() {

  }

}
