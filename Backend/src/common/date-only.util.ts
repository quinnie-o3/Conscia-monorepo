function padDatePart(value: number) {
  return value.toString().padStart(2, '0');
}

export function formatDateOnly(date: Date) {
  return [
    date.getFullYear(),
    padDatePart(date.getMonth() + 1),
    padDatePart(date.getDate()),
  ].join('-');
}

export function parseDateOnly(value: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value);

  if (!match) {
    return new Date(Number.NaN);
  }

  const [, year, month, day] = match;

  return new Date(
    Number.parseInt(year, 10),
    Number.parseInt(month, 10) - 1,
    Number.parseInt(day, 10),
  );
}

export function formatDateOnlyInTimeZone(date: Date, timeZone: string) {
  const formatter = new Intl.DateTimeFormat('en-CA', {
    timeZone,
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  });
  const parts = formatter.formatToParts(date);
  const year = parts.find((part) => part.type === 'year')?.value;
  const month = parts.find((part) => part.type === 'month')?.value;
  const day = parts.find((part) => part.type === 'day')?.value;

  if (!year || !month || !day) {
    throw new RangeError(`Unable to resolve date in timezone: ${timeZone}`);
  }

  return `${year}-${month}-${day}`;
}

export function getCurrentDateOnly(timeZone?: string) {
  if (!timeZone) {
    return formatDateOnly(new Date());
  }

  return formatDateOnlyInTimeZone(new Date(), timeZone);
}
