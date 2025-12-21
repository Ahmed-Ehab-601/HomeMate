/**
 * Example Integration Guide for TaskCalendar Component
 * 
 * This file shows various ways to integrate the TaskCalendar component
 * into different parts of your HomeMate application.
 */

// ============================================
// EXAMPLE 1: Basic Integration in a Page
// ============================================

import TaskCalendar from '../components/TaskCalendar';

export function UserDashboard() {
    // Get user info from your auth context/state
    const currentUserId = 123;

    return (
        <div>
            <h1>My Dashboard</h1>
            <TaskCalendar
                userID={currentUserId}
                userType="user"
            />
        </div>
    );
}

// ============================================
// EXAMPLE 2: Using with Authentication Context
// ============================================

import { useAuth } from '../contexts/AuthContext';
import TaskCalendar from '../components/TaskCalendar';

export function CalendarPage() {
    const { user } = useAuth();

    if (!user) {
        return <div>Please login to view your calendar</div>;
    }

    return (
        <TaskCalendar
            userID={user.id}
            userType={user.role} // Assuming user.role is 'user' or 'tasker'
        />
    );
}

// ============================================
// EXAMPLE 3: Dynamic Switch Between User/Tasker Views
// ============================================

import { useState } from 'react';
import TaskCalendar from '../components/TaskCalendar';

export function AdminCalendarView() {
    const [viewAs, setViewAs] = useState('user');
    const [selectedId, setSelectedId] = useState(1);

    return (
        <div>
            <div className="mb-4 flex gap-4">
                <select
                    value={viewAs}
                    onChange={(e) => setViewAs(e.target.value)}
                    className="px-4 py-2 border rounded"
                >
                    <option value="user">User View</option>
                    <option value="tasker">Tasker View</option>
                </select>

                <input
                    type="number"
                    value={selectedId}
                    onChange={(e) => setSelectedId(e.target.value)}
                    placeholder="Enter ID"
                    className="px-4 py-2 border rounded"
                />
            </div>

            <TaskCalendar
                userID={selectedId}
                userType={viewAs}
            />
        </div>
    );
}

// ============================================
// EXAMPLE 4: Embedding in a Modal/Dialog
// ============================================

import { useState } from 'react';
import TaskCalendar from '../components/TaskCalendar';

export function TaskCalendarModal({ isOpen, onClose, userId, userType }) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-xl shadow-2xl max-w-6xl w-full max-h-[90vh] overflow-auto">
                <div className="p-4 border-b flex justify-between items-center">
                    <h2 className="text-xl font-bold">Task Schedule</h2>
                    <button
                        onClick={onClose}
                        className="px-4 py-2 text-gray-600 hover:text-gray-900"
                    >
                        Close
                    </button>
                </div>

                <TaskCalendar
                    userID={userId}
                    userType={userType}
                />
            </div>
        </div>
    );
}

// ============================================
// EXAMPLE 5: Router Integration (React Router v6)
// ============================================

import { BrowserRouter, Routes, Route, Link } from 'react-router-dom';
import TaskCalendarPage from './pages/TaskCalendarPage';

export function App() {
    return (
        <BrowserRouter>
            <nav>
                <Link to="/">Home</Link>
                <Link to="/calendar">Calendar</Link>
            </nav>

            <Routes>
                <Route path="/" element={<HomePage />} />
                <Route path="/calendar" element={<TaskCalendarPage />} />
            </Routes>
        </BrowserRouter>
    );
}

// ============================================
// EXAMPLE 6: With Custom API URL (Production)
// ============================================

import TaskCalendar from '../components/TaskCalendar';

export function ProductionCalendar() {
    const userId = 456;
    const apiUrl = process.env.VITE_API_BASE_URL || 'https://api.homemate.com/api';

    return (
        <TaskCalendar
            userID={userId}
            userType="user"
            baseURL={apiUrl}
        />
    );
}

// ============================================
// EXAMPLE 7: Side-by-Side User and Tasker Views
// ============================================

import TaskCalendar from '../components/TaskCalendar';

export function ComparisonView({ userId, taskerId }) {
    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div>
                <h2 className="text-2xl font-bold mb-4">User Calendar</h2>
                <TaskCalendar
                    userID={userId}
                    userType="user"
                />
            </div>

            <div>
                <h2 className="text-2xl font-bold mb-4">Tasker Calendar</h2>
                <TaskCalendar
                    userID={taskerId}
                    userType="tasker"
                />
            </div>
        </div>
    );
}

// ============================================
// EXAMPLE 8: With Error Boundary
// ============================================

import { Component } from 'react';
import TaskCalendar from '../components/TaskCalendar';

class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true };
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="p-8 bg-red-50 border border-red-200 rounded-xl">
                    <h2 className="text-xl font-bold text-red-800 mb-2">Something went wrong</h2>
                    <p className="text-red-600">Please refresh the page or contact support.</p>
                </div>
            );
        }

        return this.props.children;
    }
}

export function SafeCalendarPage() {
    return (
        <ErrorBoundary>
            <TaskCalendar userID={123} userType="user" />
        </ErrorBoundary>
    );
}

// ============================================
// EXAMPLE 9: Lazy Loading for Performance
// ============================================

import { lazy, Suspense } from 'react';

const TaskCalendar = lazy(() => import('../components/TaskCalendar'));

export function LazyCalendarPage() {
    return (
        <Suspense
            fallback={
                <div className="flex items-center justify-center min-h-screen">
                    <div className="animate-spin w-12 h-12 border-4 border-primary border-t-transparent rounded-full"></div>
                </div>
            }
        >
            <TaskCalendar userID={123} userType="user" />
        </Suspense>
    );
}

// ============================================
// EXAMPLE 10: With State Management (Redux/Zustand)
// ============================================

import { useSelector } from 'react-redux';
import TaskCalendar from '../components/TaskCalendar';

export function ReduxCalendarPage() {
    // Get user from Redux store
    const user = useSelector(state => state.auth.user);
    const apiConfig = useSelector(state => state.config.apiBaseUrl);

    if (!user) {
        return <div>Loading user information...</div>;
    }

    return (
        <TaskCalendar
            userID={user.id}
            userType={user.accountType}
            baseURL={apiConfig}
        />
    );
}

// ============================================
// TIPS & BEST PRACTICES
// ============================================

/*
1. AUTHENTICATION:
   - Always verify user is authenticated before showing calendar
   - Pass actual user IDs from your auth system, not hardcoded values
   
2. ERROR HANDLING:
   - Wrap component in ErrorBoundary for production
   - Handle loading states gracefully
   
3. PERFORMANCE:
   - Use lazy loading for routes that include calendar
   - Consider memoization if rendering multiple calendars
   
4. RESPONSIVE DESIGN:
   - Calendar is fully responsive by default
   - Test on actual mobile devices, not just browser resize
   
5. API CONFIGURATION:
   - Use environment variables for API URLs
   - Never hardcode production URLs in component props
   
6. ACCESSIBILITY:
   - Component includes ARIA labels and keyboard navigation
   - Ensure your page wrapper doesn't break these features
   
7. TESTING:
   - Test with real API data, not just mock data
   - Verify error states work correctly
   - Check all status filters function properly
*/

// ============================================
// COMMON CUSTOMIZATIONS
// ============================================

/*
CHANGE DEFAULT VIEW:
Modify the initial state in TaskCalendar.jsx:
  const [viewMode, setViewMode] = useState('list'); // 'calendar' or 'list'

CHANGE DEFAULT FILTER:
Modify the initial state in TaskCalendar.jsx:
  const [filteredStatus, setFilteredStatus] = useState('Pending'); // Any status

ADD CUSTOM FIELDS TO TASK CARDS:
Edit the renderTaskCard function to include additional fields from your API

MODIFY COLORS:
Update tailwind.config.js or the statusConfig object in the component

CHANGE DATE FORMAT:
Update the toLocaleDateString options in the component
*/

export default {
    UserDashboard,
    CalendarPage,
    AdminCalendarView,
    TaskCalendarModal,
    ProductionCalendar,
    ComparisonView,
    SafeCalendarPage,
    LazyCalendarPage,
    ReduxCalendarPage,
};
