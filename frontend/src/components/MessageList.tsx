import type { ChatMessage } from '../types/chat';

type MessageListProps = {
  messages: ChatMessage[];
};

const MessageList = ({ messages }: MessageListProps) => {
  return (
    <div style={{ marginBottom: '1rem' }}>
      {messages.length === 0 ? (
        <p>No messages yet. Start a conversation!</p>
      ) : (
        messages.map((message) => (
          <div
            key={message.id}
            style={{
              padding: '0.75rem',
              marginBottom: '0.5rem',
              background: message.role === 'user' ? '#f0f4ff' : '#f7f7f7',
              borderRadius: '8px'
            }}
          >
            <strong style={{ textTransform: 'capitalize' }}>{message.role}</strong>
            <p style={{ marginTop: '0.5rem' }}>{message.content}</p>
          </div>
        ))
      )}
    </div>
  );
};

export default MessageList;
