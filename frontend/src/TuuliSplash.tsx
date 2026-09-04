import { CSSProperties, useCallback, useEffect, useState } from "react";

const SPLASH_VISIBLE_MS = 2500;
const SPLASH_FADE_MS = 600;

type Props = {
  onComplete: () => void;
};

export default function TuuliSplash({ onComplete }: Props) {
  const [leaving, setLeaving] = useState(false);

  const dismiss = useCallback(() => setLeaving(true), []);

  useEffect(() => {
    const timeout = window.setTimeout(dismiss, SPLASH_VISIBLE_MS);
    return () => window.clearTimeout(timeout);
  }, [dismiss]);

  useEffect(() => {
    if (!leaving) return;
    const reducedMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const timeout = window.setTimeout(onComplete, reducedMotion ? 0 : SPLASH_FADE_MS);
    return () => window.clearTimeout(timeout);
  }, [leaving, onComplete]);

  useEffect(() => {
    const handleKeyDown = (event: globalThis.KeyboardEvent) => {
      if (["Enter", " ", "Escape"].includes(event.key)) {
        event.preventDefault();
        dismiss();
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [dismiss]);

  return (
    <div
      aria-label="TUULI"
      className={`tuuli-splash${leaving ? " is-leaving" : ""}`}
      onPointerDown={dismiss}
      role="img"
      style={{ "--tuuli-splash-fade": `${SPLASH_FADE_MS}ms` } as CSSProperties}
    >
      <img alt="TUULI" draggable="false" src="/tuuli-logo-white.png" />
    </div>
  );
}
