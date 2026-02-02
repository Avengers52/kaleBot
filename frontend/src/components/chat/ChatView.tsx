import { useMemo } from 'react';
import type { ChatMessage, Conversation } from '../../types/chat';
import Composer from './Composer';
import MessageList from './MessageList';

type ChatViewProps = {
  conversation: Conversation | null;
  isStreaming: boolean;
  error: string | null;
  onSend: (prompt: string) => void;
  onStop: () => void;
  onRetry: () => void;
};

const ChatView = ({ conversation, isStreaming, error, onSend, onStop, onRetry }: ChatViewProps) => {
  const messages = useMemo<ChatMessage[]>(() => conversation?.messages ?? [], [conversation]);

  return (
    <div className="chat-view">
      {error ? (
        <div className="error-banner">
          <div>
            <strong>Request failed.</strong>
            <span>{error}</span>
          </div>
          <button type="button" className="button secondary" onClick={onRetry}>
            Retry
          </button>
        </div>
      ) : null}
      <MessageList messages={messages} isStreaming={isStreaming} />
      <Composer onSend={onSend} isStreaming={isStreaming} onStop={onStop} />
    </div>
  );
};

export default ChatView;
