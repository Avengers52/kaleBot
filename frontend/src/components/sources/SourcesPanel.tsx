import type { Source } from '../../types/chat';

type SourcesPanelProps = {
  sources: Source[];
  scanSummary: {
    dependencyCount: number;
    findingCount: number;
  } | null;
  isOpen: boolean;
  onClose: () => void;
};

const SourcesPanel = ({ sources, scanSummary, isOpen, onClose }: SourcesPanelProps) => {
  return (
    <div className={`sources-panel ${isOpen ? 'open' : ''}`}>
      <div className="sources-header">
        <h2>Sources</h2>
        <button type="button" className="icon-button mobile-only" onClick={onClose}>
          ✕
        </button>
      </div>
      {scanSummary ? (
        <div className="scan-summary">
          <h3>Scan summary</h3>
          <div className="summary-grid">
            <div>
              <span className="summary-label">Dependencies</span>
              <span className="summary-value">{scanSummary.dependencyCount}</span>
            </div>
            <div>
              <span className="summary-label">Findings</span>
              <span className="summary-value">{scanSummary.findingCount}</span>
            </div>
          </div>
        </div>
      ) : null}
      {sources.length === 0 ? (
        <p className="empty-state">No sources yet. Sources will appear once the scan completes.</p>
      ) : (
        <ul className="sources-list">
          {sources.map((source) => (
            <li key={`${source.url}-${source.kind}`}>
              <a href={source.url} target="_blank" rel="noreferrer noopener">
                {source.title || source.url}
              </a>
              <span className="source-kind">{source.kind}</span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default SourcesPanel;
