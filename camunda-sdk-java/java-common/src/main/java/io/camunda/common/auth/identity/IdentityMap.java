package io.camunda.common.auth.identity;

import io.camunda.common.auth.Product;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains mapping between products and their Identity and IdentityConfiguration
 */
public class IdentityMap {

  private final Map<Product, IdentityContainer> map;

  public IdentityMap() {
    map = new HashMap<>();
  }

  public void addProduct(Product product, IdentityContainer identityContainer) {
    map.put(product, identityContainer);
  }

  public IdentityContainer get(Product product) {
    return map.get(product);
  }

}
