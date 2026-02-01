import type { ChatChunk, ChatFinal } from '../types/chat';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const streamChat = async (
  prompt: string,
  onChunk: (chunk: ChatChunk) => void,
  onFinal?: (finalPayload: ChatFinal) => void
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
  let eventName = '';
  let dataLines: string[] = [];

  const flushEvent = () => {
    if (!dataLines.length) {
      eventName = '';
      return;
    }
    const data = dataLines.join('\n');
    if (eventName === 'delta') {
      onChunk(JSON.parse(data));
    } else if (eventName === 'final') {
      onFinal?.(JSON.parse(data));
    }
    eventName = '';
    dataLines = [];
  };

  while (true) {
    const { value, done } = await reader.read();
    if (done) {
      flushEvent();
      break;
    }
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() ?? '';

    for (const line of lines) {
      if (line.startsWith('event:')) {
        eventName = line.replace(/^event:\s?/, '').trim();
      } else if (line.startsWith('data:')) {
        dataLines.push(line.replace(/^data:\s?/, ''));
      } else if (line.trim() === '') {
        flushEvent();
      }
    }
  }
};
