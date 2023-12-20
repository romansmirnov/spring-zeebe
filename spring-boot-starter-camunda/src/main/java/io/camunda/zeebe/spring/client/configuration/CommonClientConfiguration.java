package io.camunda.zeebe.spring.client.configuration;

import io.camunda.common.auth.*;
import io.camunda.common.auth.identity.IdentityContainer;
import io.camunda.common.auth.identity.IdentityMap;
import io.camunda.identity.sdk.Identity;
import io.camunda.identity.sdk.IdentityConfiguration;
import io.camunda.zeebe.spring.client.properties.*;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.springframework.util.StringUtils.hasText;

@EnableConfigurationProperties(CommonConfigurationProperties.class)
public class CommonClientConfiguration {


  @Autowired(required = false)
  CommonConfigurationProperties commonConfigurationProperties;

  @Autowired(required = false)
  ZeebeClientConfigurationProperties zeebeClientConfigurationProperties;

  @Autowired(required = false)
  OperateClientConfigurationProperties operateClientConfigurationProperties;

  @Autowired(required = false)
  ConsoleClientConfigurationProperties consoleClientConfigurationProperties;

  @Autowired(required = false)
  OptimizeClientConfigurationProperties optimizeClientConfigurationProperties;

  @Autowired(required = false)
  TasklistClientConfigurationProperties tasklistClientConfigurationProperties;

  // TODO: Remove below properties when we deprecate camunda.[product].client.*
  @Autowired(required = false)
  CamundaOperateClientConfigurationProperties camundaOperateClientConfigurationProperties;

  @Autowired
  private IdentityConfiguration identityConfigurationFromProperties;

  @Bean
  public Authentication authentication() {
    // TODO: Refactor
    // If there is Identity props, then its Self-Managed with JWT authentication
    if (hasText(identityConfigurationFromProperties.getClientId())) {
      JwtConfig jwtConfig = configureSelfManagedJwtConfig();
      IdentityMap identityMap = configureIdentities(jwtConfig);
      return SelfManagedAuthentication.builder()
        .jwtConfig(jwtConfig)
        .identityMap(identityMap)
        .build();
    }
    if (zeebeClientConfigurationProperties != null) {
      // check if Zeebe has clusterId provided, then must be SaaS
      if (zeebeClientConfigurationProperties.getCloud().getClusterId() != null) {
        return SaaSAuthentication.builder()
          .jwtConfig(configureJwtConfig())
          .build();
      } else if (zeebeClientConfigurationProperties.getBroker().getGatewayAddress() != null) {
        // figure out if Self-Managed JWT or Self-Managed Basic
        // TODO: Remove when we deprecate camunda.[product].client.*
        if (camundaOperateClientConfigurationProperties != null) {
          if (camundaOperateClientConfigurationProperties.getKeycloakUrl() != null) {
            return SelfManagedAuthentication.builder()
              .jwtConfig(configureJwtConfig())
              .keycloakUrl(camundaOperateClientConfigurationProperties.getKeycloakUrl())
              .keycloakRealm(camundaOperateClientConfigurationProperties.getKeycloakRealm())
              .build();
          } else if (camundaOperateClientConfigurationProperties.getUsername() != null && camundaOperateClientConfigurationProperties.getPassword() != null) {
            SimpleConfig simpleConfig = new SimpleConfig();
            SimpleCredential simpleCredential = new SimpleCredential(camundaOperateClientConfigurationProperties.getUsername(), camundaOperateClientConfigurationProperties.getPassword());
            simpleConfig.addProduct(Product.OPERATE, simpleCredential);
            return SimpleAuthentication.builder()
              .simpleConfig(simpleConfig)
              .simpleUrl(camundaOperateClientConfigurationProperties.getUrl())
              .build();
          }
        }

        if (commonConfigurationProperties != null) {
          if (commonConfigurationProperties.getKeycloak().getUrl() != null) {
            return SelfManagedAuthentication.builder()
              .jwtConfig(configureJwtConfig())
              .keycloakUrl(commonConfigurationProperties.getKeycloak().getUrl())
              .keycloakRealm(commonConfigurationProperties.getKeycloak().getRealm())
              .build();
          } else if (commonConfigurationProperties.getUsername() != null && commonConfigurationProperties.getPassword() != null) {
            SimpleConfig simpleConfig = new SimpleConfig();
            SimpleCredential simpleCredential = new SimpleCredential(commonConfigurationProperties.getUsername(), commonConfigurationProperties.getPassword());
            simpleConfig.addProduct(Product.OPERATE, simpleCredential);
            return SimpleAuthentication.builder()
              .simpleConfig(simpleConfig)
              .simpleUrl(commonConfigurationProperties.getUrl())
              .build();
          }
        }
      }
    }
    return new DefaultNoopAuthentication().build();
  }

  /**
   * This is experimental and subject to change.
   * Fills out the JWT config for Self-Managed based on a predefined order.
   * First it checks for Identity properties, then it goes for Individual Client properties, and then uses Zeebe as the last fallback in that order.
   */
  private JwtConfig configureSelfManagedJwtConfig() {
    JwtConfig jwtConfig = new JwtConfig();

    if (hasText(identityConfigurationFromProperties.getClientId())) {
      String DEFAULT_AUDIENCE = "default-audience";
      String audience = ObjectUtils.defaultIfNull(identityConfigurationFromProperties.getAudience(), DEFAULT_AUDIENCE);
      jwtConfig.addProduct(Product.ZEEBE, new JwtCredential(identityConfigurationFromProperties.getClientId(), identityConfigurationFromProperties.getClientSecret(), audience));
      if (operateClientConfigurationProperties != null) {
        if (operateClientConfigurationProperties.getEnabled()) {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(identityConfigurationFromProperties.getClientId(), identityConfigurationFromProperties.getClientSecret(), audience));
        }
      }
      else if (camundaOperateClientConfigurationProperties != null) {
        if (camundaOperateClientConfigurationProperties.getEnabled()) {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(identityConfigurationFromProperties.getClientId(), identityConfigurationFromProperties.getClientSecret(), audience));
        }
      }
    } else if (hasText(operateClientConfigurationProperties.getClientId())) {
      jwtConfig.addProduct(Product.ZEEBE, new JwtCredential(zeebeClientConfigurationProperties.getCloud().getClientId(), zeebeClientConfigurationProperties.getCloud().getClientSecret()));
      jwtConfig.addProduct(Product.OPERATE, new JwtCredential(operateClientConfigurationProperties.getClientId(), operateClientConfigurationProperties.getClientSecret()));
    } else if (hasText(camundaOperateClientConfigurationProperties.getClientId())) {
      jwtConfig.addProduct(Product.ZEEBE, new JwtCredential(zeebeClientConfigurationProperties.getCloud().getClientId(), zeebeClientConfigurationProperties.getCloud().getClientSecret()));
      jwtConfig.addProduct(Product.OPERATE, new JwtCredential(camundaOperateClientConfigurationProperties.getClientId(), camundaOperateClientConfigurationProperties.getClientSecret()));
    } else {
      throw new RuntimeException("Unable to determine which client id and secret to use for authentication");
    }
    return jwtConfig;
  }

  private IdentityMap configureIdentities(JwtConfig jwtConfig) {
    IdentityMap identityMap = new IdentityMap();

    // zeebe identity
    IdentityConfiguration zeebeIdentityConfiguration = new IdentityConfiguration.Builder()
      .withBaseUrl(identityConfigurationFromProperties.getBaseUrl())
      .withIssuer(identityConfigurationFromProperties.getIssuer())
      .withIssuerBackendUrl(identityConfigurationFromProperties.getIssuerBackendUrl())
      .withClientId(jwtConfig.getProduct(Product.ZEEBE).getClientId())
      .withClientSecret(jwtConfig.getProduct(Product.ZEEBE).getClientSecret())
      .withAudience(jwtConfig.getProduct(Product.ZEEBE).getAudience())
      .withType(identityConfigurationFromProperties.getType().name())
      .build();
    Identity zeebeIdentity = new Identity(zeebeIdentityConfiguration);
    IdentityContainer zeebeIdentityContainer = new IdentityContainer(zeebeIdentity, zeebeIdentityConfiguration);
    identityMap.addProduct(Product.ZEEBE, zeebeIdentityContainer);

    // operate identity
    if (operateClientConfigurationProperties != null) {
      if (operateClientConfigurationProperties.getEnabled()) {
        IdentityContainer operateIdentityContainer = configureOperateIdentityContainer(jwtConfig);
        identityMap.addProduct(Product.OPERATE, operateIdentityContainer);
      }
    } else if (camundaOperateClientConfigurationProperties != null) {
      if (camundaOperateClientConfigurationProperties.getEnabled()) {
        IdentityContainer operateIdentityContainer = configureOperateIdentityContainer(jwtConfig);
        identityMap.addProduct(Product.OPERATE, operateIdentityContainer);
      }
    }

    return identityMap;
  }

  private IdentityContainer configureOperateIdentityContainer(JwtConfig jwtConfig) {
    IdentityConfiguration operateIdentityConfiguration = new IdentityConfiguration.Builder()
      .withBaseUrl(identityConfigurationFromProperties.getBaseUrl())
      .withIssuer(identityConfigurationFromProperties.getIssuer())
      .withIssuerBackendUrl(identityConfigurationFromProperties.getIssuerBackendUrl())
      .withClientId(jwtConfig.getProduct(Product.OPERATE).getClientId())
      .withClientSecret(jwtConfig.getProduct(Product.OPERATE).getClientSecret())
      .withAudience(jwtConfig.getProduct(Product.OPERATE).getAudience())
      .withType(identityConfigurationFromProperties.getType().name())
      .build();
    Identity operateIdentity = new Identity(operateIdentityConfiguration);
      return new IdentityContainer(operateIdentity, operateIdentityConfiguration);
  }

  private JwtConfig configureJwtConfig() {
    JwtConfig jwtConfig = new JwtConfig();
    if (zeebeClientConfigurationProperties.isEnabled()) {
      if (zeebeClientConfigurationProperties.getCloud().getClientId() != null && zeebeClientConfigurationProperties.getCloud().getClientSecret() != null) {
        jwtConfig.addProduct(Product.ZEEBE, new JwtCredential(
          zeebeClientConfigurationProperties.getCloud().getClientId(),
          zeebeClientConfigurationProperties.getCloud().getClientSecret(),
          zeebeClientConfigurationProperties.getCloud().getAudience(),
          zeebeClientConfigurationProperties.getCloud().getAuthUrl())
        );
      } else if (commonConfigurationProperties.getClientId() != null && commonConfigurationProperties.getClientSecret() != null) {
        jwtConfig.addProduct(Product.ZEEBE, new JwtCredential(
          commonConfigurationProperties.getClientId(),
          commonConfigurationProperties.getClientSecret(),
          zeebeClientConfigurationProperties.getCloud().getAudience(),
          zeebeClientConfigurationProperties.getCloud().getAuthUrl())
        );
      }
    }

    String operateAuthUrl = zeebeClientConfigurationProperties.getCloud().getAuthUrl();
    String operateAudience = "operate.camunda.io";
    if (operateClientConfigurationProperties != null) {
      if (operateClientConfigurationProperties.getEnabled()) {
        if (operateClientConfigurationProperties.getAuthUrl() != null) {
          operateAuthUrl = operateClientConfigurationProperties.getAuthUrl();
        }
        if (operateClientConfigurationProperties.getBaseUrl() != null) {
          operateAudience = operateClientConfigurationProperties.getBaseUrl();
        }
        if (operateClientConfigurationProperties.getClientId() != null && operateClientConfigurationProperties.getClientSecret() != null) {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(
            operateClientConfigurationProperties.getClientId(),
            operateClientConfigurationProperties.getClientSecret(),
            operateAuthUrl,
            operateAudience)
          );
        } else if (commonConfigurationProperties.getClientId() != null && commonConfigurationProperties.getClientSecret() != null) {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(
            commonConfigurationProperties.getClientId(),
            commonConfigurationProperties.getClientSecret(),
            operateAuthUrl,
            operateAudience)
          );
        } else {
          // TODO: Remove this in the future, new property scheme shouldn't depend on Zeebe
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(
            zeebeClientConfigurationProperties.getCloud().getClientId(),
            zeebeClientConfigurationProperties.getCloud().getClientSecret(),
            operateAudience, operateAuthUrl)
          );
        }
      }
    }
    if (camundaOperateClientConfigurationProperties != null) {
      // TODO: Remove this else if block when we deprecate camunda.[product].client.*
      if (camundaOperateClientConfigurationProperties.getEnabled()) {
        if (camundaOperateClientConfigurationProperties.getAuthUrl() != null) {
          operateAuthUrl = camundaOperateClientConfigurationProperties.getAuthUrl();
        }
        if (camundaOperateClientConfigurationProperties.getBaseUrl() != null) {
          operateAudience = camundaOperateClientConfigurationProperties.getBaseUrl();
        }
        if (camundaOperateClientConfigurationProperties.getClientId() != null && camundaOperateClientConfigurationProperties.getClientSecret() != null) {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(camundaOperateClientConfigurationProperties.getClientId(), camundaOperateClientConfigurationProperties.getClientSecret(), operateAudience, operateAuthUrl));
        } else {
          jwtConfig.addProduct(Product.OPERATE, new JwtCredential(zeebeClientConfigurationProperties.getCloud().getClientId(), zeebeClientConfigurationProperties.getCloud().getClientSecret(), operateAudience, operateAuthUrl));
        }
      }
    }
    return jwtConfig;
  }
}
