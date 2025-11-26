import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

// Dashboard / overview removed — redirect to users table
const Dashboard = () => {
  const navigate = useNavigate();
  useEffect(() => {
    navigate('/admin/users', { replace: true });
  }, [navigate]);
  return null;
};

export default Dashboard;

