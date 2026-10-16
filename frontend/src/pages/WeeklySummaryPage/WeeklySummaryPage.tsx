import { useEffect, useState } from "react";
import "./WeeklySummaryPage.css";
import { weeklySummaryService } from "../../api/services";
import type { WeeklySummary } from "../../types/types";

const formatDate = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString(undefined, { dateStyle: "medium" });
};

const formatDateTime = (value: string) => {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
};

function WeeklySummaryPage() {
  const [weeklySummary, setWeeklySummary] = useState<WeeklySummary | null>(
    null,
  );
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const getWeeklySummary = async () => {
      try {
        const data = await weeklySummaryService.getWeeklySummary();
        setWeeklySummary(data);
      } catch (error) {
        if (error instanceof Error) {
          alert("Error: " + error.message);
        } else {
          alert("An unknown error occurred");
        }
      } finally {
        setLoading(false);
      }
    };
    getWeeklySummary();
  }, []);

  return (
    <div className="weekly-summary-page">
      <h1>Weekly Summary</h1>
      <p>An overview of your progress over the last week.</p>

      {weeklySummary ? (
        <>
          <div className="weekly-summary-stats">
            <div className="weekly-summary-stat">
              <span className="stat-label">Period</span>
              <span className="stat-value stat-value-period">
                {formatDate(weeklySummary.periodStart)} –{" "}
                {formatDate(weeklySummary.periodEnd)}
              </span>
            </div>
            <div className="weekly-summary-stat">
              <span className="stat-label">Tasks Created</span>
              <span className="stat-value">{weeklySummary.tasksCreated}</span>
            </div>
            <div className="weekly-summary-stat">
              <span className="stat-label">Tasks Completed</span>
              <span className="stat-value">
                {weeklySummary.tasksCompleted}
              </span>
            </div>
          </div>

          <div className="weekly-summary-card">
            <div className="weekly-summary-card-header">
              <h2>Summary</h2>
              <span className="weekly-summary-meta">
                #{weeklySummary.id} · Generated{" "}
                {formatDateTime(weeklySummary.generatedAt)}
              </span>
            </div>
            <p className="weekly-summary-content">{weeklySummary.content}</p>
          </div>
        </>
      ) : (
        <div className="weekly-summary-card">
          <p className="weekly-summary-empty">
            {loading ? "Loading summary…" : "No weekly summaries yet."}
          </p>
        </div>
      )}
    </div>
  );
}

export default WeeklySummaryPage;
