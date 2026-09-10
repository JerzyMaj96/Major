import { useEffect, useState } from "react";
import "./DashboardPage.css";
import type { Task } from "../../types/types";
import { taskService } from "../../api/services";
import ControlPointIcon from "@mui/icons-material/ControlPoint";

function DashboardPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    const fetchTasks = async () => {
      try {
        const data = await taskService.getTasks();
        setTasks(data);
      } catch (error) {
        if (error instanceof Error) {
          alert("Error: " + error.message);
        } else {
          alert("An unknown error occurred");
        }
      }
    };

    fetchTasks();
  }, []);

  return (
    <div className="dashboard-page">
      <h1>Dashboard</h1>
      <p>Welcome to the dashboard!</p>
      <table>
        <thead>
          <tr>
            <th>Backlog</th>
            <th>In Progress</th>
            <th>In Review</th>
            <th>Done</th>
          </tr>
        </thead>
        {tasks.length > 0 ? (
          tasks.map((task) => (
            <tbody key={task.id}>
              <tr>
                <td>{task.title}</td>
                <td>{task.description}</td>
              </tr>
            </tbody>
          ))
        ) : (
          <ControlPointIcon
            className="add-task-icon"
            onClick={() => setShowCreateModal(true)}
          />
        )}

        {showCreateModal && (
          <div className="modal">
            <div className="modal-content">
              <span className="close" onClick={() => setShowCreateModal(false)}>
                &times;
              </span>
              <h2>Create Task</h2>
              {/* Create Task Form */}
            </div>
          </div>
        )}
      </table>
    </div>
  );
}

export default DashboardPage;
