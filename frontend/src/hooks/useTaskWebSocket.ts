import { useEffect } from "react";
import type { Task } from "../types/types";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { baseUrl } from "../api/api_helper";

export const useTaskWebSocket = (onTaskUpdate: (task: Task) => void) => {
  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${baseUrl}/ws`),
      onConnect: () => {
        client.subscribe("/topic/task-updates", (message: { body: string }) => {
          const updatedTask: Task = JSON.parse(message.body);
          onTaskUpdate(updatedTask);
        });
      },
    });

    client.activate();

    return () => {
      client.deactivate();
    };
  });
};
