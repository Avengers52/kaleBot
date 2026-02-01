import React from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import type { ChatMessage } from '../../types/chat';

type MessageItemProps = {
  message: ChatMessage;
};

type CodeRendererProps = React.ComponentPropsWithoutRef<'code'> & {
  inline?: boolean;
};

const CodeBlock = ({ className, children, ...props }: CodeRendererProps) => {
  const match = /language-(\w+)/.exec(className ?? '');
  const [copied, setCopied] = React.useState(false);
  const codeContent = String(children).replace(/\n$/, '');

  if (match) {
    return (
      <div className="code-block">
        <div className="code-block-header">
          <span>{match[1]}</span>
          <button
            type="button"
            className="button ghost small"
            onClick={async () => {
              await navigator.clipboard.writeText(codeContent);
              setCopied(true);
              setTimeout(() => setCopied(false), 1500);
            }}
          >
            {copied ? 'Copied' : 'Copy'}
          </button>
        </div>
        <pre>
          <code className={className} {...props}>
            {codeContent}
          </code>
        </pre>
      </div>
    );
  }

  return (
    <code className="inline-code" {...props}>
      {children}
    </code>
  );
};

const MessageItem = ({ message }: MessageItemProps) => {
  const isUser = message.role === 'user';

  return (
    <article className={`message-item ${isUser ? 'user' : 'assistant'}`}>
      <div className="message-role">{isUser ? 'You' : 'kaleBot'}</div>
      <div className="message-content">
        <ReactMarkdown
          remarkPlugins={[remarkGfm]}
          components={{
            code: CodeBlock
          }}
        >
          {message.content}
        </ReactMarkdown>
      </div>
    </article>
  );
};

export default MessageItem;
