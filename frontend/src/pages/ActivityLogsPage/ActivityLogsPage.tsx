import { useEffect, useState } from "react";
import "./ActivityLogsPage.css";
import { activityLogsService } from "../../api/services";
import type { ActivityLog, ChangeType } from "../../types/types";

const CHANGE_TYPE_LABELS: Record<ChangeType, string> = {
  TASK_CHANGE: "Task",
  STATUS_CHANGE: "Status",
  ASSIGNEE_CHANGE: "Assignee",
  LABEL_CHANGE: "Label",
};

const CHANGE_TYPE_CLASSES: Record<ChangeType, string> = {
  TASK_CHANGE: "task-change",
  STATUS_CHANGE: "status-change",
  ASSIGNEE_CHANGE: "assignee-change",
  LABEL_CHANGE: "label-change",
};

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function ActivityLogsPage() {
  const [activityLogs, setActivityLogs] = useState<ActivityLog[] | null>(null);

  useEffect(() => {
    const getLogs = async () => {
      try {
        const data = await activityLogsService.getAllActivityLogs();
        setActivityLogs(data);
      } catch (error) {
        if (error instanceof Error) {
          alert("Error: " + error.message);
        } else {
          alert("An unknown error occurred");
        }
      }
    };
    getLogs();
  }, []);

  return (
    <div className="activity-logs-page">
      <h1>Activity Logs</h1>
      <p>A history of changes made across your tasks.</p>

      <div className="activity-logs-table-wrapper">
        <table className="activity-logs-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Change Type</th>
              <th>Old Value</th>
              <th>New Value</th>
              <th>Created At</th>
              <th>Changed By</th>
            </tr>
          </thead>
          <tbody>
            {activityLogs?.map((log) => {
              return (
                <tr key={log.id}>
                  <td className="cell-id">{log.id}</td>
                  <td>
                    <span className={`change-type-badge ${CHANGE_TYPE_CLASSES[log.changeType]}`}>
                      {CHANGE_TYPE_LABELS[log.changeType]}
                    </span>
                  </td>
                  <td>{log.oldValue}</td>
                  <td>{log.newValue}</td>
                  <td className="cell-date">{formatDate(log.createdAt)}</td>
                  <td>{log.changedBy}</td>
                </tr>
              );
            })}
          </tbody>
        </table>

        {activityLogs?.length === 0 && (
          <p className="activity-logs-empty">No activity logs yet.</p>
        )}
      </div>
    </div>
  );
}

export default ActivityLogsPage;
