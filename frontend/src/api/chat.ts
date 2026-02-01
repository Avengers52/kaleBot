import type { ChatChunk } from '../types/chat';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const streamChat = async (
  prompt: string,
  onChunk: (chunk: ChatChunk) => void
): Promise<void> => {
  const response = await fetch(`${API_BASE_URL}/api/chat/stream`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ message: prompt })
  });

  if (!response.ok || !response.body) {
    throw new Error(`Failed to stream chat: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() ?? '';

    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.replace(/^data:\s?/, '');
        if (data) {
          onChunk(JSON.parse(data));
        }
      }
    }
  }
};
