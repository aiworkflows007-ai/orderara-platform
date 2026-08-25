import React, { useState } from 'react';
import {
  Store, MapPin, Phone, Mail, Star, Search, Utensils, Wallet, DoorOpen, DoorClosed
} from 'lucide-react';
import { api, useLiveData, formatDate, SUBSCRIPTION_LABELS, SUBSCRIPTION_STYLES } from '../api';
import { LoadingState, ErrorState, EmptyState } from '../components/StateBlocks';

const REFRESH_EVENTS = [
  'restaurant:new',
  'restaurants:updated',
  'admin:restaurants:updated',
  'subscription:updated',
  'restaurant:settings'
];

const FILTERS = [
  { id: 'ALL', label: 'All' },
  { id: 'ACTIVE_PAID', label: 'Paid' },
  { id: 'ACTIVE_TRIAL', label: 'On trial' },
  { id: 'OVERDUE', label: 'Overdue' },
  { id: 'SUSPENDED', label: 'Suspended' }
];

export default function RestaurantsManagement() {
  const { data, error, loading, reload } = useLiveData(api.restaurants, REFRESH_EVENTS);
  const [filter, setFilter] = useState('ALL');
  const [query, setQuery] = useState('');

  if (loading) return <LoadingState label="Loading restaurant directory…" />;
  if (error) return <ErrorState message={error} onRetry={reload} />;

  const restaurants = data || [];
  const visible = restaurants.filter(r => {
    const matchesFilter = filter === 'ALL' || r.subscriptionStatus === filter;
    const q = query.trim().toLowerCase();
    const matchesQuery =
      !q ||
      r.name?.toLowerCase().includes(q) ||
      r.ownerName?.toLowerCase().includes(q) ||
      r.address?.toLowerCase().includes(q);
    return matchesFilter && matchesQuery;
  });

  return (
    <div className="space-y-6">
      <div className="flex flex-col lg:flex-row gap-3 lg:items-center lg:justify-between">
        <div className="flex flex-wrap gap-2">
          {FILTERS.map(f => {
            const count = f.id === 'ALL'
              ? restaurants.length
              : restaurants.filter(r => r.subscriptionStatus === f.id).length;
            return (
              <button
                key={f.id}
                onClick={() => setFilter(f.id)}
                className={`px-3.5 py-2 rounded-xl text-xs font-bold border transition-colors ${
                  filter === f.id
                    ? 'bg-orange-500/15 text-orange-400 border-orange-500/30'
                    : 'bg-slate-900/70 text-slate-400 border-slate-800 hover:text-white'
                }`}
              >
                {f.label} <span className="ml-1 opacity-70">{count}</span>
              </button>
            );
          })}
        </div>

        <div className="relative">
          <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            value={query}
            onChange={e => setQuery(e.target.value)}
            placeholder="Search name, owner or address"
            className="w-full lg:w-80 pl-9 pr-3 py-2.5 rounded-xl bg-slate-900/70 border border-slate-800 text-sm text-white placeholder:text-slate-500 focus:outline-none focus:border-orange-500/40"
          />
        </div>
      </div>

      {visible.length === 0 ? (
        <EmptyState
          title={restaurants.length === 0 ? 'No restaurants registered yet' : 'No restaurants match this filter'}
          hint={
            restaurants.length === 0
              ? 'Complete signup in the Partner app — the restaurant appears here within seconds.'
              : 'Try a different filter or clear the search.'
          }
        />
      ) : (
        <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
          {visible.map(r => (
            <div key={r.id} className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4">
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-start gap-3 min-w-0">
                  <div className="w-11 h-11 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center shrink-0">
                    <Store className="w-5 h-5 text-orange-400" />
                  </div>
                  <div className="min-w-0">
                    <h3 className="font-extrabold text-white truncate">{r.name}</h3>
                    <p className="text-xs text-slate-400 truncate">
                      {(r.cuisineTypes || []).join(' · ') || 'Multi-Cuisine'}
                    </p>
                  </div>
                </div>
                <span className={`shrink-0 px-2.5 py-1 rounded-lg text-xs font-bold border ${SUBSCRIPTION_STYLES[r.subscriptionStatus] || ''}`}>
                  {SUBSCRIPTION_LABELS[r.subscriptionStatus] || r.subscriptionStatus}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-3 text-xs">
                <Detail icon={MapPin} value={r.address || '—'} />
                <Detail icon={Phone} value={r.phone || '—'} />
                <Detail icon={Mail} value={r.email || '—'} />
                <Detail icon={Wallet} value={r.upiId || '—'} />
              </div>

              <div className="grid grid-cols-4 gap-2 pt-3 border-t border-slate-800">
                <Stat label="Rating" value={r.totalRatings ? `${r.rating} ★` : 'New'} icon={Star} />
                <Stat label="Menu" value={r.menuItemCount ?? 0} icon={Utensils} />
                <Stat label="Orders" value={r.totalOrders ?? 0} />
                <Stat
                  label="Store"
                  value={r.isOpen ? 'Open' : 'Closed'}
                  icon={r.isOpen ? DoorOpen : DoorClosed}
                  tone={r.isOpen ? 'text-emerald-400' : 'text-red-400'}
                />
              </div>

              <div className="flex items-center justify-between text-xs text-slate-500 pt-1">
                <span>Joined {formatDate(r.joinedAt)}</span>
                <span>Min order ₹{r.minOrderValue} · {r.deliveryRadiusKm} km radius</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function Detail({ icon: Icon, value }) {
  return (
    <div className="flex items-start gap-2 text-slate-300 min-w-0">
      <Icon className="w-3.5 h-3.5 text-slate-500 mt-0.5 shrink-0" />
      <span className="truncate">{value}</span>
    </div>
  );
}

function Stat({ label, value, icon: Icon, tone = 'text-white' }) {
  return (
    <div>
      <p className="text-[10px] uppercase tracking-wider text-slate-500 font-bold">{label}</p>
      <p className={`text-sm font-black flex items-center gap-1 ${tone}`}>
        {Icon && <Icon className="w-3.5 h-3.5" />}
        {value}
      </p>
    </div>
  );
}
