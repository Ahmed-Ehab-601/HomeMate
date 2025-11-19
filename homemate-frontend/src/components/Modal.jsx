function Modal({ title, children, actions, onClose, width = 500, icon }) {
  return (
    <div className="modal-overlay" role="dialog" aria-modal="true">
      <div className="modal-window" style={{ maxWidth: `${width}px` }}>
        <button type="button" className="modal-close" aria-label="Close" onClick={onClose}>
          ×
        </button>
        <div className="modal-header">
          {icon && <span className="modal-icon">{icon}</span>}
          <h3>{title}</h3>
        </div>
        <div className="modal-body">{children}</div>
        {actions && <div className="modal-actions">{actions}</div>}
      </div>
    </div>
  );
}

export default Modal;

