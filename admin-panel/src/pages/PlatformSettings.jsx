import React from 'react';
import { Shield, Server, Clock, CreditCard, AlertTriangle, Info } from 'lucide-react';
import { api, useLiveData, money, API_BASE } from '../api';
import { LoadingState, ErrorState } from '../components/StateBlocks';

/**
 * The billing policy the server actually enforces.
 *
 * These come from the backend rather than being typed in here, so what this
 * page shows is exactly what the Partner apps are billed on. They are set by
 * environment variables on the server (TRIAL_DAYS, GRACE_DAYS, MONTHLY_PRICE).
 */
export default function PlatformSettings() {
  const { data, error, loading, reload } = useLiveData(api.stats, ['subscription:updated']);

  if (loading) return <LoadingState label="Reading platform policy…" />;
  if (error) return <ErrorState message={error} onRetry={reload} />;

  const stats = data || {};

  const policy = [
    {
      label: 'Subscription price',
      value: `${money(stats.monthlyPrice)} / month`,
      hint: 'Flat fee per restaurant, unlimited orders, no commission',
      icon: CreditCard,
      env: 'MONTHLY_PRICE'
    },
    {
      label: 'Free trial length',
      value: `${stats.trialDays} days`,
      hint: 'Starts the moment a restaurant completes signup',
      icon: Clock,
      env: 'TRIAL_DAYS'
    },
    {
      label: 'Grace period',
      value: `${stats.graceDays} days`,
      hint: 'After the due date before the listing is auto-suspended',
      icon: AlertTriangle,
      env: 'GRACE_DAYS'
    }
  ];

  return (
    <div className="space-y-6">
      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-5">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Shield className="w-4 h-4 text-orange-400" />
            Billing policy in force
          </h3>
          <span className="text-xs font-semibold text-slate-400">{stats.planName}</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {policy.map(p => (
            <div key={p.label} className="p-4 rounded-xl bg-slate-950/50 border border-slate-800">
              <div className="flex items-center gap-2 text-slate-400">
                <p.icon className="w-4 h-4" />
                <span className="text-xs font-bold uppercase tracking-wider">{p.label}</span>
              </div>
              <p className="text-2xl font-black text-white mt-2">{p.value}</p>
              <p className="text-xs text-slate-400 mt-1">{p.hint}</p>
              <p className="text-[10px] font-mono text-slate-600 mt-2">{p.env}</p>
            </div>
          ))}
        </div>

        <div className="flex items-start gap-2 p-3 rounded-xl bg-blue-500/5 border border-blue-500/20 text-xs text-blue-300">
          <Info className="w-4 h-4 shrink-0 mt-0.5" />
          <p>
            These values are read from the server, not stored in this page. To change them, update the
            environment variables on the backend and restart it — existing trials keep the end date they
            were given at signup.
          </p>
        </div>
      </div>

      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4">
        <h3 className="text-base font-bold text-white flex items-center gap-2 border-b border-slate-800 pb-3">
          <Server className="w-4 h-4 text-orange-400" />
          Connection
        </h3>
        <dl className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
          <Row label="Backend" value={API_BASE || window.location.origin} mono />
          <Row label="Restaurants on record" value={stats.totalRestaurants ?? 0} />
          <Row label="Orders on record" value={stats.totalOrders ?? 0} />
          <Row label="Reviews on record" value={stats.totalReviews ?? 0} />
        </dl>
        <p className="text-xs text-slate-500">
          Coupons are created and managed by each restaurant in the Partner app, not platform-wide.
        </p>
      </div>
    </div>
  );
}

function Row({ label, value, mono }) {
  return (
    <div>
      <dt className="text-xs font-bold uppercase tracking-wider text-slate-400">{label}</dt>
      <dd className={`text-white font-bold mt-0.5 ${mono ? 'font-mono text-xs break-all' : ''}`}>{value}</dd>
    </div>
  );
}
