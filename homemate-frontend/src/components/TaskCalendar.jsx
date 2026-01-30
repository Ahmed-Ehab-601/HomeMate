import React, { useState, useEffect, useMemo } from 'react';
import TaskCard from './TaskCard';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Loader } from 'lucide-react';
import { rescheduleTask, getTaskerBusyTime, getTaskDetails } from '../api/taskManagementApi';
import { fetchUserTasksByDate, fetchTaskerTasksByDate } from '../api/tasksApi';
import { useAuth } from '../contexts/AuthContext';

// Work hours constants (same as TaskDetailsPage)
const WORK_DAY_START_MINUTES = 8 * 60; // 08:00 = 480 minutes
const WORK_DAY_END_MINUTES = 24 * 60 + 30; // 20:30 = 1230 minutes

// Status configuration - Match TaskCard colors
const statusConfig = {
    InReview: { bg: '#FEF3C7', text: '#92400E', display: 'In Review' },
    Accepted: { bg: '#DBEAFE', text: '#1E40AF', display: 'Accepted' },
    InProgress: { bg: '#FED7AA', text: '#C2410C', display: 'In Progress' },
    Suspended: { bg: '#F3F4F6', text: '#6B7280', display: 'Suspended' },
    Done: { bg: '#DCFCE7', text: '#166534', display: 'Done' },
    Rejected: { bg: '#FEE2E2', text: '#991B1B', display: 'Rejected' },
};

// Day colors for calendar header (system colors)
const dayColors = {
    Monday: '#c6ff4d',      // Primary green
    Tuesday: '#e5e7eb',    // Neutral gray
    Wednesday: '#f3f4f6',  // Lighter gray
    Thursday: '#e5e7eb',   // Neutral gray
    Friday: '#c6ff4d',     // Primary green
    Saturday: '#a7df2d',   // Accent green (for Saturday)
    Sunday: '#f87171',      // Accent red (for Sunday)
};

function TaskCalendar() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const userRole = user?.role || 'user';

    // ==================== STATE MANAGEMENT ====================
    const [currentDate, setCurrentDate] = useState(new Date());
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [filteredStatus, setFilteredStatus] = useState('All');
    const [draggedTask, setDraggedTask] = useState(null);
    const [snackbar, setSnackbar] = useState({ show: false, message: '', type: 'success' });
    const [showTimeModal, setShowTimeModal] = useState(false);
    const [selectedDate, setSelectedDate] = useState(null);
    const [selectedTime, setSelectedTime] = useState('');
    const [busyTimes, setBusyTimes] = useState([]);
    const [loadingBusyTimes, setLoadingBusyTimes] = useState(false);
    const [taskEstimation, setTaskEstimation] = useState(0);

    // ==================== HELPER FUNCTIONS ====================
    const isStatusDraggable = (status) => {
        return ['InReview', 'Accepted'].includes(status);
    };

    const formatEstimation = (minutes) => {
        if (!minutes || minutes === 0) return '';
        const hours = Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours > 0 && mins > 0) return `${hours}h ${mins}m`;
        if (hours > 0) return `${hours}h`;
        return `${mins}m`;
    };

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const days = new Date(year, month + 1, 0).getDate();
        const firstDayOfMonth = new Date(year, month, 1).getDay();
        const firstDay = firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1;
        return { days, firstDay };
    };

    // Generate time slots (08:00 - 20:30, 30-minute intervals)
    const timeSlots = Array.from({ length: 25 }, (_, index) => {
        const minutes = index * 30;
        const hours = 8 + Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours > 20 || (hours === 20 && mins > 30)) {
            return null;
        }
        return `${String(hours).padStart(2, "0")}:${String(mins).padStart(2, "0")}`;
    }).filter(Boolean);

    // ==================== BUSY TIME VALIDATION (Same as TaskDetailsPage) ====================
    const hasEnoughTimeToComplete = (timeSlot, estimationMinutes) => {
        const [hours, minutes] = timeSlot.split(":").map(Number);
        const slotStartMinutes = hours * 60 + minutes;
        const slotEndMinutes = slotStartMinutes + estimationMinutes;
        
        if (slotEndMinutes > WORK_DAY_END_MINUTES) {
            return false;
        }
        
        return true;
    };

    const isTimeSlotBusy = (timeSlot) => {
        if (!selectedDate || busyTimes.length === 0) {
            return false;
        }

        const [slotHours, slotMinutes] = timeSlot.split(":").map(Number);
        const year = selectedDate.getFullYear();
        const month = selectedDate.getMonth() + 1;
        const day = selectedDate.getDate();
        
        const currentTaskEstMinutes = taskEstimation ?? 0;
        const slotStartMinutes = slotHours * 60 + slotMinutes;
        const slotEndMinutes = slotStartMinutes + currentTaskEstMinutes;

        for (const busyTime of busyTimes) {
            // Skip the current task being rescheduled
            if (busyTime.taskID === draggedTask?.taskID) {
                continue;
            }

            try {
                const busyStart = new Date(busyTime.startDate);
                const busyYear = busyStart.getFullYear();
                const busyMonth = busyStart.getMonth();
                const busyDay = busyStart.getDate();
                const busyHours = busyStart.getHours();
                const busyMins = busyStart.getMinutes();
                
                // Only check if dates match
                if (busyYear !== year || busyMonth !== month - 1 || busyDay !== day) {
                    continue;
                }

                const busyStartMinutes = busyHours * 60 + busyMins;
                const busyEndMinutes = busyStartMinutes + busyTime.estimation;

                if (currentTaskEstMinutes === 0 && slotStartMinutes === busyEndMinutes) {
                                     return false;
                     }
                
                // Check for ANY overlap
                const hasOverlap = (
                    (slotStartMinutes >= busyStartMinutes && slotStartMinutes < busyEndMinutes) ||
                    (slotEndMinutes > busyStartMinutes && slotEndMinutes <= busyEndMinutes) ||
                    (slotStartMinutes <= busyStartMinutes && slotEndMinutes >= busyEndMinutes) ||
                    (busyStartMinutes <= slotStartMinutes && busyEndMinutes >= slotEndMinutes) ||
                    (slotStartMinutes < busyStartMinutes && slotEndMinutes > busyStartMinutes)
                );

                if (hasOverlap) {
                    return true;
                }
            } catch (e) {
                console.warn("Failed to parse busy time:", busyTime, e);
                continue;
            }
        }

        return false;
    };

    // Filter available time slots
    const availableTimeSlots = useMemo(() => {
        if (!selectedDate) return timeSlots;

        return timeSlots.filter((slot) => {
            // Check if date is today and time has passed
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const checkDate = new Date(selectedDate);
            checkDate.setHours(0, 0, 0, 0);
            const isToday = checkDate.getTime() === today.getTime();

            if (isToday) {
                const [hours, minutes] = slot.split(":").map(Number);
                const slotTime = new Date();
                slotTime.setHours(hours, minutes, 0, 0);
                const now = new Date();
                if (slotTime <= now) {
                    return false;
                }
            }

            // Check if there's enough time in the day
            if (!hasEnoughTimeToComplete(slot, taskEstimation)) {
                return false;
            }

            // Check if slot conflicts with busy times
            return !isTimeSlotBusy(slot);
        });
    }, [selectedDate, busyTimes, taskEstimation, draggedTask]);

    // ==================== FETCH TASKS ====================
    const fetchTasks = async () => {
        try {
            setLoading(true);
            setError(null);
            
            // Get first and last day of current month
            const year = currentDate.getFullYear();
            const month = currentDate.getMonth();
            const firstDay = new Date(year, month, 1);
            const lastDay = new Date(year, month + 1, 0);
            
            // Format as YYYY-MM-DD for LocalDate
            const startDate = `${firstDay.getFullYear()}-${String(firstDay.getMonth() + 1).padStart(2, '0')}-${String(firstDay.getDate()).padStart(2, '0')}`;
            const endDate = `${lastDay.getFullYear()}-${String(lastDay.getMonth() + 1).padStart(2, '0')}-${String(lastDay.getDate()).padStart(2, '0')}`;
            
            console.log('🔵 [TaskCalendar] Fetching tasks:', { startDate, endDate, filteredStatus, userRole });
            
            let data;
            if (userRole === 'ROLE_TASKER') {
                data = await fetchTaskerTasksByDate(user?.id, startDate, endDate, filteredStatus);
            } else {
                data = await fetchUserTasksByDate(user?.id, startDate, endDate, filteredStatus);
            }
            
            setTasks(Array.isArray(data) ? data : []);
            console.log('✅ [TaskCalendar] Tasks loaded:', data);
        } catch (err) {
            console.error('❌ [TaskCalendar] Error:', err);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    // ==================== ORGANIZE TASKS BY DAY ====================
    const tasksByDay = useMemo(() => {
        const map = new Map();
        let filteredTasks = tasks;

        if (filteredStatus !== 'All') {
            filteredTasks = tasks.filter((task) => task.status === filteredStatus);
        }

        filteredTasks = filteredTasks.filter((task) => {
            if (!task.startDate) return false;
            const taskDate = new Date(task.startDate);
            return (
                taskDate.getMonth() === currentDate.getMonth() &&
                taskDate.getFullYear() === currentDate.getFullYear()
            );
        });

        filteredTasks.forEach((task) => {
            const day = new Date(task.startDate).getDate();
            if (!map.has(day)) {
                map.set(day, []);
            }
            map.get(day).push(task);
        });

        return map;
    }, [tasks, currentDate, filteredStatus]);

    // ==================== DRAG AND DROP HANDLERS ====================
    const handleDragStart = (e, task) => {
        if (!isStatusDraggable(task?.status)) {
            setSnackbar({
                show: true,
                message: 'Only tasks in "In Review" or "Accepted" status can be rescheduled.',
                type: 'error',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
            e.preventDefault();
            return;
        }

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

        console.log('🔵 [Drag Drop] Dragged task:', draggedTask);

        const dropDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        dropDate.setHours(0, 0, 0, 0);

        if (dropDate < today) {
            setSnackbar({
                show: true,
                message: 'Cannot schedule tasks in the past.',
                type: 'error',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
            setDraggedTask(null);
            return;
        }

        setLoadingBusyTimes(true);
        
        try {
            // Fetch full task details to get taskerID and estimation
            const taskDetails = await getTaskDetails(draggedTask.taskID);
            console.log('🔵 [Drag Drop] Task details:', taskDetails);
            
            const estimation = taskDetails.estimation || 0;
            const taskerID = taskDetails.taskerID;
            
            if (!taskerID) {
                setSnackbar({
                    show: true,
                    message: 'Cannot reschedule: Tasker information missing.',
                    type: 'error',
                });
                setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
                setDraggedTask(null);
                setLoadingBusyTimes(false);
                return;
            }
            
            setTaskEstimation(estimation);
            
            // Fetch busy times for the selected date
            const dateStr = `${dropDate.getFullYear()}-${String(dropDate.getMonth() + 1).padStart(2, '0')}-${String(dropDate.getDate()).padStart(2, '0')}`;
            const busyTimeData = await getTaskerBusyTime(taskerID, dateStr, userRole);
            setBusyTimes(busyTimeData || []);
            
            setSelectedDate(dropDate);
            setSelectedTime('');
            setShowTimeModal(true);
        } catch (err) {
            console.error('❌ [Drag Drop] Error:', err);
            setSnackbar({
                show: true,
                message: `Failed to load scheduling data: ${err.message}`,
                type: 'error',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
            setDraggedTask(null);
        } finally {
            setLoadingBusyTimes(false);
        }
    };

    const handleTimeConfirm = async () => {
        if (!draggedTask || !selectedDate || !selectedTime) return;

        const [hours, minutes] = selectedTime.split(':').map(Number);
        const [year, month, day] = [selectedDate.getFullYear(), selectedDate.getMonth() + 1, selectedDate.getDate()];
        
        const currentTaskEstMinutes = taskEstimation ?? 0;
        const newStartMinutes = hours * 60 + minutes;
        const newEndMinutes = newStartMinutes + currentTaskEstMinutes;

        // Validate working hours
        if (newEndMinutes > WORK_DAY_END_MINUTES) {
            const endHour = Math.floor(newEndMinutes / 60);
            const endMin = newEndMinutes % 60;
            setSnackbar({
                show: true,
                message: `⏰ Cannot reschedule: This task would end at ${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}, which is beyond working hours (08:00 - 20:30).`,
                type: 'error',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
            return;
        }

        // Check for overlaps with busy times
        for (const busyTime of busyTimes || []) {
            if (busyTime.taskID === draggedTask?.taskID) {
                continue;
            }

            try {
                const busyStart = new Date(busyTime.startDate);
                const busyYear = busyStart.getFullYear();
                const busyMonth = busyStart.getMonth();
                const busyDay = busyStart.getDate();
                const busyHours = busyStart.getHours();
                const busyMins = busyStart.getMinutes();

                if (busyYear !== year || busyMonth !== month - 1 || busyDay !== day) {
                    continue;
                }

                const busyStartMinutes = busyHours * 60 + busyMins;
                const busyEndMinutes = busyStartMinutes + busyTime.estimation;

          if (currentTaskEstMinutes === 0 && slotStartMinutes === busyEndMinutes) {
                   continue;
               }

                const hasOverlap = (
                    (newStartMinutes >= busyStartMinutes && newStartMinutes < busyEndMinutes) ||
                    (newEndMinutes > busyStartMinutes && newEndMinutes <= busyEndMinutes) ||
                    (newStartMinutes <= busyStartMinutes && newEndMinutes >= busyEndMinutes) ||
                    (busyStartMinutes <= newStartMinutes && busyEndMinutes >= newEndMinutes) ||
                    (newStartMinutes < busyStartMinutes && newEndMinutes > busyStartMinutes)
                );

                if (hasOverlap) {
                    const busyTimeStr = `${String(busyHours).padStart(2, '0')}:${String(busyMins).padStart(2, '0')}`;
                    const busyDurationHours = (busyTime.estimation / 60).toFixed(1);
                    const taskEndTime = `${String(Math.floor(newEndMinutes/60)).padStart(2, '0')}:${String(newEndMinutes%60).padStart(2, '0')}`;
                    
                    setSnackbar({
                        show: true,
                        message: `⏰ Cannot reschedule: Your task (${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} - ${taskEndTime}) would overlap with another scheduled period at ${busyTimeStr} (${busyDurationHours}h).`,
                        type: 'error',
                    });
                    setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
                    return;
                }
            } catch (e) {
                console.warn("Failed to parse busy time:", busyTime, e);
                continue;
            }
        }

        // Proceed with reschedule
        const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
        const newDateTime = `${dateStr}T${selectedTime}:00`;

        try {
            await rescheduleTask(draggedTask.taskID, newDateTime);
            setSnackbar({
                show: true,
                message: '✅ Task rescheduled successfully!',
                type: 'success',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'success' }), 3000);
            fetchTasks();
        } catch (err) {
            setSnackbar({
                show: true,
                message: `Failed to reschedule: ${err.message}`,
                type: 'error',
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 3000);
        } finally {
            setShowTimeModal(false);
            setDraggedTask(null);
            setSelectedDate(null);
            setSelectedTime('');
            setBusyTimes([]);
            setTaskEstimation(0);
        }
    };

    // ==================== NAVIGATION ====================
    const today = new Date();
    const minMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const maxMonth = new Date(today.getFullYear(), today.getMonth() + 3, 1);

    const isPrevDisabled =
        currentDate.getFullYear() < minMonth.getFullYear() ||
        (currentDate.getFullYear() === minMonth.getFullYear() &&
            currentDate.getMonth() <= minMonth.getMonth());

    const isNextDisabled =
        currentDate.getFullYear() > maxMonth.getFullYear() ||
        (currentDate.getFullYear() === maxMonth.getFullYear() &&
            currentDate.getMonth() >= maxMonth.getMonth());

    const handlePreviousMonth = () => {
        if (!isPrevDisabled) {
            setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
        }
    };

    const handleNextMonth = () => {
        if (!isNextDisabled) {
            setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
        }
    };

    // ==================== RENDER CALENDAR ====================
    const renderCalendar = () => {
        const { days: totalDays, firstDay } = getDaysInMonth(currentDate);
        const days = [];
        const blanks = [];

        const weekDays = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

        const headerRow = (
            <div
                style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(7, 1fr)',
                    marginBottom: '0',
                    overflow: 'hidden',
                    borderTopLeftRadius: '12px',
                    borderTopRightRadius: '12px',
                }}
            >
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

        for (let day = 1; day <= totalDays; day++) {
            const dayTasks = tasksByDay.get(day) || [];

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
                        cursor: dayTasks.length > 0 ? 'pointer' : isPastDate ? 'not-allowed' : 'default',
                        position: 'relative',
                        display: 'flex',
                        flexDirection: 'column',
                        opacity: isPastDate ? 0.5 : 1,
                    }}
                >
                    <div
                        style={{
                            fontSize: '12px',
                            fontWeight: '500',
                            color: isPastDate ? '#9ca3af' : '#666',
                            marginBottom: '4px',
                        }}
                    >
                        {String(day).padStart(2, '0')}
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', flex: 1 }}>
                        {dayTasks.slice(0, 2).map((task) => {
                            const config = statusConfig[task.status] || statusConfig.InReview;
                            const taskTime = task.startDate
                                ? new Date(task.startDate).toLocaleTimeString('en-US', {
                                      hour: '2-digit',
                                      minute: '2-digit',
                                      hour12: false,
                                  })
                                : 'N/A';
                            const estMinutes = task.estimation;
                            const estLabel = formatEstimation(estMinutes);

                            return (
                                <div
                                    key={task.taskID}
                                    draggable={isStatusDraggable(task.status)}
                                    onDragStart={(e) => handleDragStart(e, task)}
                                    onDragEnd={handleDragEnd}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        navigate(`/tasks/${task.taskID}`);
                                    }}
                                    title={`${task.serviceName}\nTime: ${taskTime}${estLabel ? `\nEstimation: ${estLabel}` : ''}\nStatus: ${config.display}\nLocation: ${task.addressCity || 'N/A'}\nCustomer: ${task.userName || 'N/A'}\nTasker: ${task.taskerName || 'N/A'}`}
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
                                        cursor: isStatusDraggable(task.status) ? 'move' : 'pointer',
                                        transition: 'opacity 0.2s ease',
                                    }}
                                >
                                    <div
                                        style={{
                                            display: 'flex',
                                            gap: '6px',
                                            justifyContent: 'space-between',
                                            alignItems: 'center',
                                        }}
                                    >
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
                            <div
                                style={{
                                    fontSize: '10px',
                                    color: '#999',
                                    fontWeight: '500',
                                }}
                            >
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
                <div
                    style={{
                        display: 'grid',
                        gridTemplateColumns: 'repeat(7, 1fr)',
                        border: '1px solid #ddd',
                        borderTop: 'none',
                        borderBottomLeftRadius: '12px',
                        borderBottomRightRadius: '12px',
                        overflow: 'hidden',
                    }}
                >
                    {blanks}
                    {days}
                </div>
            </div>
        );
    };

    // ==================== RENDER SNACKBAR ====================
    const renderSnackbar = () => {
        if (!snackbar.show) return null;

        return (
            <div
                style={{
                    position: 'fixed',
                    bottom: '24px',
                    right: '24px',
                    backgroundColor: snackbar.type === 'error' ? '#991B1B' : '#065F46',
                    color: '#ffffff',
                    padding: '16px 24px',
                    borderRadius: '8px',
                    boxShadow: '0 4px 6px rgba(0, 0, 0, 0.3)',
                    zIndex: 1000,
                    maxWidth: '500px',
                    animation: 'slideIn 0.3s ease-out',
                }}
            >
                {snackbar.message}
            </div>
        );
    };

    // ==================== RENDER TIME MODAL ====================
    const renderTimeModal = () => {
        if (!showTimeModal) return null;

        return (
            <div
                style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundColor: 'rgba(0, 0, 0, 0.5)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    zIndex: 1000,
                }}
                onClick={() => {
                    setShowTimeModal(false);
                    setDraggedTask(null);
                    setBusyTimes([]);
                    setTaskEstimation(0);
                }}
            >
                <div
                    style={{
                        backgroundColor: '#ffffff',
                        borderRadius: '12px',
                        padding: '24px',
                        maxWidth: '450px',
                        width: '90%',
                        maxHeight: '80vh',
                        overflowY: 'auto',
                        boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)',
                    }}
                    onClick={(e) => e.stopPropagation()}
                >
                    <h2 style={{ margin: '0 0 8px 0', fontSize: '20px', fontWeight: '700' }}>
                        Select Time for Reschedule
                    </h2>
                    <p style={{ margin: '0 0 16px 0', color: '#666', fontSize: '14px' }}>
                        Choose a time for: {selectedDate?.toLocaleDateString()}
                    </p>
                    
                    {draggedTask && (
                        <div style={{ marginBottom: '16px', padding: '12px', backgroundColor: '#f9fafb', borderRadius: '8px' }}>
                            <div style={{ fontSize: '12px', fontWeight: '600', color: '#666', marginBottom: '4px' }}>
                                Task Details:
                            </div>
                            <div style={{ fontSize: '14px', color: '#111827' }}>
                                {draggedTask.serviceName}
                            </div>
                            {taskEstimation > 0 && (
                                <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
                                    Estimation: {formatEstimation(taskEstimation)}
                                </div>
                            )}
                        </div>
                    )}

                    {loadingBusyTimes ? (
                        <div style={{ textAlign: 'center', padding: '24px' }}>
                            <Loader style={{ width: '32px', height: '32px', color: '#a7df2d', animation: 'spin 1s linear infinite', margin: '0 auto' }} />
                            <p style={{ marginTop: '12px', color: '#666' }}>Loading available times...</p>
                        </div>
                    ) : (
                        <>
                            <label style={{ display: 'block', marginBottom: '8px', fontWeight: '600', fontSize: '14px' }}>
                                New Time*:
                            </label>
                            <select
                                value={selectedTime}
                                onChange={(e) => setSelectedTime(e.target.value)}
                                style={{
                                    width: '100%',
                                    padding: '12px',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '8px',
                                    fontSize: '16px',
                                    marginBottom: '16px',
                                    backgroundColor: '#ffffff',
                                }}
                            >
                                <option value="">
                                    {availableTimeSlots.length === 0 ? 'No available times' : 'Select time'}
                                </option>
                                {availableTimeSlots.map((slot) => (
                                    <option key={slot} value={slot}>
                                        {slot}
                                    </option>
                                ))}
                            </select>

                            {selectedDate && availableTimeSlots.length === 0 && (
                                <p style={{ marginBottom: '16px', color: '#dc2626', fontSize: '14px' }}>
                                    No available time slots on this day. All slots are either in the past or conflict with existing schedules.
                                </p>
                            )}

                            {availableTimeSlots.length > 0 && (
                                <p style={{ marginBottom: '16px', color: '#6b7280', fontSize: '12px' }}>
                                    Available time slots are shown. Busy slots and times that would extend beyond working hours (20:30) are filtered out.
                                </p>
                            )}
                        </>
                    )}

                    <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
                        <button
                            onClick={() => {
                                setShowTimeModal(false);
                                setDraggedTask(null);
                                setBusyTimes([]);
                                setTaskEstimation(0);
                            }}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: '#f3f4f6',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontWeight: '600',
                                fontSize: '14px',
                            }}
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleTimeConfirm}
                            disabled={!selectedTime || loadingBusyTimes}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: !selectedTime || loadingBusyTimes ? '#e5e7eb' : '#c6ff4d',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: !selectedTime || loadingBusyTimes ? 'not-allowed' : 'pointer',
                                fontWeight: '600',
                                fontSize: '14px',
                                opacity: !selectedTime || loadingBusyTimes ? 0.5 : 1,
                            }}
                        >
                            Confirm Reschedule
                        </button>
                    </div>
                </div>
            </div>
        );
    };

    // ==================== EFFECTS ====================
    useEffect(() => {
        fetchTasks();
        // eslint-disable-next-line
    }, [currentDate, filteredStatus, userRole]);

    // Add CSS animation
    useEffect(() => {
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
            @keyframes spin {
                from {
                    transform: rotate(0deg);
                }
                to {
                    transform: rotate(360deg);
                }
            }
        `;
        if (!document.head.querySelector('style[data-calendar-animations]')) {
            style.setAttribute('data-calendar-animations', 'true');
            document.head.appendChild(style);
        }
    }, []);

    // ==================== LOADING STATE ====================
    if (loading) {
        return (
            <div
                style={{
                    minHeight: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: '#ffffff',
                    padding: '24px',
                }}
            >
                <div style={{ textAlign: 'center' }}>
                    <Loader
                        style={{ width: '48px', height: '48px', color: '#a7df2d', animation: 'spin 1s linear infinite' }}
                    />
                    <p style={{ marginTop: '24px', fontSize: '18px', fontWeight: '600', color: '#4b5563' }}>
                        Loading your tasks...
                    </p>
                </div>
            </div>
        );
    }

    // ==================== ERROR STATE ====================
    if (error) {
        return (
            <div
                style={{
                    minHeight: '100vh',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: '#ffffff',
                    padding: '24px',
                }}
            >
                <div
                    style={{
                        maxWidth: '500px',
                        width: '100%',
                        backgroundColor: '#ffffff',
                        borderRadius: '24px',
                        border: '1px solid #e5e7eb',
                        padding: '32px',
                        textAlign: 'center',
                        boxShadow: '0 25px 50px rgba(15, 23, 42, 0.08)',
                    }}
                >
                    <div style={{ fontSize: '48px', marginBottom: '16px' }}>⚠️</div>
                    <div style={{ fontSize: '24px', fontWeight: '700', color: '#991B1B', marginBottom: '12px' }}>
                        Error Loading Tasks
                    </div>
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
                        onMouseOver={(e) => (e.target.style.backgroundColor = '#a7df2d')}
                        onMouseOut={(e) => (e.target.style.backgroundColor = '#c6ff4d')}
                    >
                        🔄 Try Again
                    </button>
                </div>
            </div>
        );
    }

    // ==================== MAIN RENDER ====================
    return (
        <div
            style={{
                minHeight: '100vh',
                backgroundColor: '#ffffff',
                padding: '40px 20px',
            }}
        >
            <div style={{ maxWidth: '1200px', margin: '0 auto' }}>
                <div
                    style={{
                        backgroundColor: '#ffffff',
                        borderRadius: '12px',
                        padding: '24px',
                        marginBottom: '20px',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                    }}
                >
                    <div
                        style={{
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'space-between',
                            marginBottom: '16px',
                        }}
                    >
                        <h1
                            style={{
                                fontSize: '28px',
                                fontWeight: '700',
                                color: '#111827',
                                margin: 0,
                            }}
                        >
                            {`${currentDate.toLocaleString('default', { month: 'long' })} ${currentDate.getFullYear()}`}
                        </h1>

                        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                            <button
                                onClick={handlePreviousMonth}
                                style={{
                                    padding: '8px 12px',
                                    backgroundColor: isPrevDisabled ? '#e5e7eb' : '#f3f4f6',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '8px',
                                    cursor: isPrevDisabled ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    transition: 'all 0.2s ease',
                                    opacity: isPrevDisabled ? 0.5 : 1,
                                }}
                                onMouseOver={(e) => {
                                    if (!isPrevDisabled) e.target.style.backgroundColor = '#e5e7eb';
                                }}
                                onMouseOut={(e) => {
                                    if (!isPrevDisabled) e.target.style.backgroundColor = '#f3f4f6';
                                }}
                                disabled={isPrevDisabled}
                            >
                                <ChevronLeft style={{ width: '20px', height: '20px' }} />
                            </button>
                            <button
                                onClick={handleNextMonth}
                                style={{
                                    padding: '8px 12px',
                                    backgroundColor: isNextDisabled ? '#e5e7eb' : '#f3f4f6',
                                    border: '1px solid #e5e7eb',
                                    borderRadius: '8px',
                                    cursor: isNextDisabled ? 'not-allowed' : 'pointer',
                                    display: 'flex',
                                    alignItems: 'center',
                                    transition: 'all 0.2s ease',
                                    opacity: isNextDisabled ? 0.5 : 1,
                                }}
                                onMouseOver={(e) => {
                                    if (!isNextDisabled) e.target.style.backgroundColor = '#e5e7eb';
                                }}
                                onMouseOut={(e) => {
                                    if (!isNextDisabled) e.target.style.backgroundColor = '#f3f4f6';
                                }}
                                disabled={isNextDisabled}
                            >
                                <ChevronRight style={{ width: '20px', height: '20px' }} />
                            </button>
                        </div>
                    </div>

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

                <div
                    style={{
                        backgroundColor: '#ffffff',
                        borderRadius: '12px',
                        overflow: 'hidden',
                        boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)',
                        minHeight: '300px',
                    }}
                >
                    {renderCalendar()}
                </div>
            </div>
            {renderSnackbar()}
            {renderTimeModal()}
        </div>
    );
}

export default TaskCalendar;