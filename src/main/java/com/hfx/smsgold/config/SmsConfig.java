package com.hfx.smsgold.config;

public class SmsConfig {

  private static final String PARAM_SMS_TEMPLATE = "SMS_TEMPLATE";
  private static final String PARAM_USERNAME = "SMS_USERNAME";
  private static final String PARAM_SECRET = "SMS_SECRET";
  private static final String PARAM_PROVIDER_URL = "SMS_PROVIDER_URL";

  private final String template;
  private final String username;
  private final String secret;
  private final String providerUrl;

  public SmsConfig() {
    this.template = System.getenv(PARAM_SMS_TEMPLATE);
    assertNotEmpty(PARAM_SMS_TEMPLATE, this.template);

    this.username = System.getenv(PARAM_USERNAME);
    assertNotEmpty(PARAM_USERNAME, this.username);

    this.secret = System.getenv(PARAM_SECRET);
    assertNotEmpty(PARAM_SECRET, this.secret);

    this.providerUrl = System.getenv(PARAM_PROVIDER_URL);
    assertNotEmpty(PARAM_PROVIDER_URL, this.providerUrl);
  }

  public String getTemplate() {
    return template;
  }

  public String getUsername() {
    return username;
  }

  public String getSecret() {
    return secret;
  }

  public String getProviderUrl() {
    return providerUrl;
  }

  private static void assertNotEmpty(String name, String value) {
    if (value == null || value.trim().length() == 0) {
      throw new IllegalArgumentException("argument '" + name + "' must not be empty");
    }
  }

}
