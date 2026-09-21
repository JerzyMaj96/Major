import { useState } from "react";
import "./Header.css";
import { useAuth } from "../../hooks/useAuth";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import { userService } from "../../api/services";
import LogoutIcon from "@mui/icons-material/Logout";
import { useNavigate, useLocation } from "react-router-dom";

function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [pagesMenuOpen, setPagesMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const togglePagesMenu = () => setPagesMenuOpen((prev) => !prev);
  const toggleUserMenu = () => setUserMenuOpen((prev) => !prev);
  const closeUserMenu = () => setUserMenuOpen(false);

  const goToPage = (path: string) => {
    navigate(path);
    setPagesMenuOpen(false);
  };

  const handleDeleteAccount = async () => {
    if (!window.confirm(`Would you like to delete your account?`)) {
      return closeUserMenu();
    }

    try {
      await userService.deleteAccount();
      alert("Account deleted successfully");
      logout();
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

  return (
    <header>
      <h1 onClick={togglePagesMenu}>MAJOR</h1>

      {user && (
        <div className={`pages-menu ${pagesMenuOpen ? "open" : ""}`}>
          <h2
            className={location.pathname === "/dashboard" ? "active" : ""}
            onClick={() => goToPage("/dashboard")}
          >
            Dashboard
          </h2>
          <h2
            className={location.pathname === "/activity-logs" ? "active" : ""}
            onClick={() => goToPage("/activity-logs")}
          >
            Activity Logs
          </h2>
        </div>
      )}

      {user && (
        <div>
          <div className="user-box" onClick={toggleUserMenu}>
            <AccountCircleIcon />
            <p className="user-name">Hello, {user.name}!</p>
            <div>
              {userMenuOpen && (
                <div className="user-menu">
                  <button
                    onClick={() => {
                      logout();
                      closeUserMenu();
                    }}
                  >
                    <LogoutIcon className="logout-icon" /> Log out
                  </button>
                  <button onClick={handleDeleteAccount}>Delete Account</button>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </header>
  );
}

export default Header;
