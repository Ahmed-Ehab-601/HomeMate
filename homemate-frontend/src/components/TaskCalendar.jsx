import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import {
    ChevronLeft,
    ChevronRight,
    Loader,
} from 'lucide-react';
import { rescheduleTask } from '../api/taskManagementApi';
import { useAuth } from '../contexts/AuthContext';

/**
 * TaskCalendar Component
 * A clean calendar view matching the project's original design
 */
const TaskCalendar = ({ onBackToList }) => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [tasks, setTasks] = useState([]);
    const [filteredStatus, setFilteredStatus] = useState('All');
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [draggedTask, setDraggedTask] = useState(null);
    const [snackbar, setSnackbar] = useState({ show: false, message: '', type: 'error' });
    const { user, getUserRole } = useAuth();

    // Status Colors - matching project style
    const statusConfig = {
        InReview: {
            bg: '#FFFBEB',
            text: '#92400E',
            display: 'Sample Text',
        },
        Accepted: {
            bg: '#DCFCE7',
            text: '#166534',
            display: 'Sample Text',
        },
        InProgress: {
            bg: '#E0F2FE',
            text: '#075985',
            display: 'Sample Text',
        },
        Suspended: {
            bg: '#F3F4F6',
            text: '#374151',
            display: 'Sample Text',
        },
        Done: {
            bg: '#F0FDF4',
            text: '#166534',
            display: 'Sample Text',
        },
        Rejected: {
            bg: '#FEF2F2',
            text: '#991B1B',
            display: 'Sample Text',
        },
    };

    // Day header colors matching project style
    const dayColors = {
        Monday: '#c6ff4d',
        Tuesday: '#b8f03d',
        Wednesday: '#aae02d',
        Thursday: '#9cd01d',
        Friday: '#8ec00d',
        Saturday: '#4b5563',
        Sunday: '#a7df2d',
    };

    // Helper to get days in month
    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const days = new Date(year, month + 1, 0).getDate();
        const firstDay = new Date(year, month, 1).getDay();
        // Adjust firstDay so Monday is 0 (convert from Sunday=0 to Monday=0)
        const adjustedFirstDay = firstDay === 0 ? 6 : firstDay - 1;
        return { days, firstDay: adjustedFirstDay };
    };

    // ==================== API FETCHING ====================
    const fetchTasks = async () => {
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('homemate_token');
            if (!token) throw new Error("No authentication token found");

            // Calculate start and end date for current view (entire month)
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth(); // 0-indexed (0 = January, 11 = December)
            const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
            const lastDay = new Date(year, month + 1, 0).getDate();  // Fixed: Gets correct last day of month
            const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;
            
            console.log('🔗 URL DEBUG:', { year, month, startDate, endDate, filteredStatus });

            // Determine user role to pick correct endpoint
            const userRole = user?.role || 'ROLE_USER';
            let endpoint = userRole === 'ROLE_TASKER' ? '/api/tasker/getTasks' : '/api/user/getTasks';

            console.log('📅 [TaskCalendar] Fetching with role:', userRole, 'endpoint:', endpoint);

            const url = `http://localhost:8080${endpoint}?startDate=${startDate}&endDate=${endDate}&status=${filteredStatus}`;
            
            console.log('🔗 Full URL:', url);

            const response = await fetch(url, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.status === 204) {
                setTasks([]);
                return;
            }

            if (response.status === 401) {
                throw new Error("Unauthorized. Please login again.");
            }

            if (response.status === 403) {
                throw new Error("Access forbidden.");
            }

            if (!response.ok) {
                const errorText = await response.text().catch(() => response.statusText);
                throw new Error(`Failed to fetch tasks: ${errorText}`);
            }

            const data = await response.json();
            setTasks(Array.isArray(data) ? data : []);
            console.log('📅 [TaskCalendar] Loaded:', (Array.isArray(data) ? data.length : 0), 'tasks');
            console.log('📅 [TaskCalendar] Task details:', data);
            data.forEach(task => {
                console.log(`  - ${task.serviceName} on ${task.startDate} (${task.status})`);
            });

        } catch (err) {
            console.error("❌ Fetch error:", err);
            setError(err.message || "Failed to load tasks");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTasks();
    }, [currentDate, filteredStatus]);

    // ==================== DRAG AND DROP HANDLERS ====================
    const handleDragStart = (e, task) => {
        setDraggedTask(task);
        e.dataTransfer.effectAllowed = 'move';
        e.currentTarget.style.opacity = '0.5';
    };

    const handleDragEnd = (e) => {
        e.currentTarget.style.opacity = '1';
    };

    const handleDragOver = (e) => {
        e.preventDefault();
        e.dataTransfer.dropEffect = 'move';
    };

    const handleDrop = async (e, day) => {
        e.preventDefault();
        if (!draggedTask) return;

        // Calculate new date string YYYY-MM-DD
        const newDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);

        // Preserve time if present or default to start of day
        const oldDate = new Date(draggedTask.startDate);
        newDate.setHours(oldDate.getHours(), oldDate.getMinutes(), oldDate.getSeconds());

        // VALIDATION: Check if new date is in the future
        const now = new Date();
        if (newDate <= now) {
            const isPast = newDate.toDateString() === now.toDateString() ? 
                "Cannot reschedule to a past time. Please select a future time." :
                "Cannot reschedule to a past date. Please select a future date.";
            
            console.warn("⚠️ Invalid reschedule attempt:", isPast);
            setSnackbar({ show: true, message: isPast, type: 'error' });
            setDraggedTask(null);
            
            // Auto-dismiss after 5 seconds
            setTimeout(() => {
                setSnackbar({ show: false, message: '', type: 'error' });
            }, 5000);
            return;
        }

        const newDateStr = newDate.toISOString();

        console.log(`🔄 [Reschedule] Moving task ${draggedTask.taskID} to ${newDateStr}`);

        // 1. Optimistic update
        const previousTasks = [...tasks];
        const updatedTasks = tasks.map(t => {
            if (t.taskID === draggedTask.taskID) {
                return { ...t, startDate: newDateStr };
            }
            return t;
        });
        setTasks(updatedTasks);
        setDraggedTask(null);

        // 2. Call API
        try {
            await rescheduleTask(draggedTask.taskID, newDateStr);
            console.log("✅ Reschedule successful");
        } catch (err) {
            // Extract error message properly
            let errorMessage = 'Failed to reschedule task';
            if (err?.response?.data) {
                if (typeof err.response.data === 'string') {
                    errorMessage = err.response.data;
                } else if (err.response.data.message) {
                    errorMessage = err.response.data.message;
                } else if (err.response.data.error) {
                    errorMessage = err.response.data.error;
                }
            } else if (err?.message) {
                errorMessage = err.message;
            }
            
            // Add context for validation errors
            if (err?.status === 400 || err?.response?.status === 400) {
                errorMessage = "The selected date/time is not valid. Please choose a future date and time.";
            }
            
            console.error("❌ Reschedule failed:", errorMessage);
            
            // Revert changes on error
            setTasks(previousTasks);
            
            // Show snackbar instead of alert
            setSnackbar({ show: true, message: errorMessage, type: 'error' });
            
            // Auto-dismiss after 5 seconds
            setTimeout(() => {
                setSnackbar({ show: false, message: '', type: 'error' });
            }, 5000);
        }
    };

    // ==================== NAVIGATION HANDLERS ====================
    const handlePreviousMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    };

    const handleNextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    };

    // ==================== RENDER HELPERS ====================

    // Group tasks by day and calculate spanning
    const tasksByDay = useMemo(() => {
        const map = new Map();
        tasks.forEach(task => {
            if (!task.startDate) {
                console.log('⚠️ Task missing startDate:', task);
                return;
            }
            const date = new Date(task.startDate);
            const day = date.getDate();
            const taskMonth = date.getMonth();
            const taskYear = date.getFullYear();
            const currentMonth = currentDate.getMonth();
            const currentYear = currentDate.getFullYear();
            
            // Debug log for date mismatches
            if (taskMonth !== currentMonth || taskYear !== currentYear) {
                console.log(`📅 Task date mismatch - Task: ${date.toISOString()} (${taskMonth}/${taskYear}), Current: ${currentDate.toISOString()} (${currentMonth}/${currentYear})`);
            }
            
            // Filter to ensure task belongs to current month view
            if (taskMonth === currentMonth && taskYear === currentYear) {
                if (!map.has(day)) map.set(day, []);
                map.get(day).push(task);
                console.log(`✅ Added task to day ${day}: ${task.serviceName}`);
            }
        });
        console.log('📊 Tasks by day map:', map);
        return map;
    }, [tasks, currentDate]);

    // ==================== SNACKBAR COMPONENT ====================
    const renderSnackbar = () => {
        if (!snackbar.show) return null;

        return (
            <div style={{
                position: 'fixed',
                bottom: '20px',
                right: '20px',
                backgroundColor: snackbar.type === 'error' ? '#FEE2E2' : '#ECFDF5',
                color: snackbar.type === 'error' ? '#991B1B' : '#065F46',
                padding: '16px 20px',
                borderRadius: '8px',
                boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                zIndex: 9999,
                animation: 'slideIn 0.3s ease-out',
                border: `1px solid ${snackbar.type === 'error' ? '#FECACA' : '#A7F3D0'}`,
                fontSize: '14px',
                fontWeight: '500',
                maxWidth: '400px',
            }}>
                <span style={{ fontSize: '18px' }}>{snackbar.type === 'error' ? '❌' : '✅'}</span>
                <span style={{ flex: 1 }}>{snackbar.message}</span>
                <button
                    onClick={() => setSnackbar({ show: false, message: '', type: 'error' })}
                    style={{
                        background: 'none',
                        border: 'none',
                        color: 'inherit',
                        cursor: 'pointer',
                        fontSize: '18px',
                        padding: '0',
                    }}
                >
                    ×
                </button>
            </div>
        );
    };

    // Add CSS animation
    React.useEffect(() => {
        const style = document.createElement('style');
        style.textContent = `
            @keyframes slideIn {
                from {
                    transform: translateX(400px);
                    opacity: 0;
                }
                to {
                    transform: translateX(0);
                    opacity: 1;
                }
            }
        `;
        if (!document.head.querySelector('style[data-snackbar]')) {
            style.setAttribute('data-snackbar', 'true');
            document.head.appendChild(style);
        }
    }, []);

    // ==================== RENDER CALENDAR GRID ====================
    const renderCalendar = () => {
        const { days: totalDays, firstDay } = getDaysInMonth(currentDate);
        const days = [];
        const blanks = [];

        // Header Row (Mon - Sun) with colors
        const weekDays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];
        
        const headerRow = (
            <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(7, 1fr)',
                marginBottom: '0',
                overflow: 'hidden',
                borderTopLeftRadius: '12px',
                borderTopRightRadius: '12px',
            }}>
                {weekDays.map((day) => (
                    <div 
                        key={day} 
                        style={{
                            backgroundColor: dayColors[day],
                            color: day === 'Saturday' ? '#ffffff' : '#111827',
                            padding: '16px 8px',
                            textAlign: 'center',
                            fontWeight: '600',
                            fontSize: '14px',
                            borderRight: day !== 'Sunday' ? '1px solid rgba(0,0,0,0.1)' : 'none',
                        }}
                    >
                        {day}
                    </div>
                ))}
            </div>
        );

        // Empty slots for previous month
        for (let i = 0; i < firstDay; i++) {
            blanks.push(
                <div 
                    key={`blank-${i}`} 
                    style={{
                        minHeight: '100px',
                        backgroundColor: '#f5f5f5',
                        border: '1px solid #ddd',
                        borderTop: 'none',
                    }}
                ></div>
            );
        }

        // Day Cells
        for (let day = 1; day <= totalDays; day++) {
            const dayTasks = tasksByDay.get(day) || [];
            
            // Check if this day is in the past
            const cellDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            cellDate.setHours(0, 0, 0, 0);
            const isPastDate = cellDate < today;
            
            days.push(
                <div
                    key={day}
                    onClick={() => dayTasks.length > 0 && navigate(`/tasks/${dayTasks[0].taskID}`)}
                    onDragOver={handleDragOver}
                    onDrop={(e) => handleDrop(e, day)}
                    style={{
                        minHeight: '100px',
                        padding: '8px',
                        backgroundColor: isPastDate ? '#f9fafb' : '#ffffff',
                        border: '1px solid #ddd',
                        borderTop: 'none',
                        borderLeft: (day + firstDay - 1) % 7 === 0 ? '1px solid #ddd' : 'none',
                        cursor: dayTasks.length > 0 ? 'pointer' : (isPastDate ? 'not-allowed' : 'default'),
                        position: 'relative',
                        display: 'flex',
                        flexDirection: 'column',
                        opacity: isPastDate ? 0.5 : 1,
                    }}
                >
                    {/* Day Number */}
                    <div style={{
                        fontSize: '12px',
                        fontWeight: '500',
                        color: isPastDate ? '#9ca3af' : '#666',
                        marginBottom: '4px',
                    }}>
                        {String(day).padStart(2, '0')}
                    </div>

                    {/* Tasks */}
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
                        {dayTasks.slice(0, 2).map((task, idx) => {
                            const config = statusConfig[task.status] || statusConfig.InReview;
                            
                            // Format time from task startDate
                            const taskTime = task.startDate ? new Date(task.startDate).toLocaleTimeString('en-US', {
                                hour: '2-digit',
                                minute: '2-digit',
                                hour12: false
                            }) : 'N/A';
                            
                            return (
                                <div
                                    key={task.taskID}
                                    draggable="true"
                                    onDragStart={(e) => handleDragStart(e, task)}
                                    onDragEnd={handleDragEnd}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        navigate(`/tasks/${task.taskID}`);
                                    }}
                                    title={`Time: ${taskTime}\nStatus: ${task.status}\nService: ${task.serviceName}\nCustomer: ${task.userName || 'N/A'}\nTasker: ${task.taskerName || 'N/A'}`}
                                    style={{
                                        backgroundColor: config.bg,
                                        color: config.text,
                                        padding: '4px 8px',
                                        borderRadius: '4px',
                                        fontSize: '10px',
                                        fontWeight: '500',
                                        overflow: 'hidden',
                                        textOverflow: 'ellipsis',
                                        whiteSpace: 'nowrap',
                                        cursor: 'move',
                                        transition: 'opacity 0.2s ease',
                                    }}
                                >
                                    <div style={{ display: 'flex', gap: '4px', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {task.serviceName || config.display}
                                        </span>
                                        <span style={{ fontSize: '9px', opacity: 0.8, whiteSpace: 'nowrap' }}>
                                            {taskTime}
                                        </span>
                                    </div>
                                </div>
                            );
                        })}
                        {dayTasks.length > 2 && (
                            <div style={{
                                fontSize: '10px',
                                color: '#999',
                                fontWeight: '500',
                            }}>
                                +{dayTasks.length - 2} more
                            </div>
                        )}
                    </div>
                </div>
            );
        }

        return (
            <div>
                {headerRow}
                <div style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 1fr)',
                    border: '1px solid #ddd',
                    borderTop: 'none',
                    borderBottomLeftRadius: '12px',
                    borderBottomRightRadius: '12px',
                    overflow: 'hidden',
                }}>
                    {blanks}
                    {days}
                </div>
            </div>
        );
    };

    // ==================== LOADING STATE ====================
    if (loading) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#ffffff',
                padding: '24px',
            }}>
                <div style={{ textAlign: 'center' }}>
                    <Loader style={{ width: '48px', height: '48px', color: '#a7df2d', animation: 'spin 1s linear infinite' }} />
                    <p style={{ marginTop: '24px', fontSize: '18px', fontWeight: '600', color: '#4b5563' }}>Loading your tasks...</p>
                </div>
            </div>
        );
    }

    // ==================== ERROR STATE ====================
    if (error) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                backgroundColor: '#ffffff',
                padding: '24px',
            }}>
                <div style={{
                    maxWidth: '500px',
                    width: '100%',
                    backgroundColor: '#ffffff',
                    borderRadius: '24px',
                    border: '1px solid #e5e7eb',
                    padding: '32px',
                    textAlign: 'center',
                    boxShadow: '0 25px 50px rgba(15, 23, 42, 0.08)',
                }}>
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>⚠️</div>
                    <div style={{ fontSize: '24px', fontWeight: '700', color: '#991B1B', marginBottom: '12px' }}>Error Loading Tasks</div>
                    <p style={{ color: '#4b5563', marginBottom: '24px' }}>{error}</p>
                    <button
                        onClick={fetchTasks}
                        style={{
                            padding: '12px 32px',
                            backgroundColor: '#c6ff4d',
                            color: '#111827',
                            borderRadius: '12px',
                            border: 'none',
                            fontSize: '14px',
                            fontWeight: '600',
                            cursor: 'pointer',
                            transition: 'all 0.2s ease',
                        }}
                        onMouseOver={(e) => e.target.style.backgroundColor = '#a7df2d'}
                        onMouseOut={(e) => e.target.style.backgroundColor = '#c6ff4d'}
                    >
                        🔄 Try Again
                    </button>
                </div>
            </div>
        );
    }

    // ==================== MAIN RENDER ====================
    return (
        <div style={{
            minHeight: '100vh',
            backgroundColor: '#ffffff',
            padding: '40px 20px',
        }}>
            <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
                {/* Header */}
                <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '12px',
                    padding: '24px',
                    marginBottom: '20px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                }}>
                    <div style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        marginBottom: '16px',
                    }}>
                        <h1 style={{
                            fontSize: '28px',
                            fontWeight: '700',
                            color: '#111827',
                            margin: 0,
                        }}>
                            [{currentDate.toLocaleString('default', { month: 'long' })}] - [{currentDate.getFullYear()}]
                        </h1>

                        {/* Navigation */}
                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                            {onBackToList && (
                                <button
                                    onClick={onBackToList}
                                    style={{
                                        padding: '8px 16px',
                                        backgroundColor: '#c6ff4d',
                                        color: '#111827',
                                        border: '1px solid #a7df2d',
                                        borderRadius: '8px',
                                        cursor: 'pointer',
                                        fontWeight: '600',
                                        fontSize: '14px',
                                        transition: 'all 0.2s ease',
                                        marginRight: '16px',
                                    }}
                                    onMouseOver={(e) => e.target.style.backgroundColor = '#a7df2d'}
                                    onMouseOut={(e) => e.target.style.backgroundColor = '#c6ff4d'}
                                >
                                    ← Back to List
                                </button>
                            )}
                            <button
                                onClick={handlePreviousMonth}
                                style={{
                                    padding: '8px 12px',
                                    backgroundColor: '#f3f4f6',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '8px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => e.target.style.backgroundColor = '#e5e7eb'}
                                onMouseOut={(e) => e.target.style.backgroundColor = '#f3f4f6'}
                            >
                                <ChevronLeft style={{ width: '20px', height: '20px' }} />
                            </button>
                            <button
                                onClick={handleNextMonth}
                                style={{
                                    padding: '8px 12px',
                                    backgroundColor: '#f3f4f6',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '8px',
                                    cursor: 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    transition: 'all 0.2s ease',
                                }}
                                onMouseOver={(e) => e.target.style.backgroundColor = '#e5e7eb'}
                                onMouseOut={(e) => e.target.style.backgroundColor = '#f3f4f6'}
                            >
                                <ChevronRight style={{ width: '20px', height: '20px' }} />
                            </button>
                        </div>
                    </div>

                    {/* Filter */}
                    <div>
                        <select
                            value={filteredStatus}
                            onChange={(e) => setFilteredStatus(e.target.value)}
                            style={{
                                padding: '8px 16px',
                                border: '1px solid #e5e7eb',
                                borderRadius: '8px',
                                backgroundColor: '#ffffff',
                                color: '#111827',
                                fontSize: '14px',
                                fontWeight: '500',
                                cursor: 'pointer',
                                outline: 'none',
                            }}
                        >
                            <option value="All">All Status</option>
                            <option value="InReview">In Review</option>
                            <option value="Accepted">Accepted</option>
                            <option value="InProgress">In Progress</option>
                            <option value="Suspended">Suspended</option>
                            <option value="Done">Done</option>
                            <option value="Rejected">Rejected</option>
                        </select>
                    </div>
                </div>

                {/* Calendar */}
                <div style={{
                    backgroundColor: '#ffffff',
                    borderRadius: '12px',
                    overflow: 'hidden',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                }}>
                    {renderCalendar()}
                </div>
            </div>
            {renderSnackbar()}
        </div>
    );
};

export default TaskCalendar;
