import { gptService, taskService } from "../../api/services";
import type { CreateTask } from "../../types/types";
import { useFormState } from "../../hooks/useFormState";
import "./CreateTaskModal.css";

interface CreateTaskModalProps {
  onTaskCreated: () => void;
}

function CreateTaskModal({ onTaskCreated }: CreateTaskModalProps) {
  const { values, handleChange } = useFormState<CreateTask>({
    title: "",
    description: "",
    assigneeId: undefined,
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
      <form className="create-task-form" onSubmit={handleCreateTask}>
        <div className="create-task-field">
          <label className="create-task-label" htmlFor="title">
            Title
          </label>
          <input
            id="title"
            type="text"
            name="title"
            placeholder="e.g. Design the landing page"
            value={values.title}
            onChange={handleChange}
            className="create-task-title-input"
            autoFocus
          />
        </div>

        <div className="create-task-field">
          <label className="create-task-label" htmlFor="description">
            Description
          </label>
          <textarea
            id="description"
            name="description"
            placeholder="Add a more detailed description..."
            value={values.description}
            onChange={handleChange}
            className="create-task-description-input"
          ></textarea>
        </div>

        <div className="create-task-field">
          <label className="create-task-label" htmlFor="assigneeId">
            Assignee ID
          </label>
          <input
            id="assigneeId"
            type="number"
            name="assigneeId"
            placeholder="Enter user ID"
            value={values.assigneeId}
            onChange={handleChange}
            className="create-task-assignee-input"
          />
        </div>

        <button
          type="button"
          className="generate-description-btn"
          onClick={async () => {
            const generatedDescription = await gptService.generateDescription(
              values.title,
            );
            handleChange({
              target: {
                name: "description",
                value: generatedDescription,
                type: "text",
              },
            } as React.ChangeEvent<HTMLTextAreaElement>);
          }}
        >
          Generate Description
        </button>

        <div className="create-task-actions">
          <button type="submit" className="create-task-submit-btn">
            Create Task
          </button>
        </div>
      </form>
    </div>
  );
}

export default CreateTaskModal;
