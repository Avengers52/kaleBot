import { useCallback, useRef, useState } from 'react';
import { streamChat } from '../api/chat';
import type { ChatChunk, ChatFinal } from '../types/chat';

type StreamHandlers = {
  onChunk: (chunk: ChatChunk) => void;
  onFinal?: (finalPayload: ChatFinal) => void;
  onError?: (error: Error) => void;
};

export const useChatStream = () => {
  const abortControllerRef = useRef<AbortController | null>(null);
  const [isStreaming, setIsStreaming] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const startStream = useCallback(
    async (prompt: string, handlers: StreamHandlers) => {
      abortControllerRef.current?.abort();
      const controller = new AbortController();
      abortControllerRef.current = controller;
      setIsStreaming(true);
      setError(null);

      try {
        await streamChat(prompt, handlers.onChunk, handlers.onFinal, controller.signal);
      } catch (err) {
        if ((err as Error).name !== 'AbortError') {
          const errorInstance = err instanceof Error ? err : new Error('Streaming failed');
          setError(errorInstance.message);
          handlers.onError?.(errorInstance);
        }
      } finally {
        setIsStreaming(false);
      }
    },
    []
  );

  const stopStream = useCallback(() => {
    abortControllerRef.current?.abort();
    abortControllerRef.current = null;
    setIsStreaming(false);
  }, []);

  return {
    startStream,
    stopStream,
    isStreaming,
    error
  };
};
