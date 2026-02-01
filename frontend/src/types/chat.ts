export type ChatMessage = {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  createdAt?: number;
};

export type ChatChunk = {
  content: string;
};

export type Source = {
  title: string;
  url: string;
  kind: 'osv' | 'nvd' | 'advisory';
};

export type ChatFinal = {
  sources: Source[];
  scan?: {
    dependencyCount: number;
    findingCount: number;
  } | null;
};

export type Conversation = {
  id: string;
  title: string;
  messages: ChatMessage[];
  sources: Source[];
  scanSummary?: ChatFinal['scan'] | null;
  updatedAt: number;
};
