/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_FAC_UI_MODE?: "legacy" | "commercial";
  readonly VITE_FAC_SAFT_DEMO?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
