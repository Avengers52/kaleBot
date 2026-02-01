export type ChatMessage = {
  id: string;
  role: 'user' | 'assistant';
  content: string;
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
