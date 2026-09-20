import { useDraggable } from "@dnd-kit/react";
import type { Task } from "../../types/types";
import "./TaskCard.css";

function TaskCard({
  task,
  onClick,
}: {
  task: Task;
  onClick: (task: Task) => void;
}) {
  const { ref } = useDraggable({ id: task.id });

  return (
    <div ref={ref} className="task-card" onClick={() => onClick(task)}>
      <p className="task-card-title">{task.title}</p>
      <p className="task-card-description">{task.description}</p>
    </div>
  );
}

export default TaskCard;