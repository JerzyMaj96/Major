import { useEffect, useState } from "react";
import { activityLogsService } from "../../api/services";
import type { ActivityLog } from "../../types/types";

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
      {activityLogs && (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>CHANGE TYPE</th>
              <th>OLD VALUE</th>
              <th>NEW VALUE</th>
              <th>CREATED AT</th>
              <th>CHANGED BY</th>
            </tr>
          </thead>
          <tbody>
            {activityLogs.map((log) => {
              return (
                <tr key={log.id}>
                  <td>{log.id}</td>
                  <td>{log.changeType}</td>
                  <td>{log.oldValue}</td>
                  <td>{log.newValue}</td>
                  <td>{log.createdAt}</td>
                  <td>{log.changedBy}</td>
                </tr>
              );
            })}
          </tbody>
        </table>
      )}
    </div>
  );
}

export default ActivityLogsPage;
