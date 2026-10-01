import { defineConfig } from '@playwright/test';
import { fileURLToPath } from 'node:url';
const root = fileURLToPath(new URL('.', import.meta.url)).replace(/[\\/]$/, '');
const vite = fileURLToPath(new URL('./node_modules/vite/bin/vite.js', import.meta.url));
export default defineConfig({
  testDir: './tests', workers: 1, timeout: 30000,
  use: { baseURL: 'http://127.0.0.1:4175', channel: process.env.PLAYWRIGHT_CHANNEL || 'msedge', headless: true, viewport: { width: 1440, height: 1000 } },
  webServer: process.env.LOOKUP_EXTERNAL_SERVER ? undefined : { command: `"${process.execPath}" "${vite}" "${root}" --host 127.0.0.1 --port 4175`, url: 'http://127.0.0.1:4175', reuseExistingServer: false }
});
