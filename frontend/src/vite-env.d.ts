/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_FAC_UI_MODE?: "legacy" | "commercial";
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
