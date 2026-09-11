import { useEffect, useState } from "react";
import "./DashboardPage.css";
import type { Task, TaskStatus } from "../../types/types";
import { taskService } from "../../api/services";
import ControlPointIcon from "@mui/icons-material/ControlPoint";
import CreateTaskModal from "../../components/CreateTaskModal/CreateTaskModal";

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: "BACKLOG", label: "Backlog" },
  { status: "IN_PROGRESS", label: "In Progress" },
  { status: "IN_REVIEW", label: "In Review" },
  { status: "DONE", label: "Done" },
];

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
  }, [tasks]);

  return (
    <div className="dashboard-page">
      <h1>Dashboard</h1>
      <p>Welcome to the dashboard!</p>
      <div className="board">
        {COLUMNS.map((column) => {
          const columnTasks = tasks.filter(
            (task) => task.status === column.status,
          );

          return (
            <div className="board-column" key={column.status}>
              <div className="board-column-header">
                <span>{column.label}</span>
                <span className="board-column-count">{columnTasks.length}</span>
              </div>

              <div className="board-column-body">
                {columnTasks.map((task) => (
                  <div className="task-card" key={task.id}>
                    <p className="task-card-title">{task.title}</p>
                    <p className="task-card-description">{task.description}</p>
                  </div>
                ))}

                {column.status === "BACKLOG" && (
                  <ControlPointIcon
                    className="add-task-icon"
                    onClick={() => setShowCreateModal(true)}
                  />
                )}
              </div>
            </div>
          );
        })}
      </div>

      {showCreateModal && (
        <div className="modal">
          <div className="modal-content">
            <span className="close" onClick={() => setShowCreateModal(false)}>
              &times;
            </span>
            {showCreateModal && <CreateTaskModal />}
          </div>
        </div>
      )}
    </div>
  );
}

export default DashboardPage;
