import React, { useState, useEffect } from 'react';
import TaskCalendar from '../components/TaskCalendar';
import { Calendar, Sparkles, TrendingUp, CheckCircle2, Clock } from 'lucide-react';
import { baseUrl } from '../utils/apiClient';

/**
 * TaskCalendarPage - Ultra Modern Edition
 * A stunning page wrapper for the TaskCalendar component with stats and animations
 */
const TaskCalendarPage = () => {
    const [userType, setUserType] = useState('user');
    const [stats, setStats] = useState({
        total: 0,
        completed: 0,
        inProgress: 0,
        pending: 0
    });
    const [mounted, setMounted] = useState(false);

    useEffect(() => {
        setMounted(true);
        // Mock stats - replace with real data from your API
        setStats({
            total: 24,
            completed: 12,
            inProgress: 8,
            pending: 4
        });
    }, []);

    // Mock user data - replace with real authentication context
    const userName = "John Doe";
    const userRole = userType === 'tasker' ? 'Service Provider' : 'Customer';

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-indigo-50/30">
            {/* Animated Background Elements */}
            <div className="fixed inset-0 overflow-hidden pointer-events-none">
                <div className="absolute -top-40 -right-40 w-96 h-96 bg-gradient-to-br from-indigo-400/20 to-purple-400/20 rounded-full blur-3xl animate-float"></div>
                <div className="absolute -bottom-40 -left-40 w-96 h-96 bg-gradient-to-br from-blue-400/20 to-cyan-400/20 rounded-full blur-3xl animate-float-delayed"></div>
                <div className="absolute top-1/2 left-1/2 w-96 h-96 bg-gradient-to-br from-pink-400/10 to-rose-400/10 rounded-full blur-3xl animate-pulse"></div>
            </div>

            <div className="relative z-10 max-w-[1600px] mx-auto px-4 py-8 md:px-8">
                {/* Header Section with Glassmorphism */}
                <div className={`mb-8 transition-all duration-1000 ${mounted ? 'opacity-100 translate-y-0' : 'opacity-0 -translate-y-10'}`}>
                    <div className="bg-white/60 backdrop-blur-2xl rounded-3xl shadow-2xl border border-white/50 p-8 relative overflow-hidden">
                        {/* Gradient Overlay */}
                        <div className="absolute inset-0 bg-gradient-to-r from-indigo-500/5 via-purple-500/5 to-pink-500/5"></div>
                        
                        <div className="relative z-10">
                            {/* Welcome Section */}
                            <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6 mb-8">
                                <div>
                                    <div className="flex items-center gap-3 mb-3">
                                        <div className="w-12 h-12 bg-gradient-to-br from-indigo-600 via-purple-600 to-pink-600 rounded-2xl flex items-center justify-center shadow-lg shadow-indigo-300/50 animate-scale-pulse">
                                            <Calendar className="w-6 h-6 text-white" />
                                        </div>
                                        <div>
                                            <h1 className="text-4xl font-black bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 bg-clip-text text-transparent">
                                                Welcome back, {userName}!
                                            </h1>
                                            <p className="text-gray-600 font-medium flex items-center gap-2 mt-1">
                                                <span className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></span>
                                                {userRole}
                                            </p>
                                        </div>
                                    </div>
                                    <p className="text-gray-600 text-lg ml-15">
                                        Here's an overview of your task schedule
                                    </p>
                                </div>

                                {/* Quick Actions */}
                                <div className="flex gap-3">
                                    <button className="px-6 py-3 bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600 text-white rounded-xl font-bold shadow-lg shadow-indigo-300/50 hover:shadow-xl hover:scale-105 transition-all duration-300 flex items-center gap-2">
                                        <Sparkles className="w-5 h-5" />
                                        New Task
                                    </button>
                                    <button className="px-6 py-3 bg-white/80 backdrop-blur-sm text-gray-700 rounded-xl font-bold border-2 border-gray-200 hover:border-indigo-400 hover:bg-indigo-50 transition-all duration-300 hover:scale-105 shadow-md">
                                        View All
                                    </button>
                                </div>
                            </div>

                            {/* Stats Cards */}
                            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                                {/* Total Tasks */}
                                <div className="group bg-gradient-to-br from-blue-50 to-cyan-50 rounded-2xl p-6 border-2 border-blue-200/50 hover:border-blue-400 transition-all duration-300 hover:shadow-xl hover:-translate-y-1 cursor-pointer">
                                    <div className="flex items-start justify-between mb-4">
                                        <div className="p-3 bg-blue-500/10 rounded-xl group-hover:bg-blue-500 transition-all duration-300">
                                            <Calendar className="w-6 h-6 text-blue-600 group-hover:text-white transition-colors" />
                                        </div>
                                        <span className="text-xs font-bold px-3 py-1 bg-blue-500/20 text-blue-700 rounded-full">
                                            All Time
                                        </span>
                                    </div>
                                    <h3 className="text-3xl font-black text-gray-800 mb-1">
                                        {stats.total}
                                    </h3>
                                    <p className="text-sm font-semibold text-gray-600">Total Tasks</p>
                                </div>

                                {/* Completed */}
                                <div className="group bg-gradient-to-br from-emerald-50 to-green-50 rounded-2xl p-6 border-2 border-emerald-200/50 hover:border-emerald-400 transition-all duration-300 hover:shadow-xl hover:-translate-y-1 cursor-pointer">
                                    <div className="flex items-start justify-between mb-4">
                                        <div className="p-3 bg-emerald-500/10 rounded-xl group-hover:bg-emerald-500 transition-all duration-300">
                                            <CheckCircle2 className="w-6 h-6 text-emerald-600 group-hover:text-white transition-colors" />
                                        </div>
                                        <span className="text-xs font-bold px-3 py-1 bg-emerald-500/20 text-emerald-700 rounded-full">
                                            +{Math.round((stats.completed / stats.total) * 100)}%
                                        </span>
                                    </div>
                                    <h3 className="text-3xl font-black text-gray-800 mb-1">
                                        {stats.completed}
                                    </h3>
                                    <p className="text-sm font-semibold text-gray-600">Completed</p>
                                </div>

                                {/* In Progress */}
                                <div className="group bg-gradient-to-br from-purple-50 to-pink-50 rounded-2xl p-6 border-2 border-purple-200/50 hover:border-purple-400 transition-all duration-300 hover:shadow-xl hover:-translate-y-1 cursor-pointer">
                                    <div className="flex items-start justify-between mb-4">
                                        <div className="p-3 bg-purple-500/10 rounded-xl group-hover:bg-purple-500 transition-all duration-300">
                                            <TrendingUp className="w-6 h-6 text-purple-600 group-hover:text-white transition-colors" />
                                        </div>
                                        <span className="text-xs font-bold px-3 py-1 bg-purple-500/20 text-purple-700 rounded-full flex items-center gap-1">
                                            <span className="w-1.5 h-1.5 bg-purple-600 rounded-full animate-pulse"></span>
                                            Active
                                        </span>
                                    </div>
                                    <h3 className="text-3xl font-black text-gray-800 mb-1">
                                        {stats.inProgress}
                                    </h3>
                                    <p className="text-sm font-semibold text-gray-600">In Progress</p>
                                </div>

                                {/* Pending */}
                                <div className="group bg-gradient-to-br from-amber-50 to-orange-50 rounded-2xl p-6 border-2 border-amber-200/50 hover:border-amber-400 transition-all duration-300 hover:shadow-xl hover:-translate-y-1 cursor-pointer">
                                    <div className="flex items-start justify-between mb-4">
                                        <div className="p-3 bg-amber-500/10 rounded-xl group-hover:bg-amber-500 transition-all duration-300">
                                            <Clock className="w-6 h-6 text-amber-600 group-hover:text-white transition-colors" />
                                        </div>
                                        <span className="text-xs font-bold px-3 py-1 bg-amber-500/20 text-amber-700 rounded-full">
                                            Waiting
                                        </span>
                                    </div>
                                    <h3 className="text-3xl font-black text-gray-800 mb-1">
                                        {stats.pending}
                                    </h3>
                                    <p className="text-sm font-semibold text-gray-600">Pending Review</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Calendar Component */}
                <div className={`transition-all duration-1000 delay-300 ${mounted ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-10'}`}>
                    <TaskCalendar
                        userType={userType}
                        baseUrl={baseUrl}
                    />
                </div>

                {/* Footer Info */}
                <div className={`mt-8 text-center transition-all duration-1000 delay-500 ${mounted ? 'opacity-100' : 'opacity-0'}`}>
                    <div className="inline-flex items-center gap-3 px-6 py-3 bg-white/60 backdrop-blur-xl rounded-full border border-white/50 shadow-lg">
                        <div className="flex items-center gap-2">
                            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                            <span className="text-sm font-semibold text-gray-600">System Status: Online</span>
                        </div>
                        <span className="text-gray-300">•</span>
                        <span className="text-sm font-semibold text-gray-600">
                            Last updated: {new Date().toLocaleTimeString()}
                        </span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TaskCalendarPage;