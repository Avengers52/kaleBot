import { useState } from 'react';

type ComposerProps = {
  onSend: (prompt: string) => void;
  isStreaming: boolean;
};

const Composer = ({ onSend, isStreaming }: ComposerProps) => {
  const [prompt, setPrompt] = useState('');

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    onSend(prompt);
    setPrompt('');
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
      <input
        value={prompt}
        onChange={(event) => setPrompt(event.target.value)}
        placeholder="Ask something..."
        style={{ flex: 1, padding: '0.75rem' }}
        disabled={isStreaming}
      />
      <button type="submit" disabled={isStreaming || !prompt.trim()}>
        {isStreaming ? 'Streaming...' : 'Send'}
      </button>
    </form>
  );
};

export default Composer;
