package io.camunda.common.auth;

import io.camunda.common.exception.SdkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Map;

/**
 * Default implementation for Authentication
 * Typically you will replace this by a proper authentication by setting the right properties
 */
public class DefaultNoopAuthentication implements Authentication {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String errorMessage = "Unable to determine authentication. Please check your configuration";

  @Override
  public Authentication build() {
    LOG.error(errorMessage);
    return this;
  }

  @Override
  public void resetToken(Product product) {
    throw new SdkException(errorMessage);
  }

  @Override
  public Map.Entry<String, String> getTokenHeader(Product product) {
    throw new UnsupportedOperationException("Unable to determine authentication");
  }
}
