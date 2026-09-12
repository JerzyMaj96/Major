import { taskService } from "../../api/services";
import type { CreateTask } from "../../types/types";
import { useFormState } from "../../hooks/useFormState";

interface CreateTaskModalProps {
  onTaskCreated: () => void;
}

function CreateTaskModal({ onTaskCreated }: CreateTaskModalProps) {
  const { values, handleChange } = useFormState<CreateTask>({
    title: "",
    description: "",
    assigneeId: undefined,
    generateDescription: false,
  });

  const handleCreateTask = async (
    event: React.SubmitEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();

    try {
      const data = await taskService.createTask(values);
      alert("Task created successfully! Task ID: " + data.id);
      onTaskCreated();
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

  return (
    <div className="create-task-modal">
      <h2>Create Task</h2>
      <form onSubmit={handleCreateTask}>
        <input
          type="text"
          placeholder="Task Title"
          value={values.title}
          onChange={handleChange}
        />
        <textarea
          placeholder="Task Description"
          value={values.description}
          onChange={handleChange}
        ></textarea>
        <input
          type="checkbox"
          name="generateDescription"
          checked={values.generateDescription}
          onChange={handleChange}
        />
        <input
          type="number"
          name="assigneeId"
          placeholder="Assignee ID"
          value={values.assigneeId}
          onChange={handleChange}
        />
        <label htmlFor="generateDescription">Generate Description</label>
        <button type="submit">Create Task</button>
      </form>
    </div>
  );
}

export default CreateTaskModal;
