import { useDroppable } from "@dnd-kit/react";
import type { Task, TaskStatus } from "../../types/types";
import TaskCard from "../TaskCard/TaskCard";
import ControlPointIcon from "@mui/icons-material/ControlPoint";
import "./DroppableColumn.css";

function DroppableColumn({
  status,
  label,
  tasks,
  onAddClick,
  onClick,
}: {
  status: TaskStatus;
  label: string;
  tasks: Task[];
  onAddClick: () => void;
  onClick: () => void;
}) {
  const { ref } = useDroppable({ id: status });

  return (
    <div className="board-column">
      <div className="board-column-header">
        <span>{label}</span>
        <span className="board-column-count">{tasks.length}</span>
      </div>

      <div ref={ref} className="board-column-body">
        {tasks.map((task) => (
          <TaskCard key={task.id} task={task} onClick={onClick} />
        ))}

        {status === "BACKLOG" && (
          <ControlPointIcon className="add-task-icon" onClick={onAddClick} />
        )}
      </div>
    </div>
  );
}

export default DroppableColumn;
