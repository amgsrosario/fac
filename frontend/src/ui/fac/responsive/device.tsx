import { ReactNode, useEffect, useMemo, useState } from "react";

export type DeviceClass = "mobile" | "tablet" | "desktop";

export const FAC_BREAKPOINTS = {
  mobileMax: 767,
  tabletMax: 1023
} as const;

function classifyDevice(width: number): DeviceClass {
  if (width <= FAC_BREAKPOINTS.mobileMax) return "mobile";
  if (width <= FAC_BREAKPOINTS.tabletMax) return "tablet";
  return "desktop";
}

function currentDeviceClass(): DeviceClass {
  if (typeof window === "undefined") return "desktop";
  return classifyDevice(window.innerWidth);
}

export function useDeviceClass() {
  const [deviceClass, setDeviceClass] = useState<DeviceClass>(() => currentDeviceClass());

  useEffect(() => {
    const controller = new AbortController();
    const update = () => setDeviceClass(currentDeviceClass());
    window.addEventListener("resize", update, { signal: controller.signal });
    window.addEventListener("orientationchange", update, { signal: controller.signal });

    const mobileQuery = window.matchMedia(`(max-width: ${FAC_BREAKPOINTS.mobileMax}px)`);
    const tabletQuery = window.matchMedia(`(min-width: ${FAC_BREAKPOINTS.mobileMax + 1}px) and (max-width: ${FAC_BREAKPOINTS.tabletMax}px)`);
    mobileQuery.addEventListener("change", update, { signal: controller.signal });
    tabletQuery.addEventListener("change", update, { signal: controller.signal });

    update();
    return () => controller.abort();
  }, []);

  return useMemo(() => ({
    deviceClass,
    isDesktop: deviceClass === "desktop",
    isMobile: deviceClass === "mobile",
    isTablet: deviceClass === "tablet"
  }), [deviceClass]);
}

export function DesktopOnly({ children }: { children: ReactNode }) {
  return useDeviceClass().isDesktop ? <>{children}</> : null;
}

export function MobileOnly({ children }: { children: ReactNode }) {
  return useDeviceClass().isMobile ? <>{children}</> : null;
}

export function ResponsiveSlot({
  desktop,
  mobile,
  tablet
}: {
  desktop: ReactNode;
  mobile: ReactNode;
  tablet?: ReactNode;
}) {
  const { deviceClass } = useDeviceClass();
  if (deviceClass === "mobile") return <>{mobile}</>;
  if (deviceClass === "tablet") return <>{tablet ?? desktop}</>;
  return <>{desktop}</>;
}
