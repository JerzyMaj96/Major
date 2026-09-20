import { useDraggable } from "@dnd-kit/react";
import type { Task } from "../../types/types";
import "./TaskCard.css";

function TaskCard({ task }: { task: Task }) {
  const { ref } = useDraggable({ id: task.id });

  return (
    <div ref={ref} className="task-card">
      <p className="task-card-title">{task.title}</p>
      <p className="task-card-description">{task.description}</p>
    </div>
  );
}

export default TaskCard;