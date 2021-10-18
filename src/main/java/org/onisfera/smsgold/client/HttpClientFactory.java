package org.onisfera.smsgold.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientFactory {

  private volatile CloseableHttpClient cachedKeycloakClient;

  public synchronized CloseableHttpClient getClient() {
    if (cachedKeycloakClient == null) {
      cachedKeycloakClient = getHttpClient();
    }

    return cachedKeycloakClient;
  }

  private CloseableHttpClient getHttpClient() {
    if (cachedKeycloakClient != null) {
      return cachedKeycloakClient;
    }

    cachedKeycloakClient = HttpClientBuilder.create()
        .disableCookieManagement()
        .build();

    return cachedKeycloakClient;
  }

}
