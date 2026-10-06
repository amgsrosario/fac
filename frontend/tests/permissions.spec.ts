import { test, expect } from "@playwright/test";
import { hasCapability, PERMISSIONS } from "../src/permissions";

test("capacidades ausentes nunca permitem uma ação", () => {
  for (const permission of PERMISSIONS) {
    expect(hasCapability(null, permission)).toBe(false);
    expect(hasCapability(undefined, permission)).toBe(false);
    expect(hasCapability({}, permission)).toBe(false);
    expect(hasCapability({ permissoes: [] }, permission)).toBe(false);
  }
});

test("a apresentação depende da capacidade explícita recebida", () => {
  const session = { permissoes: ["DOCUMENTO_CONSULTAR", "DOCUMENTO_OBTER_PDF"] };
  expect(hasCapability(session, "DOCUMENTO_CONSULTAR")).toBe(true);
  expect(hasCapability(session, "DOCUMENTO_OBTER_PDF")).toBe(true);
  expect(hasCapability(session, "DOCUMENTO_CRIAR")).toBe(false);
  expect(hasCapability(session, "CONFIGURACAO_GERIR")).toBe(false);
});
