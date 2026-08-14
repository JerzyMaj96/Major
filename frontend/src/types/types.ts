export interface User {
  id: number;
  name: string;
  email: string;
  role: "USER";
  creationDate: string;
}

export interface UserSummary {
  id: number;
  name: string;
}

export interface UserRegister {
    name: string;
    password: string;
}

export type TaskStatus = "BACKLOG" | "IN_PROGRESS" | "IN_REVIEW" | "DONE";

export interface Task {
  id: number;
  title: string;
  description: string;
  status: TaskStatus;
  assignee: UserSummary | null;
  createdBy: UserSummary;
  labels: Label[];
  createdAt: string;
  updatedAt: string;
}

export interface Label {
  id: number;
  name: string;
  color: string;
}

export type ChangeType =
  | "TASK_CHANGE"
  | "STATUS_CHANGE"
  | "ASSIGNEE_CHANGE"
  | "LABEL_CHANGE";

export interface ActivityLog {
  id: number;
  changeType: ChangeType;
  oldValue: string;
  newValue: string;
  createdAt: string;
  changedBy: string;
}

export type EventType =
  | "PUSH"
  | "PULL_REQUEST_OPENED"
  | "PULL_REQUEST_MERGED"
  | "PULL_REQUEST_CLOSED"
  | "PULL_REQUEST_OTHER"
  | "PULL_REQUEST_UNKNOWN";

export type WebhookEventStatus = "PENDING" | "PROCESSED" | "FAILED";

export interface WebhookEvent {
  id: number;
  eventType: EventType;
  payload: string;
  receivedAt: string;
  status: WebhookEventStatus;
  errorMessage: string | null;
  taskId: number | null;
}

export interface WeeklySummary {
  id: number;
  content: string;
  periodStart: string;
  periodEnd: string;
  tasksCreated: number;
  tasksCompleted: number;
  generatedAt: string;
}
