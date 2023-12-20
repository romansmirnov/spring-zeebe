package io.camunda.common.auth;

import io.camunda.common.auth.identity.IdentityContainer;
import io.camunda.common.auth.identity.IdentityMap;
import io.camunda.common.json.JsonMapper;
import io.camunda.common.json.SdkObjectMapper;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.identity.sdk.authentication.Tokens;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SelfManagedAuthentication extends JwtAuthentication {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private String authUrl;

  // TODO: Check with Identity about upcoming IDPs to abstract this
  private String keycloakRealm = "camunda-platform";
  private String keycloakUrl;
  private JwtConfig jwtConfig;
  //private Map<Product, String> tokens;
  //private Map<Product, LocalDateTime> expirations;

  // TODO: have a single object mapper to be used all throughout the SDK, i.e.bean injection
  private JsonMapper jsonMapper = new SdkObjectMapper();

  private IdentityMap identityMap;

  public SelfManagedAuthentication() {
    //tokens = new HashMap<>();
  }

  public static SelfManagedAuthenticationBuilder builder() {
    return new SelfManagedAuthenticationBuilder();
  }

  public void setKeycloakRealm(String keycloakRealm) {
    this.keycloakRealm = keycloakRealm;
  }

  public void setKeycloakUrl(String keycloakUrl) {
    this.keycloakUrl = keycloakUrl;
  }

  public void setJwtConfig(JwtConfig jwtConfig) {
    this.jwtConfig = jwtConfig;
  }

  public void setIdentityMap(IdentityMap identityMap) {
    this.identityMap = identityMap;
  }

  @Override
  public Authentication build() {
    //authUrl = keycloakUrl+"/auth/realms/"+keycloakRealm+"/protocol/openid-connect/token";

    // reconfigure Identity with user provided fields
//    IdentityConfiguration.Builder newIdentityConfigurationBuilder = new IdentityConfiguration.Builder();
//    if (keycloakUrl != null) {
//      newIdentityConfigurationBuilder.withIssuerBackendUrl(keycloakUrl);
//    }



    //identity = new Identity(identityConfiguration1);


    //identityConfiguration.
    //identityConfiguration.getIssuerBackendUrl() = keycloakUrl;
    //jwtConfig.getMap().forEach(this::retrieveToken);
    return this;
  }



//  private void retrieveToken(Product product, JwtCredential jwtCredential) {
//    Identity identity = identityMap.get(product).getIdentity();
//    Tokens identityTokens = identity.authentication().requestToken(jwtCredential.audience);
//    //tokens.put(product, identityTokens.getAccessToken());
//    // TODO how to handle auto-refreshing of tokens?
//    //expirations.put(product, LocalDateTime.now().plusSeconds(identityTokens.getExpiresIn()));
//  }

//  private void retrieveToken(Product product) {
//    JwtCredential jwtCredential = jwtConfig.getMap().get(product);
//    retrieveToken(product, jwtCredential);
//  }

  @Override
  public Map.Entry<String, String> getTokenHeader(Product product) {
    //retrieveToken(product);
    Identity identity = identityMap.get(product).getIdentity();
    String audience = jwtConfig.getProduct(product).audience;
    Tokens identityTokens = identity.authentication().requestToken(audience);
    return new AbstractMap.SimpleEntry<>("Authorization", "Bearer " + identityTokens.getAccessToken());
  }
//
//  private void refreshToken() {
//    expirations.forEach((product, expiration) -> {
//      if (expiration.isAfter(LocalDateTime.now())) {
//        retrieveToken(product);
//      }
//    });
//  }
}
