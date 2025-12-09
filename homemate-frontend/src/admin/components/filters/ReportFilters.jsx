// ReportFilters.jsx
import React from 'react';
import { useAdminThemeMode } from '../../theme/ThemeProviderSwitcher';
import '../../styles/ReportFilters.css';

const ReportFilters = ({ filters, onFilterChange, onApplyFilters }) => {
  const { mode } = useAdminThemeMode();
  const isDark = mode === 'dark';

  return (
    <div className={`report-filters-card ${isDark ? 'dark-mode' : ''}`}>
      <div className="report-filters-content">
        <div className="filters-grid">
          <div className="filter-item">
            <div className="input-wrapper">
              <input
                type="text"
                placeholder="Search headers..."
                value={filters.header || ''}
                onChange={(e) => onFilterChange('header', e.target.value)}
                className="filter-input"
              />
              <label className="input-label">Search by Header</label>
            </div>
          </div>

          <div className="filter-item">
            <div className="input-wrapper">
              <input
                type="text"
                placeholder="Search body text..."
                value={filters.body || ''}
                onChange={(e) => onFilterChange('body', e.target.value)}
                className="filter-input"
              />
              <label className="input-label">Search by Body</label>
            </div>
          </div>

          <div className="filter-item">
            <div className="input-wrapper">
              <input
                type="number"
                placeholder="Task ID..."
                value={filters.taskID || ''}
                onChange={(e) => onFilterChange('taskID', e.target.value)}
                className="filter-input"
              />
              <label className="input-label">Search by Task ID</label>
            </div>
          </div>

          <div className="filter-item">
            <div className="select-wrapper">
              <select
                value={filters.reporterType || 'all'}
                onChange={(e) => onFilterChange('reporterType', e.target.value)}
                className="filter-select"
              >
                <option value="all">All</option>
                <option value="user">User</option>
                <option value="tasker">Tasker</option>
              </select>
              <label className="select-label">Reporter</label>
            </div>
          </div>

          <div className="filter-item">
            <div className="select-wrapper">
              <select
                value={filters.status || 'all'}
                onChange={(e) => onFilterChange('status', e.target.value)}
                className="filter-select"
              >
                <option value="all">All</option>
                <option value="pending">Pending</option>
                <option value="done">Done</option>
              </select>
              <label className="select-label">Status</label>
            </div>
          </div>
        </div>

        <div className="filters-actions">
          <button
            onClick={onApplyFilters}
            className="search-button"
          >
            Search
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReportFilters;