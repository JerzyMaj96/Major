import { useEffect, useState } from "react";
import "./DashboardPage.css";
import type { Task, TaskStatus } from "../../types/types";
import { taskService } from "../../api/services";
import CreateTaskModal from "../../components/CreateTaskModal/CreateTaskModal";
import { useTaskWebSocket } from "../../hooks/useTaskWebSocket";
import { DragDropProvider, type DragEndEvent } from "@dnd-kit/react";
import DroppableColumn from "../../components/DroppableColumn/DroppableColumn";

const COLUMNS: { status: TaskStatus; label: string }[] = [
  { status: "BACKLOG", label: "Backlog" },
  { status: "IN_PROGRESS", label: "In Progress" },
  { status: "IN_REVIEW", label: "In Review" },
  { status: "DONE", label: "Done" },
];

function DashboardPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [showCreateModal, setShowCreateModal] = useState(false);

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

  useEffect(() => {
    let ignore = false;

    taskService
      .getTasks()
      .then((data) => {
        if (!ignore) setTasks(data);
      })
      .catch((error) => {
        if (!ignore) {
          alert(
            error instanceof Error
              ? "Error: " + error.message
              : "An unknown error occurred",
          );
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  useTaskWebSocket((updatedTask) => {
    setTasks((prevTasks) =>
      prevTasks.map((task) =>
        task.id === updatedTask.id ? updatedTask : task,
      ),
    );
  });

  const handleDragEnd = async (event: DragEndEvent) => {
    if (event.canceled) return;

    console.log("Full event:", event);
    console.log("Source:", event.operation.source);
    console.log("Target:", event.operation.target);

    const source = event.operation.source;
    const target = event.operation.target;

    if (!source || !target) return;

    const taskId = source.id as number;
    const newStatus = target.id as TaskStatus;

    try {
      const updatedTask = await taskService.updateTaskStatus(taskId, newStatus);
      setTasks((prevTasks) =>
        prevTasks.map((task) => (task.id === taskId ? updatedTask : task)),
      );
    } catch (error) {
      alert(
        error instanceof Error
          ? "Error: " + error.message
          : "An unknown error occurred",
      );
    }
  };

  return (
    <div className="dashboard-page">
      <h1>Dashboard</h1>
      <p>Welcome to the dashboard!</p>

      <DragDropProvider onDragEnd={handleDragEnd}>
        <div className="board">
          {COLUMNS.map((column) => (
            <DroppableColumn
              key={column.status}
              status={column.status}
              label={column.label}
              tasks={tasks.filter((task) => task.status === column.status)}
              onAddClick={() => setShowCreateModal(true)}
            />
          ))}
        </div>
      </DragDropProvider>

      {showCreateModal && (
        <div className="modal">
          <div className="modal-content">
            <span className="close" onClick={() => setShowCreateModal(false)}>
              &times;
            </span>
            {showCreateModal && (
              <CreateTaskModal
                onTaskCreated={() => {
                  fetchTasks();
                  setShowCreateModal(false);
                }}
              />
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default DashboardPage;
