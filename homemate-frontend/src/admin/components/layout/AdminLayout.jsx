import { useMemo } from "react";
import { Outlet, NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/AuthContext";
import "../../styles/admin.css";
import {
  AppBar,
  Toolbar,
  Typography,
  CssBaseline,
  Box,
  Stack,
  Avatar,
  Badge,
  Container,
  Chip,
  Button,
  IconButton,
} from "@mui/material";
// Dashboard (overview) removed — icon import dropped
import PeopleIcon from "@mui/icons-material/People";
import HandymanIcon from "@mui/icons-material/Handyman";
import MiscellaneousServicesIcon from "@mui/icons-material/MiscellaneousServices";
import ReportIcon from "@mui/icons-material/Report";
import LogoutIcon from "@mui/icons-material/Logout";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import LightModeIcon from "@mui/icons-material/LightMode";
import BarChartIcon from "@mui/icons-material/BarChart";
import { useAdminThemeMode } from "../../theme/ThemeProviderSwitcher";

const navItems = [
  {
    label: "Users",
    path: "/admin/users",
    icon: <PeopleIcon fontSize="small" />,
  },
  {
    label: "Taskers",
    path: "/admin/taskers",
    icon: <HandymanIcon fontSize="small" />,
  },
  {
    label: "Services",
    path: "/admin/services",
    icon: <MiscellaneousServicesIcon fontSize="small" />,
  },
  {
    label: "Reports",
    path: "/admin/reports",
    icon: <ReportIcon fontSize="small" />,
  },
  {
    label: "Analysis",
    path: "/admin/analysis",
    icon: <BarChartIcon fontSize="small" />,
  },
];

const AdminLayout = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { mode, toggleMode } = useAdminThemeMode();
  const { logout, user } = useAuth();

  const handleLogout = () => {
    logout();
    navigate("/signin");
  };

  const activePath = useMemo(() => {
    if (location.pathname.startsWith("/admin/taskers")) return "/admin/taskers";
    if (location.pathname.startsWith("/admin/services"))
      return "/admin/services";
    if (location.pathname.startsWith("/admin/reports")) return "/admin/reports";
    if (location.pathname.startsWith("/admin/analysis")) return "/admin/analysis";
    if (location.pathname.startsWith("/admin/users")) return "/admin/users";

    return "/admin/users";
  }, [location.pathname]);

  return (
    <Box sx={{ display: "flex" }}>
      <CssBaseline />
      <AppBar
        position="fixed"
        color="inherit"
        elevation={0}
        sx={{
          zIndex: (theme) => theme.zIndex.drawer + 1,
          borderBottom: "1px solid",
          borderColor: "divider",
          backdropFilter: "blur(10px)",
        }}
      >
        <Toolbar
          sx={{ justifyContent: "space-between", gap: 2, flexWrap: "wrap" }}
        >
          <Stack direction="row" spacing={2} alignItems="center">
            <Typography variant="h6" sx={{ fontWeight: 700, mr: 1 }}>
              HomeMate Admin
            </Typography>
            <Stack direction="row" spacing={1} flexWrap="wrap">
              {navItems.map((item) => (
                <Button
                  key={item.path}
                  startIcon={item.icon}
                  variant={activePath === item.path ? "contained" : "text"}
                  color={activePath === item.path ? "primary" : "inherit"}
                  onClick={(e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    navigate(item.path);
                  }}
                  type="button"
                  sx={{
                    borderRadius: 999,
                    px: 2,
                    textTransform: "none",
                    cursor: "pointer",
                    "&.active": {
                      backgroundColor: "primary.main",
                      color: "primary.contrastText",
                    },
                  }}
                >
                  {item.label}
                </Button>
              ))}
            </Stack>
          </Stack>
          <Stack direction="row" alignItems="center" spacing={2}>
            <Chip
              label={(() => {
                const p = location.pathname.replace("/admin", "") || "/users";
                const name = p.replace("/", "") || "users";
                return name.charAt(0).toUpperCase() + name.slice(1);
              })()}
              color="primary"
              variant="soft"
              sx={{
                fontWeight: 600,
                display: { xs: "none", md: "inline-flex" },
              }}
            />
            <IconButton onClick={toggleMode} color="inherit" size="small">
              {mode === "dark" ? <LightModeIcon /> : <DarkModeIcon />}
            </IconButton>
            <Badge color="secondary" variant="dot">
              <NotificationsNoneIcon />
            </Badge>
            <Button
              startIcon={<LogoutIcon />}
              onClick={handleLogout}
              color="inherit"
              sx={{ textTransform: "none" }}
            >
              Sign out
            </Button>
            <Stack direction="row" spacing={1} alignItems="center">
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {user?.username || "Admin"}
              </Typography>
              <Avatar sx={{ bgcolor: "primary.main", width: 36, height: 36 }}>
                {user?.username?.charAt(0)?.toUpperCase() || "A"}
              </Avatar>
            </Stack>
          </Stack>
        </Toolbar>
      </AppBar>
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          py: 4,
          px: { xs: 2, md: 4 },
          pr: 0,
          minHeight: "100vh",
          width: "100%",
          maxWidth: "100vw",
          overflowX: "hidden",
          background: (theme) =>
            theme.palette.mode === "dark"
              ? theme.palette.background.default
              : "linear-gradient(180deg, #f8f9ff 0%, #f1f4ff 60%, #eff5ff 100%)",
        }}
      >
        <Toolbar />
        <Container
          maxWidth={false}
          sx={{ px: 0, pr: 0, width: "100%", maxWidth: "100%" }}
        >
          <Outlet />
        </Container>
      </Box>
    </Box>
  );
};

export default AdminLayout;
