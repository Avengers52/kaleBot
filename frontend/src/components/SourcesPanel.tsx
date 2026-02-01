import type { Source } from '../types/chat';

type SourcesPanelProps = {
  sources: Source[];
};

const SourcesPanel = ({ sources }: SourcesPanelProps) => {
  return (
    <aside
      style={{
        padding: '1rem',
        border: '1px solid #e0e0e0',
        borderRadius: '8px',
        background: '#fafafa'
      }}
    >
      <h2>Sources</h2>
      {sources.length === 0 ? (
        <p>No sources available yet.</p>
      ) : (
        <ul style={{ paddingLeft: '1.25rem' }}>
          {sources.map((source) => (
            <li key={source.url}>
              <a href={source.url} target="_blank" rel="noreferrer">
                {source.title || source.url}
              </a>
              <span style={{ marginLeft: '0.5rem', color: '#666', fontSize: '0.85rem' }}>
                ({source.kind})
              </span>
            </li>
          ))}
        </ul>
      )}
    </aside>
  );
};

export default SourcesPanel;
