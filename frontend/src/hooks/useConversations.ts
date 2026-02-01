import { useCallback, useEffect, useMemo, useState } from 'react';
import type { Conversation } from '../types/chat';

const STORAGE_KEY = 'kaleBot.conversations';

const createEmptyConversation = (): Conversation => ({
  id: crypto.randomUUID(),
  title: 'New chat',
  messages: [],
  sources: [],
  scanSummary: null,
  updatedAt: Date.now()
});

export const useConversations = () => {
  const [conversations, setConversations] = useState<Conversation[]>([]);
  const [activeConversationId, setActiveConversationId] = useState<string | null>(null);
  const [isHydrated, setIsHydrated] = useState(false);

  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored) as Conversation[];
        if (parsed.length) {
          setConversations(parsed);
          setActiveConversationId(parsed[0].id);
        }
      } catch {
        setConversations([]);
      }
    }
    setIsHydrated(true);
  }, []);

  useEffect(() => {
    if (!isHydrated) {
      return;
    }
    localStorage.setItem(STORAGE_KEY, JSON.stringify(conversations));
  }, [conversations, isHydrated]);

  const createConversation = useCallback(() => {
    const newConversation = createEmptyConversation();
    setConversations((prev) => [newConversation, ...prev]);
    setActiveConversationId(newConversation.id);
    return newConversation.id;
  }, []);

  const upsertConversation = useCallback((nextConversation: Conversation) => {
    setConversations((prev) => {
      const exists = prev.some((conversation) => conversation.id === nextConversation.id);
      if (!exists) {
        return [nextConversation, ...prev].sort((a, b) => b.updatedAt - a.updatedAt);
      }
      const updated = prev.map((conversation) =>
        conversation.id === nextConversation.id ? nextConversation : conversation
      );
      return updated.sort((a, b) => b.updatedAt - a.updatedAt);
    });
  }, []);

  const updateConversation = useCallback((id: string, updater: (conversation: Conversation) => Conversation) => {
    setConversations((prev) => {
      const updated = prev.map((conversation) =>
        conversation.id === id ? updater(conversation) : conversation
      );
      return [...updated].sort((a, b) => b.updatedAt - a.updatedAt);
    });
  }, []);

  const deleteConversation = useCallback((id: string) => {
    setConversations((prev) => prev.filter((conversation) => conversation.id !== id));
    setActiveConversationId((prev) => (prev === id ? null : prev));
  }, []);

  const activeConversation = useMemo(
    () => conversations.find((conversation) => conversation.id === activeConversationId) ?? null,
    [conversations, activeConversationId]
  );

  return {
    conversations,
    activeConversation,
    activeConversationId,
    setActiveConversationId,
    createConversation,
    upsertConversation,
    updateConversation,
    deleteConversation
  };
};
