import { useEffect, useRef, useState } from 'react';
import type { ChatMessage } from '../../types/chat';
import MessageItem from './MessageItem';

type MessageListProps = {
  messages: ChatMessage[];
  isStreaming: boolean;
};

const MessageList = ({ messages, isStreaming }: MessageListProps) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const [isAtBottom, setIsAtBottom] = useState(true);

  const handleScroll = () => {
    const container = containerRef.current;
    if (!container) {
      return;
    }
    const threshold = 120;
    const atBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight < threshold;
    setIsAtBottom(atBottom);
  };

  useEffect(() => {
    const container = containerRef.current;
    if (!container || !isAtBottom) {
      return;
    }
    container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
  }, [messages, isAtBottom, isStreaming]);

  return (
    <div className="message-list" ref={containerRef} onScroll={handleScroll}>
      {messages.length === 0 ? (
        <div className="empty-chat">
          <h2>Start a new conversation</h2>
          <p>Ask kaleBot about vulnerabilities, dependencies, or best practices.</p>
        </div>
      ) : (
        messages.map((message) => <MessageItem key={message.id} message={message} />)
      )}
      {isStreaming ? (
        <div className="typing-indicator">
          <span />
          <span />
          <span />
        </div>
      ) : null}
    </div>
  );
};

export default MessageList;
