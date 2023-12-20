package io.camunda.common.auth;

/**
 * Contains credential for particular product. Used for JWT authentication.
 */
public class JwtCredential {

  public JwtCredential(String clientId, String clientSecret, String audience, String authUrl) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.audience = audience;
    this.authUrl = authUrl;
  }

  public JwtCredential(String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  public JwtCredential(String clientId, String clientSecret, String audience) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.audience = audience;
  }

  String clientId;
  String clientSecret;
  String audience;
  String authUrl;

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getAudience() {
    return audience;
  }

  public String getAuthUrl() {
    return authUrl;
  }

}
