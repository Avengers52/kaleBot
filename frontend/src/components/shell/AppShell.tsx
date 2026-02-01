import { useState } from 'react';
import { useChatStream } from '../../hooks/useChatStream';
import { useConversations } from '../../hooks/useConversations';
import type { ChatFinal, ChatMessage, Conversation } from '../../types/chat';
import ChatView from '../chat/ChatView';
import ConversationList from '../sidebar/ConversationList';
import SourcesPanel from '../sources/SourcesPanel';

const AppShell = () => {
  const {
    conversations,
    activeConversation,
    activeConversationId,
    setActiveConversationId,
    createConversation,
    upsertConversation,
    updateConversation,
    deleteConversation
  } = useConversations();
  const { startStream, stopStream, isStreaming, error } = useChatStream();
  const [sourcesOpen, setSourcesOpen] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [lastPrompt, setLastPrompt] = useState('');

  const handleNewChat = () => {
    createConversation();
  };

  const handleSend = async (prompt: string) => {
    const trimmed = prompt.trim();
    if (!trimmed) {
      return;
    }
    setLastPrompt(trimmed);

    let conversationId = activeConversationId;
    if (!conversationId) {
      const newConversationId = crypto.randomUUID();
      const newConversation: Conversation = {
        id: newConversationId,
        title: trimmed.slice(0, 50),
        messages: [],
        sources: [],
        scanSummary: null,
        updatedAt: Date.now()
      };
      setActiveConversationId(newConversationId);
      upsertConversation(newConversation);
      conversationId = newConversationId;
    }
    const assistantId = crypto.randomUUID();
    const userMessage: ChatMessage = {
      id: crypto.randomUUID(),
      role: 'user',
      content: trimmed,
      createdAt: Date.now()
    };
    const assistantMessage: ChatMessage = {
      id: assistantId,
      role: 'assistant',
      content: '',
      createdAt: Date.now()
    };

    updateConversation(conversationId, (conversation) => {
      const title =
        conversation.title === 'New chat'
          ? trimmed.slice(0, 50)
          : conversation.title;
      return {
        ...conversation,
        title,
        messages: [...conversation.messages, userMessage, assistantMessage],
        sources: [],
        scanSummary: null,
        updatedAt: Date.now()
      };
    });

    await startStream(trimmed, {
      onChunk: (chunk) => {
        updateConversation(conversationId, (conversation) => ({
          ...conversation,
          messages: conversation.messages.map((message) =>
            message.id === assistantId
              ? { ...message, content: message.content + chunk.content }
              : message
          ),
          updatedAt: Date.now()
        }));
      },
      onFinal: (finalPayload: ChatFinal) => {
        updateConversation(conversationId, (conversation) => ({
          ...conversation,
          sources: finalPayload.sources ?? [],
          scanSummary: finalPayload.scan ?? null,
          updatedAt: Date.now()
        }));
      },
      onError: () => {
        updateConversation(conversationId, (conversation) => ({
          ...conversation,
          messages: conversation.messages.map((message) =>
            message.id === assistantId
              ? { ...message, content: message.content || 'Error streaming response.' }
              : message
          ),
          updatedAt: Date.now()
        }));
      }
    });
  };

  const handleRetry = () => {
    if (!lastPrompt) {
      return;
    }
    void handleSend(lastPrompt);
  };

  const handleStop = () => {
    stopStream();
  };

  const activeSources = activeConversation?.sources ?? [];
  const activeScanSummary = activeConversation?.scanSummary ?? null;

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="header-left">
          <button
            className="icon-button"
            type="button"
            onClick={() => setSidebarOpen((open) => !open)}
            aria-label="Toggle sidebar"
          >
            ☰
          </button>
          <div className="header-title">
            <span className="logo">kaleBot</span>
            <span className="header-subtitle">Secure chat assistant</span>
          </div>
        </div>
        <div className="header-actions">
          <button type="button" className="button secondary" onClick={handleNewChat}>
            New chat
          </button>
          <button
            type="button"
            className="button ghost mobile-only"
            onClick={() => setSourcesOpen((open) => !open)}
          >
            Sources
          </button>
        </div>
      </header>
      <div className="app-body">
        <aside className={`sidebar ${sidebarOpen ? 'open' : 'collapsed'}`}>
          <ConversationList
            conversations={conversations}
            activeConversationId={activeConversationId}
            onSelectConversation={(id) => setActiveConversationId(id)}
            onDeleteConversation={deleteConversation}
          />
        </aside>
        <main className="chat-main">
          <ChatView
            conversation={activeConversation}
            isStreaming={isStreaming}
            error={error}
            onSend={handleSend}
            onStop={handleStop}
            onRetry={handleRetry}
          />
        </main>
        <aside className="sources-pane">
          <SourcesPanel
            sources={activeSources}
            scanSummary={activeScanSummary}
            isOpen={sourcesOpen}
            onClose={() => setSourcesOpen(false)}
          />
        </aside>
      </div>
    </div>
  );
};

export default AppShell;
