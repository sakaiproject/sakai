import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import timezone from "dayjs/plugin/timezone";

dayjs.extend(utc);
dayjs.extend(timezone);

export function getPortal() {
  try {
    return window.portal || window.parent?.portal || null;
  } catch (e) {
    return window.portal || null;
  }
}

export function getUserTimezone() {
  const tz = getPortal()?.user?.timezone;
  if (tz) {
    return tz;
  }
  try {
    return dayjs.tz.guess() || "UTC";
  } catch (e) {
    return "UTC";
  }
}

/**
 * "Now" using Sakai server clock when available, shown in the user's preferred timezone.
 */
export function nowInUserTimezone() {
  const p = getPortal();
  if (p?.serverTimeMillis != null && p.serverTimeMillis !== "") {
    return dayjs(Number(p.serverTimeMillis)).tz(getUserTimezone());
  }
  return dayjs().tz(getUserTimezone());
}

/**
 * Parse stored model value (ISO from API or legacy naive string) to an instant in user TZ for display.
 */
export function parseModelToUserTz(val) {
  if (val == null || val === "") {
    return null;
  }
  const s = String(val).trim();
  const tz = getUserTimezone();
  if (/[zZ]|[+-]\d{2}:?\d{2}$/.test(s)) {
    const d = dayjs(s);
    return d.isValid() ? d.tz(tz) : null;
  }
  const m = s.match(/^(\d{4}-\d{2}-\d{2})[T ](\d{1,2}:\d{2})(?::\d{2})?/);
  if (m) {
    const d = dayjs.tz(`${m[1]} ${m[2]}`, "YYYY-MM-DD H:mm", tz);
    return d.isValid() ? d : null;
  }
  const d = dayjs(s);
  return d.isValid() ? d.tz(tz) : null;
}

export function formatPickerInitial(val) {
  if (!val) {
    return nowInUserTimezone().format("YYYY-MM-DD HH:mm");
  }
  const d = parseModelToUserTz(val);
  return d && d.isValid()
    ? d.format("YYYY-MM-DD HH:mm")
    : nowInUserTimezone().format("YYYY-MM-DD HH:mm");
}

export function formatForDatetimeLocalControl(val) {
  const d = val ? parseModelToUserTz(val) : nowInUserTimezone();
  if (!d || !d.isValid()) {
    return nowInUserTimezone().format("YYYY-MM-DDTHH:mm");
  }
  return d.format("YYYY-MM-DDTHH:mm");
}

/**
 * Interpret native datetime-local / picker output as wall time in the user's Sakai timezone → UTC ISO.
 */
export function parseControlValueToIso(raw) {
  if (!raw) {
    return "";
  }
  const tz = getUserTimezone();
  const normalized = String(raw).trim().replace(" ", "T").slice(0, 16);
  const d = dayjs.tz(normalized, "YYYY-MM-DDTHH:mm", tz);
  return d.isValid() ? d.toISOString() : "";
}
