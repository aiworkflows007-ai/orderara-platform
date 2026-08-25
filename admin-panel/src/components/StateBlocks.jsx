import React from 'react';
import { Loader2, WifiOff, Inbox } from 'lucide-react';

/** Shown while the first fetch is in flight. */
export function LoadingState({ label = 'Loading live data…' }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-slate-400">
      <Loader2 className="w-6 h-6 animate-spin mb-3" />
      <p className="text-sm font-medium">{label}</p>
    </div>
  );
}

/** Shown when the backend cannot be reached — never silently fall back to fake numbers. */
export function ErrorState({ message, onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center py-20 text-center">
      <div className="w-12 h-12 rounded-2xl bg-red-500/10 border border-red-500/20 flex items-center justify-center mb-4">
        <WifiOff className="w-6 h-6 text-red-400" />
      </div>
      <p className="text-white font-bold mb-1">Can't reach the OrderAra server</p>
      <p className="text-sm text-slate-400 max-w-md mb-4">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="px-4 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 border border-slate-700 text-sm font-semibold text-white"
        >
          Try again
        </button>
      )}
    </div>
  );
}

/** Shown when the server responded fine but there is genuinely nothing yet. */
export function EmptyState({ title, hint }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-12 h-12 rounded-2xl bg-slate-800 border border-slate-700 flex items-center justify-center mb-4">
        <Inbox className="w-6 h-6 text-slate-500" />
      </div>
      <p className="text-white font-bold mb-1">{title}</p>
      {hint && <p className="text-sm text-slate-400 max-w-md">{hint}</p>}
    </div>
  );
}
