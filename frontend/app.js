const { useState } = React;

function App() {
  const [count, setCount] = useState(0);

  return (
    <div className="container">
      <h1>⚛️ React Frontend (Import Trực Tiếp)</h1>
      <p className="subtitle">Không cần <code>node_modules</code>, không cần <code>npm</code>, siêu nhẹ cho máy!</p>

      <div className="card">
        <p>Số lần nhấn: <strong>{count}</strong></p>
        <button onClick={() => setCount(count + 1)}>
          Tăng số
        </button>
      </div>
    </div>
  );
}

// Render ứng dụng vào thẻ div#root
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<App />);
