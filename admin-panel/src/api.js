import { io } from 'socket.io-client';
import { useEffect, useState } from 'react';

/**
 * When the admin panel is served by the backend itself (production), the API
 * lives on the same origin. In local development Vite serves the panel on a
 * different port, so point at the backend directly.
 */
export const API_BASE =
  import.meta.env.VITE_API_BASE ||
  (import.meta.env.DEV ? 'http://localhost:8080' : '');

async function request(path, options = {}) {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
    body: options.body ? JSON.stringify(options.body) : undefined
  });
  const json = await res.json().catch(() => null);
  if (!res.ok || !json?.success) {
    throw new Error(json?.message || `Request failed (${res.status})`);
  }
  return json.data;
}

export const api = {
  // Every restaurant, including suspended ones the customer app cannot see.
  restaurants: () => request('/api/admin/restaurants'),
  stats: () => request('/api/admin/stats'),
  orders: () => request('/api/orders'),
  setSubscription: (restaurantId, status, reason) =>
    request(`/api/admin/restaurants/${restaurantId}/subscription`, {
      method: 'PATCH',
      body: { status, reason }
    })
};

// ---------------------------------------------------------------------------
// Live connection — the admin room receives every order and billing change
// ---------------------------------------------------------------------------

let socket = null;

export function getSocket() {
  if (!socket) {
    socket = io(API_BASE || window.location.origin, { transports: ['websocket'] });
    socket.on('connect', () => socket.emit('join', { role: 'admin' }));
  }
  return socket;
}

/** Subscribes to one server event for the life of a component. */
export function useLiveEvent(event, handler) {
  useEffect(() => {
    const s = getSocket();
    s.on(event, handler);
    return () => s.off(event, handler);
  });
}

/**
 * Loads data from the backend, refreshes it on an interval, and re-fetches
 * whenever one of `events` fires — so the panel mirrors what the phones show.
 */
export function useLiveData(loader, events = [], intervalMs = 5000) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);

  const reload = () => setReloadKey(k => k + 1);

  useEffect(() => {
    let cancelled = false;

    const load = async () => {
      try {
        const result = await loader();
        if (!cancelled) {
          setData(result);
          setError(null);
        }
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    const timer = setInterval(load, intervalMs);

    const s = getSocket();
    events.forEach(evt => s.on(evt, load));

    return () => {
      cancelled = true;
      clearInterval(timer);
      events.forEach(evt => s.off(evt, load));
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reloadKey]);

  return { data, error, loading, reload };
}

// ---------------------------------------------------------------------------
// Shared display helpers
// ---------------------------------------------------------------------------

export const SUBSCRIPTION_LABELS = {
  ACTIVE_PAID: 'Paid',
  ACTIVE_TRIAL: 'Free trial',
  OVERDUE: 'Overdue',
  SUSPENDED: 'Suspended'
};

export const SUBSCRIPTION_STYLES = {
  ACTIVE_PAID: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  ACTIVE_TRIAL: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  OVERDUE: 'bg-orange-500/10 text-orange-400 border-orange-500/20',
  SUSPENDED: 'bg-red-500/10 text-red-400 border-red-500/20'
};

export const ORDER_STATUS_STYLES = {
  PLACED: 'bg-amber-500/10 text-amber-400 border-amber-500/20',
  ACCEPTED: 'bg-blue-500/10 text-blue-400 border-blue-500/20',
  PREPARING: 'bg-orange-500/10 text-orange-400 border-orange-500/20',
  OUT_FOR_DELIVERY: 'bg-indigo-500/10 text-indigo-400 border-indigo-500/20',
  DELIVERED: 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20',
  REJECTED: 'bg-red-500/10 text-red-400 border-red-500/20',
  CANCELLED: 'bg-red-500/10 text-red-400 border-red-500/20'
};

export const money = n => `₹${Number(n || 0).toLocaleString('en-IN')}`;

export const formatDate = iso => {
  if (!iso) return '—';
  const d = new Date(iso);
  return Number.isNaN(d.getTime())
    ? iso
    : d.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
};

export const formatTime = iso => {
  if (!iso) return '—';
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' });
};

export const statusLabel = s => (s || '').replace(/_/g, ' ');
