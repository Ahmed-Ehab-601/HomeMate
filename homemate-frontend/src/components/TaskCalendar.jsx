import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Loader } from 'lucide-react';
import { rescheduleTask, getTaskerBusyTime, getTaskEstimation, getTaskDetails } from '../api/taskManagementApi';
import { useAuth } from '../contexts/AuthContext';

/**
 * TaskCalendar Component - Fixed Drag & Drop
 * Now matches TaskDetailsPage reschedule logic exactly
 */
const TaskCalendar = ({ onBackToList, onTasksUpdated, userRole }) => {
    const [currentDate, setCurrentDate] = useState(new Date());
    const [tasks, setTasks] = useState([]);
    const [filteredStatus, setFilteredStatus] = useState('All');
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [draggedTask, setDraggedTask] = useState(null);
    const [snackbar, setSnackbar] = useState({ show: false, message: '', type: 'error' });
    const [showTimeModal, setShowTimeModal] = useState(false);
    const [targetDate, setTargetDate] = useState(null);
    const [targetTask, setTargetTask] = useState(null);
    const [selectedTime, setSelectedTime] = useState('');
    const [availableTimeSlots, setAvailableTimeSlots] = useState([]);
    const [loadingTimeSlots, setLoadingTimeSlots] = useState(false);
    const [rescheduleBusyTimes, setRescheduleBusyTimes] = useState({});
    const [taskEstimation, setTaskEstimation] = useState(null);
    const { user, getUserRole } = useAuth();

    // Status Colors
    const statusConfig = {
        InReview: { bg: '#FFFBEB', text: '#92400E', display: 'In Review' },
        Accepted: { bg: '#DCFCE7', text: '#166534', display: 'Accepted' },
        InProgress: { bg: '#E0F2FE', text: '#075985', display: 'In Progress' },
        Suspended: { bg: '#F3F4F6', text: '#374151', display: 'Suspended' },
        Done: { bg: '#F0FDF4', text: '#166534', display: 'Done' },
        Rejected: { bg: '#FEF2F2', text: '#991B1B', display: 'Rejected' },
    };

    const dayColors = {
        Monday: '#c6ff4d',
        Tuesday: '#b8f03d',
        Wednesday: '#aae02d',
        Thursday: '#9cd01d',
        Friday: '#8ec00d',
        Saturday: '#4b5563',
        Sunday: '#a7df2d',
    };

    // EXACT SAME CONSTANTS AS TASKDETAILSPAGE
    const WORK_DAY_START_MINUTES = 8 * 60;          // 08:00 = 480 minutes
    const WORK_DAY_END_MINUTES = 20 * 60 + 30;      // 20:30 = 1230 minutes
    const DRAGGABLE_STATUSES = ['InReview', 'Accepted'];

    const isStatusDraggable = (status) => DRAGGABLE_STATUSES.includes(status);

    // Generate time slots - EXACT SAME AS TASKDETAILSPAGE
    const timeSlots = Array.from({ length: 25 }, (_, index) => {
        const minutes = index * 30;
        const hours = 8 + Math.floor(minutes / 60);
        const mins = minutes % 60;
        if (hours > 20 || (hours === 20 && mins > 0)) {
            return null;
        }
        return `${String(hours).padStart(2, '0')}:${String(mins).padStart(2, '0')}`;
    }).filter(Boolean);

    const getDaysInMonth = (date) => {
        const year = date.getFullYear();
        const month = date.getMonth();
        const days = new Date(year, month + 1, 0).getDate();
        const firstDay = new Date(year, month, 1).getDay();
        const adjustedFirstDay = firstDay === 0 ? 6 : firstDay - 1;
        return { days, firstDay: adjustedFirstDay };
    };

    // ==================== EXACT LOGIC FROM TASKDETAILSPAGE ====================
    
    // Check if task would extend beyond working hours
    const hasEnoughTimeToComplete = (timeSlot, estimationMinutes) => {
        const [hours, minutes] = timeSlot.split(":").map(Number);
        const slotStartMinutes = hours * 60 + minutes;
        const slotEndMinutes = slotStartMinutes + estimationMinutes;
        
        if (slotEndMinutes > WORK_DAY_END_MINUTES) {
            console.log(`❌ Slot ${timeSlot}: Task would end at ${Math.floor(slotEndMinutes/60)}:${String(slotEndMinutes%60).padStart(2, '0')}, beyond 20:30`);
            return false;
        }
        
        return true;
    };

    // Check if a time slot is busy - EXACT SAME AS TASKDETAILSPAGE
    const isRescheduleTimeSlotBusy = (timeSlot, rescheduleDate, busyTimes, currentTask, estimationMinutes) => {
        if (!rescheduleDate || Object.keys(busyTimes).length === 0) {
            return false;
        }

        const [slotHours, slotMinutes] = timeSlot.split(":").map(Number);
        const [year, month, day] = rescheduleDate.split("-").map(Number);
        
        const slotStartMinutes = slotHours * 60 + slotMinutes;
        const slotEndMinutes = slotStartMinutes + estimationMinutes;

        for (const [busyStartStr, busyEstimationMinutes] of Object.entries(busyTimes)) {
            let busyStart;
            try {
                busyStart = new Date(busyStartStr);
            } catch (e) {
                console.warn("Failed to parse busy start date:", busyStartStr);
                continue;
            }
            
            const busyYear = busyStart.getFullYear();
            const busyMonth = busyStart.getMonth();
            const busyDay = busyStart.getDate();
            const busyHours = busyStart.getHours();
            const busyMins = busyStart.getMinutes();
            
            // Skip if this is the current task being rescheduled
            if (currentTask?.startDate) {
                const taskStart = new Date(currentTask.startDate);
                const taskYear = taskStart.getFullYear();
                const taskMonth = taskStart.getMonth();
                const taskDay = taskStart.getDate();
                const taskHours = taskStart.getHours();
                const taskMins = taskStart.getMinutes();
                
                if (
                    busyYear === taskYear &&
                    busyMonth === taskMonth &&
                    busyDay === taskDay &&
                    busyHours === taskHours &&
                    busyMins === taskMins
                ) {
                    continue;
                }
            }

            // Only check if dates match
            if (busyYear !== year || busyMonth !== month - 1 || busyDay !== day) {
                continue;
            }

            const busyStartMinutes = busyHours * 60 + busyMins;
            const busyEndMinutes = busyStartMinutes + busyEstimationMinutes;

            // Check for ANY overlap - ALL 5 SCENARIOS
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
        }

        return false;
    };

    // ==================== API FETCHING ====================
    const fetchTasks = async () => {
        // Don't fetch if userRole is not set
        if (!userRole) {
            console.log('⏸️ Skipping fetch - userRole not set yet');
            return;
        }
        
        setLoading(true);
        setError(null);
        try {
            const token = localStorage.getItem('homemate_token');
            if (!token) throw new Error('No authentication token found');

            const year = currentDate.getFullYear();
            const month = currentDate.getMonth();
            const startDate = `${year}-${String(month + 1).padStart(2, '0')}-01`;
            const lastDay = new Date(year, month + 1, 0).getDate();
            const endDate = `${year}-${String(month + 1).padStart(2, '0')}-${String(lastDay).padStart(2, '0')}`;

            // Use the userRole prop directly - it's passed from parent component and is the source of truth
            const role = userRole;
            
            console.log('📋 Fetching tasks with role:', role, { propRole: userRole });

            const endpoint = role === 'ROLE_TASKER' ? '/api/tasker/getTasks' : '/api/user/getTasks';
            const url = `http://localhost:8080${endpoint}?startDate=${startDate}&endDate=${endDate}&status=${filteredStatus}`;
            
            console.log('🔗 Task fetch URL:', url);

            const response = await fetch(url, {
                headers: {
                    Authorization: `Bearer ${token}`,
                    'Content-Type': 'application/json',
                },
            });

            if (response.status === 204) {
                setTasks([]);
                return;
            }
            if (response.status === 401) throw new Error('Unauthorized. Please login again.');
            if (response.status === 403) {
                console.error('❌ 403 Forbidden - likely wrong endpoint for user role. Role:', userRole, 'Endpoint:', endpoint);
                throw new Error('Access forbidden.');
            }
            if (!response.ok) {
                const errorText = await response.text().catch(() => response.statusText);
                throw new Error(`Failed to fetch tasks: ${errorText}`);
            }

            const data = await response.json();
            setTasks(Array.isArray(data) ? data : []);
        } catch (err) {
            console.error('❌ Fetch error:', err);
            setError(err.message || 'Failed to load tasks');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchTasks();
    }, [currentDate, filteredStatus, userRole]);

    // ==================== DRAG AND DROP HANDLERS ====================
    const handleDragStart = (e, task) => {
        if (!isStatusDraggable(task?.status)) {
            setSnackbar({ show: true, message: 'This task cannot be rescheduled in its current status.', type: 'error' });
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

    // Load available time slots - EXACT SAME LOGIC AS TASKDETAILSPAGE
    const loadAvailableTimeSlots = async (dateStr, task) => {
        setLoadingTimeSlots(true);

        console.log('🔍 Loading slots for:', { 
            dateStr, 
            taskID: task.taskID, 
            taskerID: task.taskerID,
            taskerId: task.taskerId,
        });

        try {
            // Use taskerID if available, otherwise use taskerId (different API endpoints use different naming)
            let taskerID = task.taskerID || task.taskerId;
            
            // If taskerID still missing, fetch full task details
            if (!taskerID) {
                console.log('📥 Fetching full task details to get taskerID...');
                try {
                    const fullTaskData = await getTaskDetails(task.taskID);
                    taskerID = fullTaskData.taskerID;
                    console.log('✅ Got taskerID from full task details:', taskerID);
                } catch (err) {
                    console.error('❌ Failed to fetch task details:', err);
                    setSnackbar({ 
                        show: true, 
                        message: 'Error: Cannot reschedule - failed to load task information. Please refresh and try again.', 
                        type: 'error' 
                    });
                    setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 4000);
                    setAvailableTimeSlots([]);
                    setLoadingTimeSlots(false);
                    return;
                }
            }
            
            if (!taskerID) {
                console.error('❌ Task missing taskerID even after fetching details. Task object:', task);
                setSnackbar({ 
                    show: true, 
                    message: 'Error: Cannot reschedule - tasker information missing. Please refresh and try again.', 
                    type: 'error' 
                });
                setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 4000);
                setAvailableTimeSlots([]);
                setLoadingTimeSlots(false);
                return;
            }

            // Use the userRole prop directly - it's passed from parent component and is the source of truth
            const role = userRole;

            // 1. Fetch task estimation
            let currentTaskEstMinutes = 60;
            try {
                currentTaskEstMinutes = await getTaskEstimation(task.taskID, role);
                setTaskEstimation(currentTaskEstMinutes);
                console.log('✅ Task estimation loaded:', currentTaskEstMinutes, 'minutes');
            } catch (err) {
                console.error('❌ Failed to load estimation:', err);
                setTaskEstimation(60);
                currentTaskEstMinutes = 60;
            }

            // 2. Fetch busy times
            let busyTimes = {};
            try {
                busyTimes = await getTaskerBusyTime(taskerID, dateStr, role);
                setRescheduleBusyTimes(busyTimes || {});
                console.log('✅ Busy times loaded for date:', dateStr, busyTimes);
                console.log('📊 Number of busy periods:', Object.keys(busyTimes || {}).length);
            } catch (err) {
                console.error('❌ Failed to fetch busy time:', err);
                console.error('Error details:', err.response?.data || err.message);
                setRescheduleBusyTimes({});
                busyTimes = {};
            }

            // 3. Filter available slots using EXACT TaskDetailsPage logic
            const [year, month, day] = dateStr.split('-').map(Number);
            
            // IMPORTANT: Create date using year, month-1, day to avoid timezone issues
            const selectedDate = new Date(year, month - 1, day);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            selectedDate.setHours(0, 0, 0, 0);
            const isToday = selectedDate.getTime() === today.getTime();

            console.log('🗓️ Date info:', {
                dateStr,
                year,
                month,
                day,
                isToday,
                selectedDate: selectedDate.toISOString(),
                today: today.toISOString(),
                estimation: currentTaskEstMinutes,
                timeSlots: timeSlots.length,
                busyTimesCount: Object.keys(busyTimes).length
            });

            const available = timeSlots.filter((slot) => {
                const [hours, minutes] = slot.split(":").map(Number);
                const slotStartMinutes = hours * 60 + minutes;
                const slotEndMinutes = slotStartMinutes + currentTaskEstMinutes;

                // 1. Check if date is today and time has passed
                if (isToday) {
                    const [slotHours, slotMins] = slot.split(":").map(Number);
                    const slotTime = new Date();
                    slotTime.setHours(slotHours, slotMins, 0, 0);
                    const now = new Date();
                    if (slotTime <= now) {
                        return false;
                    }
                }

                // 2. Check if there's enough time to complete
                if (slotEndMinutes > WORK_DAY_END_MINUTES) {
                    return false;
                }

                // 3. Check if slot conflicts with busy times
                const isBusy = isRescheduleTimeSlotBusy(slot, dateStr, busyTimes, task, currentTaskEstMinutes);
                return !isBusy;
            });

            console.log('✅ Available slots:', available.length, 'out of', timeSlots.length);
            console.log('📋 Available slots list:', available);

            setAvailableTimeSlots(available);
            
            // Auto-select current time if available
            if (task?.startDate) {
                const currentTime = new Date(task.startDate);
                const hours = String(currentTime.getHours()).padStart(2, '0');
                const minutes = String(currentTime.getMinutes()).padStart(2, '0');
                const timeSlot = `${hours}:${minutes}`;
                
                if (available.includes(timeSlot)) {
                    setSelectedTime(timeSlot);
                    console.log('🎯 Auto-selected current time:', timeSlot);
                } else {
                    setSelectedTime('');
                    console.log('⚠️ Current time not available:', timeSlot);
                }
            }
        } catch (err) {
            console.error('❌ Error loading time slots:', err);
            setSnackbar({ show: true, message: err?.message || 'Failed to load time slots', type: 'error' });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 4000);
            setAvailableTimeSlots([]);
        } finally {
            setLoadingTimeSlots(false);
        }
    };

    const handleDrop = async (e, day) => {
        e.preventDefault();
        if (!draggedTask) return;

        // Create date in local timezone to avoid timezone issues
        const newDate = new Date(currentDate.getFullYear(), currentDate.getMonth(), day);
        const now = new Date();
        now.setHours(0, 0, 0, 0);
        newDate.setHours(0, 0, 0, 0);

        if (newDate < now) {
            setSnackbar({
                show: true,
                message: 'Cannot reschedule to a past date. Please select a future date.',
                type: 'error',
            });
            setDraggedTask(null);
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 5000);
            return;
        }

        // Format date consistently - CRITICAL: Use the day parameter directly
        const year = currentDate.getFullYear();
        const month = String(currentDate.getMonth() + 1).padStart(2, '0');
        const dayStr = String(day).padStart(2, '0');
        const dateStr = `${year}-${month}-${dayStr}`;

        console.log('📅 Drop detected:', {
            day,
            dateStr,
            draggedTask: draggedTask.taskID,
            taskerID: draggedTask.taskerID
        });

        setTargetDate(dateStr);
        setTargetTask(draggedTask);
        setDraggedTask(null);
        
        await loadAvailableTimeSlots(dateStr, draggedTask);
        setShowTimeModal(true);
    };

    // EXACT VALIDATION LOGIC FROM TASKDETAILSPAGE handleReschedule
    const handleTimeSelection = async () => {
        if (!selectedTime || !targetDate || !targetTask) return;

        const [hours, minutes] = selectedTime.split(":").map(Number);
        const [year, month, day] = targetDate.split("-").map(Number);
        
        const currentTaskEstMinutes = taskEstimation || 60;
        const newStartMinutes = hours * 60 + minutes;
        const newEndMinutes = newStartMinutes + currentTaskEstMinutes;

        console.log("Validating reschedule:", {
            newTime: `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}`,
            newStartMinutes,
            newEndMinutes,
            currentTaskEstMinutes,
            workDayEnd: WORK_DAY_END_MINUTES
        });

        // 1. Check if task would extend beyond working hours
        if (newEndMinutes > WORK_DAY_END_MINUTES) {
            const endHour = Math.floor(newEndMinutes / 60);
            const endMin = newEndMinutes % 60;
            setSnackbar({
                show: true,
                message: `⏰ Cannot reschedule: This task would end at ${String(endHour).padStart(2, '0')}:${String(endMin).padStart(2, '0')}, which is beyond working hours (08:00 - 20:30). Please choose an earlier time.`,
                type: 'error'
            });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 5000);
            return;
        }

        // 2. Check for conflicts with busy times
        for (const [busyStartStr, busyEstimationMinutes] of Object.entries(rescheduleBusyTimes || {})) {
            const busyStart = new Date(busyStartStr);
            const busyYear = busyStart.getFullYear();
            const busyMonth = busyStart.getMonth();
            const busyDay = busyStart.getDate();
            const busyHours = busyStart.getHours();
            const busyMins = busyStart.getMinutes();

            if (busyYear !== year || busyMonth !== month - 1 || busyDay !== day) {
                continue;
            }

            const busyStartMinutes = busyHours * 60 + busyMins;
            const busyEndMinutes = busyStartMinutes + busyEstimationMinutes;

            // Skip current task's busy period
            if (targetTask?.startDate) {
                const taskStart = new Date(targetTask.startDate);
                const taskYear = taskStart.getFullYear();
                const taskMonth = taskStart.getMonth();
                const taskDay = taskStart.getDate();
                const taskHours = taskStart.getHours();
                const taskMins = taskStart.getMinutes();

                if (busyYear === taskYear && busyMonth === taskMonth && busyDay === taskDay &&
                    busyHours === taskHours && busyMins === taskMins) {
                    continue;
                }
            }

            // Check for overlap
            const hasOverlap = (
                (newStartMinutes >= busyStartMinutes && newStartMinutes < busyEndMinutes) ||
                (newEndMinutes > busyStartMinutes && newEndMinutes <= busyEndMinutes) ||
                (newStartMinutes <= busyStartMinutes && newEndMinutes >= busyEndMinutes) ||
                (busyStartMinutes <= newStartMinutes && busyEndMinutes >= newEndMinutes) ||
                (newStartMinutes < busyStartMinutes && newEndMinutes > busyStartMinutes)
            );

            if (hasOverlap) {
                const busyTimeStr = `${String(busyHours).padStart(2, '0')}:${String(busyMins).padStart(2, '0')}`;
                const busyDurationHours = (busyEstimationMinutes / 60).toFixed(1);
                const taskEndTime = `${String(Math.floor(newEndMinutes/60)).padStart(2, '0')}:${String(newEndMinutes%60).padStart(2, '0')}`;
                
                setSnackbar({
                    show: true,
                    message: `⏰ Cannot reschedule: Your task (${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')} - ${taskEndTime}, ${(currentTaskEstMinutes/60).toFixed(1)}h) would overlap with another scheduled period starting at ${busyTimeStr} (${busyDurationHours}h duration). Please choose a different time slot.`,
                    type: 'error'
                });
                setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 5000);
                return;
            }
        }

        // No conflicts, proceed
        const newDateTime = `${targetDate}T${selectedTime}:00`;
        setShowTimeModal(false);

        const previousTasks = [...tasks];
        const updatedTasks = tasks.map((t) => 
            t.taskID === targetTask.taskID ? { ...t, startDate: newDateTime } : t
        );
        setTasks(updatedTasks);

        try {
            await rescheduleTask(targetTask.taskID, newDateTime);
            setSnackbar({ show: true, message: '✅ Task rescheduled successfully!', type: 'success' });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'success' }), 3000);
            if (typeof onTasksUpdated === 'function') {
                onTasksUpdated();
            }
        } catch (err) {
            setTasks(previousTasks);
            
            let errorMessage = 'Failed to reschedule task';
            if (err?.response?.data) {
                if (typeof err.response.data === 'string') errorMessage = err.response.data;
                else if (err.response.data.message) errorMessage = err.response.data.message;
                else if (err.response.data.error) errorMessage = err.response.data.error;
            } else if (err?.message) {
                errorMessage = err.message;
            }

            console.error('❌ Reschedule failed:', errorMessage);
            setSnackbar({ show: true, message: errorMessage, type: 'error' });
            setTimeout(() => setSnackbar({ show: false, message: '', type: 'error' }), 5000);
        } finally {
            setSelectedTime('');
            setTargetDate(null);
            setTargetTask(null);
        }
    };

    // ==================== NAVIGATION ====================
    const handlePreviousMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
    };

    const handleNextMonth = () => {
        setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
    };

    // ==================== RENDER HELPERS ====================
    const tasksByDay = useMemo(() => {
        const map = new Map();
        tasks.forEach((task) => {
            if (!task.startDate) return;
            const date = new Date(task.startDate);
            const day = date.getDate();
            const taskMonth = date.getMonth();
            const taskYear = date.getFullYear();
            const currentMonth = currentDate.getMonth();
            const currentYear = currentDate.getFullYear();

            if (taskMonth === currentMonth && taskYear === currentYear) {
                if (!map.has(day)) map.set(day, []);
                map.get(day).push(task);
            }
        });
        return map;
    }, [tasks, currentDate]);

    const renderSnackbar = () => {
        if (!snackbar.show) return null;

        const isError = snackbar.type === 'error';
        const isSuccess = snackbar.type === 'success';
        const backgroundColor = isError ? '#991b1b' : (isSuccess ? '#166534' : '#991b1b');
        const textColor = '#ffffff';

        return (
            <div
                style={{
                    position: 'fixed',
                    bottom: '20px',
                    right: '20px',
                    backgroundColor,
                    color: textColor,
                    padding: '16px 20px',
                    borderRadius: '8px',
                    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '12px',
                    zIndex: 9999,
                    animation: 'slideIn 0.3s ease-out',
                    border: `1px solid ${isError ? '#FECACA' : '#A7F3D0'}`,
                    fontSize: '14px',
                    fontWeight: '500',
                    maxWidth: '500px',
                }}
            >
                <span style={{ fontSize: '18px' }}>{isError ? '❌' : '✅'}</span>
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
            >
                <div
                    style={{
                        backgroundColor: '#ffffff',
                        borderRadius: '12px',
                        padding: '32px',
                        maxWidth: '500px',
                        width: '90%',
                        maxHeight: '80vh',
                        overflow: 'auto',
                        boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
                    }}
                >
                    <h2 style={{ fontSize: '20px', fontWeight: '700', color: '#111827', marginBottom: '6px' }}>
                        Select Time
                    </h2>
                    <p style={{ fontSize: '14px', color: '#4b5563', marginBottom: '16px', fontWeight: '500' }}>
                        {targetDate &&
                            (() => {
                                const [year, month, day] = targetDate.split('-').map(Number);
                                const dateObj = new Date(year, month - 1, day);
                                return `Rescheduling to ${dateObj.toLocaleDateString('en-US', {
                                    weekday: 'long',
                                    year: 'numeric',
                                    month: 'long',
                                    day: 'numeric',
                                })}`;
                            })()}
                    </p>

                    {loadingTimeSlots ? (
                        <div style={{ textAlign: 'center', padding: '40px 0' }}>
                            <Loader style={{ animation: 'spin 1s linear infinite', margin: '0 auto' }} />
                            <p style={{ marginTop: '16px', color: '#6b7280' }}>Loading available time slots...</p>
                        </div>
                    ) : availableTimeSlots.length === 0 ? (
                        <div style={{ padding: '24px', backgroundColor: '#fef2f2', borderRadius: '8px', marginBottom: '24px', textAlign: 'center' }}>
                            <p style={{ color: '#991b1b', fontWeight: '600' }}>No available time slots for this date.</p>
                            <p style={{ color: '#991b1b', fontSize: '14px', marginTop: '8px' }}>
                                The tasker is fully booked or there isn't enough time before end of day (20:30). Please choose a different date.
                            </p>
                        </div>
                    ) : (
                        <div style={{ marginBottom: '24px' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                                <span style={{ fontSize: '14px', fontWeight: '600', color: '#374151' }}>
                                    Available Time Slots ({availableTimeSlots.length} slots)
                                </span>
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '8px', maxHeight: '340px', overflowY: 'auto', padding: '8px' }}>
                                {availableTimeSlots.map((slot) => (
                                    <button
                                        key={slot}
                                        onClick={() => setSelectedTime(slot)}
                                        style={{
                                            padding: '10px 12px',
                                            backgroundColor: selectedTime === slot ? '#c6ff4d' : '#ffffff',
                                            border: selectedTime === slot ? '2px solid #a7df2d' : '1px solid #e5e7eb',
                                            borderRadius: '8px',
                                            fontSize: '14px',
                                            fontWeight: selectedTime === slot ? '700' : '500',
                                            color: '#111827',
                                            cursor: 'pointer',
                                            transition: 'all 0.2s ease',
                                            textAlign: 'center',
                                        }}
                                        onMouseOver={(e) => {
                                            if (selectedTime !== slot) {
                                                e.target.style.backgroundColor = '#f3f4f6';
                                                e.target.style.borderColor = '#d1d5db';
                                            }
                                        }}
                                        onMouseOut={(e) => {
                                            if (selectedTime !== slot) {
                                                e.target.style.backgroundColor = '#ffffff';
                                                e.target.style.borderColor = '#e5e7eb';
                                            }
                                        }}
                                    >
                                        {slot}
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
                        <button
                            onClick={() => {
                                setShowTimeModal(false);
                                setSelectedTime('');
                                setTargetDate(null);
                                setTargetTask(null);
                            }}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: '#ffffff',
                                border: '1px solid #e5e7eb',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '600',
                                color: '#374151',
                                transition: 'all 0.2s ease',
                            }}
                            onMouseOver={(e) => (e.target.style.backgroundColor = '#f9fafb')}
                            onMouseOut={(e) => (e.target.style.backgroundColor = '#ffffff')}
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleTimeSelection}
                            disabled={!selectedTime || loadingTimeSlots}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: selectedTime && !loadingTimeSlots ? '#c6ff4d' : '#e5e7eb',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: selectedTime && !loadingTimeSlots ? 'pointer' : 'not-allowed',
                                fontSize: '14px',
                                fontWeight: '600',
                                color: '#111827',
                                transition: 'all 0.2s ease',
                            }}
                            onMouseOver={(e) => {
                                if (selectedTime && !loadingTimeSlots) e.target.style.backgroundColor = '#a7df2d';
                            }}
                            onMouseOut={(e) => {
                                if (selectedTime && !loadingTimeSlots) e.target.style.backgroundColor = '#c6ff4d';
                            }}
                        >
                            Confirm Reschedule
                        </button>
                    </div>
                </div>
            </div>
        );
    };

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
                                        cursor: isStatusDraggable(task.status) ? 'move' : 'not-allowed',
                                        transition: 'opacity 0.2s ease',
                                    }}
                                >
                                    <div style={{ display: 'flex', gap: '4px', justifyContent: 'space-between', alignItems: 'center' }}>
                                        <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                                            {task.serviceName || config.display}
                                        </span>
                                        <span style={{ fontSize: '9px', opacity: 0.8, whiteSpace: 'nowrap' }}>{taskTime}</span>
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
                    <Loader style={{ width: '48px', height: '48px', color: '#a7df2d', animation: 'spin 1s linear infinite' }} />
                    <p style={{ marginTop: '24px', fontSize: '18px', fontWeight: '600', color: '#4b5563' }}>Loading your tasks...</p>
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
                            {currentDate.toLocaleString('default', { month: 'long' })} {currentDate.getFullYear()}
                        </h1>

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
                                    onMouseOver={(e) => (e.target.style.backgroundColor = '#a7df2d')}
                                    onMouseOut={(e) => (e.target.style.backgroundColor = '#c6ff4d')}
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
                                onMouseOver={(e) => (e.target.style.backgroundColor = '#e5e7eb')}
                                onMouseOut={(e) => (e.target.style.backgroundColor = '#f3f4f6')}
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
                                onMouseOver={(e) => (e.target.style.backgroundColor = '#e5e7eb')}
                                onMouseOut={(e) => (e.target.style.backgroundColor = '#f3f4f6')}
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
                    }}
                >
                    {renderCalendar()}
                </div>
            </div>
            {renderSnackbar()}
            {renderTimeModal()}
        </div>
    );
};

export default TaskCalendar;