/// <reference types="vite/client" />

interface Window {
  ToluNative?: {
    syncTasks(tasksJson: string): void;
    consumeCompletions(): string;
  };
}
