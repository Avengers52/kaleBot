import type { Conversation } from '../../types/chat';

type ConversationListProps = {
  conversations: Conversation[];
  activeConversationId: string | null;
  onSelectConversation: (id: string) => void;
  onDeleteConversation: (id: string) => void;
};

const formatTimestamp = (timestamp: number) =>
  new Date(timestamp).toLocaleDateString(undefined, {
    month: 'short',
    day: 'numeric'
  });

const ConversationList = ({
  conversations,
  activeConversationId,
  onSelectConversation,
  onDeleteConversation
}: ConversationListProps) => {
  return (
    <div className="conversation-list">
      <div className="conversation-list-header">
        <h2>Conversations</h2>
      </div>
      {conversations.length === 0 ? (
        <p className="empty-state">No conversations yet.</p>
      ) : (
        <ul>
          {conversations.map((conversation) => (
            <li key={conversation.id} className="conversation-row">
              <button
                type="button"
                className={`conversation-item ${
                  conversation.id === activeConversationId ? 'active' : ''
                }`}
                onClick={() => onSelectConversation(conversation.id)}
              >
                <span className="conversation-title">{conversation.title || 'Untitled'}</span>
                <span className="conversation-date">{formatTimestamp(conversation.updatedAt)}</span>
              </button>
              <button
                type="button"
                className="icon-button subtle"
                onClick={() => onDeleteConversation(conversation.id)}
                aria-label="Delete conversation"
              >
                ✕
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default ConversationList;
