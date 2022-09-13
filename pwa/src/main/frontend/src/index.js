import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

Notification.requestPermission();
const root = ReactDOM.createRoot(document.getElementById('root'));

console.log("BALLS");
root.render(
  <App />
);

