import React from 'react';
import type { ChatMessage } from '../../types/chat';

type MessageItemProps = {
  message: ChatMessage;
};

type InlineNode = string | React.ReactElement;

type CodeBlockProps = {
  language?: string;
  code: string;
};

const isSafeHref = (href: string) => {
  try {
    const url = new URL(href, window.location.origin);
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch {
    return false;
  }
};

const CodeBlock = ({ language, code }: CodeBlockProps) => {
  const [copied, setCopied] = React.useState(false);

  return (
    <div className="code-block">
      <div className="code-block-header">
        <span>{language || 'code'}</span>
        <button
          type="button"
          className="button ghost small"
          onClick={async () => {
            await navigator.clipboard.writeText(code);
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
          }}
        >
          {copied ? 'Copied' : 'Copy'}
        </button>
      </div>
      <pre>
        <code className={language ? `language-${language}` : undefined}>{code}</code>
      </pre>
    </div>
  );
};

const renderInline = (text: string) => {
  const nodes: InlineNode[] = [];
  let remaining = text;
  let key = 0;
  const pattern =
    /(`[^`]+`)|(\*\*[^*]+\*\*)|(__(?:[^_]+)__)|(\*[^*]+\*)|(_[^_]+_)|(\[[^\]]+\]\([^)]+\))/;

  while (remaining) {
    const match = pattern.exec(remaining);
    if (!match) {
      nodes.push(remaining);
      break;
    }

    if (match.index > 0) {
      nodes.push(remaining.slice(0, match.index));
    }

    const token = match[0];
    if (token.startsWith('`')) {
      nodes.push(
        <code className="inline-code" key={`code-${key++}`}>
          {token.slice(1, -1)}
        </code>
      );
    } else if (token.startsWith('**') || token.startsWith('__')) {
      nodes.push(
        <strong key={`bold-${key++}`}>{token.slice(2, -2)}</strong>
      );
    } else if (token.startsWith('*') || token.startsWith('_')) {
      nodes.push(
        <em key={`em-${key++}`}>{token.slice(1, -1)}</em>
      );
    } else if (token.startsWith('[')) {
      const linkMatch = /\[([^\]]+)\]\(([^)]+)\)/.exec(token);
      if (linkMatch) {
        const href = linkMatch[2];
        if (!isSafeHref(href)) {
          nodes.push(`${linkMatch[1]} (${href})`);
        } else {
        nodes.push(
          <a href={href} target="_blank" rel="noreferrer noopener" key={`link-${key++}`}>
            {linkMatch[1]}
          </a>
        );
        }
      } else {
        nodes.push(token);
      }
    } else {
      nodes.push(token);
    }

    remaining = remaining.slice(match.index + token.length);
  }

  return nodes;
};

const renderParagraph = (text: string, key: number) => (
  <p key={`p-${key}`}>{renderInline(text)}</p>
);

const renderMarkdownBlocks = (content: string) => {
  const parts = content.split('```');
  const blocks: React.ReactNode[] = [];
  let blockKey = 0;

  parts.forEach((part, index) => {
    if (index % 2 === 1) {
      const [firstLine, ...rest] = part.split('\n');
      const language = rest.length > 0 ? firstLine.trim() : '';
      const code = rest.length > 0 ? rest.join('\n') : firstLine;
      blocks.push(
        <CodeBlock key={`codeblock-${blockKey++}`} language={language || undefined} code={code.trimEnd()} />
      );
      return;
    }

    const lines = part.split('\n');
    let paragraphLines: string[] = [];
    let listItems: string[] = [];
    let listType: 'ul' | 'ol' | null = null;

    const flushParagraph = () => {
      if (paragraphLines.length > 0) {
        blocks.push(renderParagraph(paragraphLines.join(' '), blockKey++));
        paragraphLines = [];
      }
    };

    const flushList = () => {
      if (listItems.length > 0 && listType) {
        const listContent = listItems.map((item, itemIndex) => (
          <li key={`li-${blockKey}-${itemIndex}`}>{renderInline(item)}</li>
        ));
        blocks.push(
          listType === 'ol' ? (
            <ol key={`ol-${blockKey++}`}>{listContent}</ol>
          ) : (
            <ul key={`ul-${blockKey++}`}>{listContent}</ul>
          )
        );
        listItems = [];
        listType = null;
      }
    };

    lines.forEach((line) => {
      const trimmed = line.trim();
      if (!trimmed) {
        flushParagraph();
        flushList();
        return;
      }

      const orderedMatch = /^(\d+)\.\s+(.*)/.exec(trimmed);
      const unorderedMatch = /^[-*]\s+(.*)/.exec(trimmed);

      if (orderedMatch) {
        flushParagraph();
        if (listType && listType !== 'ol') {
          flushList();
        }
        listType = 'ol';
        listItems.push(orderedMatch[2]);
        return;
      }

      if (unorderedMatch) {
        flushParagraph();
        if (listType && listType !== 'ul') {
          flushList();
        }
        listType = 'ul';
        listItems.push(unorderedMatch[1]);
        return;
      }

      flushList();
      paragraphLines.push(trimmed);
    });

    flushParagraph();
    flushList();
  });

  return blocks;
};

const MessageItem = ({ message }: MessageItemProps) => {
  const isUser = message.role === 'user';

  return (
    <article className={`message-item ${isUser ? 'user' : 'assistant'}`}>
      <div className="message-role">{isUser ? 'You' : 'kaleBot'}</div>
      <div className="message-content">
        {renderMarkdownBlocks(message.content)}
      </div>
    </article>
  );
};

export default MessageItem;
