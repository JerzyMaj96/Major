import { taskService } from "../../api/services";
import type { Task, TaskStatus } from "../../types/types";
import "./TaskDetailsModal.css";

const STATUS_LABELS: Record<TaskStatus, string> = {
  BACKLOG: "Backlog",
  IN_PROGRESS: "In Progress",
  IN_REVIEW: "In Review",
  DONE: "Done",
};

interface TaskDetailsModalProps {
  task: Task;
  onTaskDeleted: () => void;
}

function TaskDetailsModal({ task, onTaskDeleted }: TaskDetailsModalProps) {
  const handleDeleteTask = async () => {
    if (!confirm("Are you sure you want to delete this task?")) return;

    try {
      await taskService.deleteTask(task.id);
      onTaskDeleted();
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

  return (
    <div className="task-details-modal">
      <h2>{task.title}</h2>
      <div className="task-details-form">
        <div className="task-details-field">
          <label className="task-details-label" htmlFor="description">
            Description
          </label>
          <p className="task-details-description">
            {task.description || "No description provided."}
          </p>
        </div>

        <div className="task-details-field">
          <label className="task-details-label" htmlFor="assignee">
            Assignee
          </label>
          <p className="task-details-value">
            {task.assignee ? task.assignee.name : "Unassigned"}
          </p>
        </div>

        <div className="task-details-field">
          <label className="task-details-label" htmlFor="createdBy">
            Created By
          </label>
          <p className="task-details-value">{task.createdBy.name}</p>
        </div>

        <div className="task-details-field">
          <label className="task-details-label" htmlFor="status">
            Status
          </label>
          <p className="task-details-value">{STATUS_LABELS[task.status]}</p>
        </div>

        <div className="task-details-actions">
          <button
            type="button"
            className="task-details-delete-btn"
            onClick={handleDeleteTask}
          >
            Delete Task
          </button>
        </div>
      </div>
    </div>
  );
}

export default TaskDetailsModal;
