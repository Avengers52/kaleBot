import { useState } from 'react';

type ComposerProps = {
  onSend: (prompt: string) => void;
  onStop: () => void;
  isStreaming: boolean;
};

const Composer = ({ onSend, onStop, isStreaming }: ComposerProps) => {
  const [prompt, setPrompt] = useState('');

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!prompt.trim()) {
      return;
    }
    onSend(prompt);
    setPrompt('');
  };

  return (
    <form onSubmit={handleSubmit} className="composer">
      <textarea
        value={prompt}
        onChange={(event) => setPrompt(event.target.value)}
        placeholder="Message kaleBot..."
        rows={1}
        onKeyDown={(event) => {
          if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault();
            handleSubmit(event);
          }
        }}
      />
      <div className="composer-actions">
        {isStreaming ? (
          <button type="button" className="button secondary" onClick={onStop}>
            Stop
          </button>
        ) : null}
        <button type="submit" className="button primary" disabled={isStreaming || !prompt.trim()}>
          Send
        </button>
      </div>
    </form>
  );
};

export default Composer;
