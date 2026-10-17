import { useEffect, useState } from "react";
import "./WeeklySummaryPage.css";
import { weeklySummaryService } from "../../api/services";
import type { WeeklySummary } from "../../types/types";
import { formatDate, formatDateTime } from "../../utils";

function WeeklySummaryPage() {
  const [weeklySummary, setWeeklySummary] = useState<WeeklySummary | null>(
    null,
  );
  const [allSummaries, setAllSummaries] = useState<WeeklySummary[] | null>(
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

  const fetchAllSummaries = async () => {
    try {
      const data = await weeklySummaryService.getAllWeeklySummaries();
      setAllSummaries(data);
    } catch (error) {
      if (error instanceof Error) {
        alert("Error: " + error.message);
      } else {
        alert("An unknown error occurred");
      }
    }
  };

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
              <span className="stat-value">{weeklySummary.tasksCompleted}</span>
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

      <section className="weekly-summary-history">
        <button
          className="weekly-summary-history-button"
          onClick={fetchAllSummaries}
        >
          {allSummaries ? "Refresh all summaries" : "Show all summaries"}
        </button>

        {allSummaries?.length === 0 && (
          <p className="weekly-summary-empty">No weekly summaries available.</p>
        )}

        {allSummaries?.map((summary) => (
          <div key={summary.id} className="weekly-summary-card">
            <div className="weekly-summary-card-header">
              <h2>
                {formatDate(summary.periodStart)} –{" "}
                {formatDate(summary.periodEnd)}
              </h2>
              <span className="weekly-summary-meta">
                #{summary.id} · Generated {formatDateTime(summary.generatedAt)}
              </span>
            </div>
            <p className="weekly-summary-content">{summary.content}</p>
          </div>
        ))}
      </section>
    </div>
  );
}

export default WeeklySummaryPage;
