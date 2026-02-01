import { useState } from 'react';
import { streamChat } from '../api/chat';
import type { ChatMessage } from '../types/chat';
import Composer from './Composer';
import MessageList from './MessageList';
import SourcesPanel from './SourcesPanel';

const ChatPage = () => {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isStreaming, setIsStreaming] = useState(false);

  const handleSend = async (prompt: string) => {
    if (!prompt.trim()) {
      return;
    }

    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: prompt
    };

    const assistantId = crypto.randomUUID();
    const assistantMessage: ChatMessage = {
      id: assistantId,
      role: 'assistant',
      content: ''
    };

    setMessages((prev) => [...prev, userMessage, assistantMessage]);
    setIsStreaming(true);

    try {
      await streamChat(prompt, (chunk) => {
        setMessages((prev) =>
          prev.map((message) =>
            message.id === assistantId
              ? { ...message, content: message.content + chunk.content }
              : message
          )
        );
      });
    } catch (error) {
      setMessages((prev) =>
        prev.map((message) =>
          message.id === assistantId
            ? { ...message, content: 'Error streaming response.' }
            : message
        )
      );
    } finally {
      setIsStreaming(false);
    }
  };

  return (
    <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '2rem' }}>
      <section>
        <MessageList messages={messages} />
        <Composer onSend={handleSend} isStreaming={isStreaming} />
      </section>
      <SourcesPanel />
    </div>
  );
};

export default ChatPage;
