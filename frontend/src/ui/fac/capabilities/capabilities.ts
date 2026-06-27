export type ProductProfile = "SERVICES_SIMPLE" | "FULL";

export type FeatureKey =
  | "ADVANCED_ARTICLE_FIELDS"
  | "ADVANCED_CLIENT_FIELDS"
  | "ADVANCED_DOCUMENT_DISCOUNTS"
  | "ADVANCED_WAREHOUSES"
  | "TECHNICAL_TABLES"
  | "TRANSPORT_FIELDS";

export type Capabilities = Readonly<Record<FeatureKey, boolean>>;

const fullCapabilities: Capabilities = {
  ADVANCED_ARTICLE_FIELDS: true,
  ADVANCED_CLIENT_FIELDS: true,
  ADVANCED_DOCUMENT_DISCOUNTS: true,
  ADVANCED_WAREHOUSES: true,
  TECHNICAL_TABLES: true,
  TRANSPORT_FIELDS: true
};

const servicesSimpleCapabilities: Capabilities = {
  ADVANCED_ARTICLE_FIELDS: false,
  ADVANCED_CLIENT_FIELDS: false,
  ADVANCED_DOCUMENT_DISCOUNTS: false,
  ADVANCED_WAREHOUSES: false,
  TECHNICAL_TABLES: false,
  TRANSPORT_FIELDS: false
};

export const PRODUCT_PROFILE_CAPABILITIES: Readonly<Record<ProductProfile, Capabilities>> = {
  FULL: fullCapabilities,
  SERVICES_SIMPLE: servicesSimpleCapabilities
};

export const DEFAULT_PRODUCT_PROFILE: ProductProfile = "SERVICES_SIMPLE";

export function getCapabilities(profile: ProductProfile = DEFAULT_PRODUCT_PROFILE): Capabilities {
  return PRODUCT_PROFILE_CAPABILITIES[profile];
}

export function hasCapability(feature: FeatureKey, capabilities: Capabilities = getCapabilities()) {
  return capabilities[feature];
}
