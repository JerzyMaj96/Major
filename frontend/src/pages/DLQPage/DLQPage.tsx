import { useEffect, useState } from "react";
import ReplayIcon from "@mui/icons-material/Replay";
import "./DLQPage.css";
import { webhookEventService } from "../../api/services";
import type { WebhookEvent } from "../../types/types";
import { formatDateTime } from "../../utils";

const showError = (error: unknown) => {
  alert(error instanceof Error ? "Error: " + error.message : "An unknown error occurred");
};

const loadFailedEvents = () =>
  webhookEventService.getWebhookEventsByStatus("FAILED");

function DLQPage() {
  const [webhookEvents, setWebhookEvents] = useState<WebhookEvent[] | null>(
    null,
  );
  const [retryingId, setRetryingId] = useState<number | null>(null);

  useEffect(() => {
    loadFailedEvents().then(setWebhookEvents).catch(showError);
  }, []);

  const handleRetry = async (eventId: number) => {
    setRetryingId(eventId);
    try {
      await webhookEventService.retryHandleGitHubWebhook(eventId);
      setWebhookEvents(await loadFailedEvents());
    } catch (error) {
      showError(error);
    } finally {
      setRetryingId(null);
    }
  };

  return (
    <div className="dlq-page">
      <h1>Failed Webhooks</h1>
      <p>
        Webhook events that failed to process. Retry them after fixing the
        cause.
      </p>

      {webhookEvents === null ? (
        <p>Loading...</p>
      ) : webhookEvents.length === 0 ? (
        <p>No failed webhook events.</p>
      ) : (
        <div className="dlq-list">
          {webhookEvents.map((event) => (
            <div key={event.id} className="dlq-card">
              <div className="dlq-card-header">
                <h2>{event.eventType}</h2>
                <span className="dlq-meta">
                  #{event.id} · Received {formatDateTime(event.receivedAt)}
                  {event.taskId !== null && ` · Task #${event.taskId}`}
                </span>
              </div>
              <p className="dlq-error">
                {event.errorMessage ?? "No error message"}
              </p>
              <div className="dlq-actions">
                <button
                  className="dlq-retry-button"
                  onClick={() => handleRetry(event.id)}
                  disabled={retryingId === event.id}
                >
                  {retryingId === event.id ? (
                    <span className="dlq-spinner" aria-hidden="true" />
                  ) : (
                    <ReplayIcon fontSize="small" />
                  )}
                  {retryingId === event.id ? "Retrying..." : "Retry"}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default DLQPage;
