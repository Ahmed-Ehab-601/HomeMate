/**
 * Generates time ranges based on the grouping type (year, month, or day)
 * @param {string} grouping - The grouping type ('year', 'month', or 'day')
 * @param {string} year - The selected year
 * @param {string} month - The selected month in 'YYYY-MM' format
 * @param {string} date - The selected date in 'YYYY-MM-DD' format
 * @returns {Array} Array of time ranges with 'from' and 'to' ISO timestamps
 */
export const generateTimeRanges = (grouping, year, month, date) => {
    const now = new Date();
    const ranges = [];

    if (grouping === 'year') {
        const startYear = parseInt(year);
        const currentYear = now.getUTCFullYear();

        for (let y = startYear; y <= currentYear; y++) {
            const rangeStart = new Date(Date.UTC(y, 0, 1, 0, 0, 0));
            let rangeEnd = new Date(Date.UTC(y, 11, 31, 23, 59, 59));

            if (y === currentYear) {
                rangeEnd = new Date(now);
            }

            ranges.push({
                from: rangeStart.toISOString().slice(0, 19),
                to: rangeEnd.toISOString().slice(0, 19),
            });
        }
    }

    else if (grouping === 'month') {
        const [selectedYear, selectedMonth] = month.split('-').map(Number);

        const currentYear = now.getUTCFullYear();
        const currentMonth = now.getUTCMonth();

        let current = new Date(Date.UTC(selectedYear, selectedMonth - 1, 1));

        while (current <= now) {
            const y = current.getUTCFullYear();
            const m = current.getUTCMonth();

            const rangeStart = new Date(Date.UTC(y, m, 1, 0, 0, 0));
            let rangeEnd = new Date(Date.UTC(y, m + 1, 0, 23, 59, 59));

            if (y === currentYear && m === currentMonth) {
                rangeEnd = new Date(now);
            }

            if (rangeEnd > now) rangeEnd = new Date(now);

            ranges.push({
                from: rangeStart.toISOString().slice(0, 19),
                to: rangeEnd.toISOString().slice(0, 19),
            });

            current = new Date(Date.UTC(y, m + 1, 1));
        }
    }

    else if (grouping === 'day') {
        const selected = new Date(date);

        let current = new Date(Date.UTC(
            selected.getUTCFullYear(),
            selected.getUTCMonth(),
            selected.getUTCDate()
        ));

        while (current <= now) {
            const y = current.getUTCFullYear();
            const m = current.getUTCMonth();
            const d = current.getUTCDate();

            const rangeStart = new Date(Date.UTC(y, m, d, 0, 0, 0));
            let rangeEnd = new Date(Date.UTC(y, m, d, 23, 59, 59));

            if (rangeEnd > now) {
                rangeEnd = new Date(now);
            }

            ranges.push({
                from: rangeStart.toISOString().slice(0, 19),
                to: rangeEnd.toISOString().slice(0, 19),
            });

            current = new Date(Date.UTC(y, m, d + 1));
        }
    }

    return ranges;
};

/**
 * Formats a time range label based on the grouping type
 * @param {Object} range - The time range object with 'from' and 'to' properties
 * @param {string} grouping - The grouping type ('year', 'month', or 'day')
 * @returns {string} Formatted label for the time range
 */
export const formatRangeLabel = (range, grouping) => {
    const from = new Date(range.from);

    if (grouping === 'year') {
        return from.getUTCFullYear().toString();
    } else if (grouping === 'month') {
        const monthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        return `${monthNames[from.getUTCMonth()]} ${from.getUTCFullYear()}`;
    } else if (grouping === 'day') {
        return from.toISOString().split('T')[0];
    }
    return '';
};
