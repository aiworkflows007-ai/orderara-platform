import React, { useState } from 'react';
import {
  CreditCard, CheckCircle2, Clock, AlertTriangle, ShieldCheck, Sparkles, Ban, RotateCcw
} from 'lucide-react';
import {
  api, useLiveData, money, formatDate, SUBSCRIPTION_LABELS, SUBSCRIPTION_STYLES
} from '../api';
import { LoadingState, ErrorState, EmptyState } from '../components/StateBlocks';

const REFRESH_EVENTS = ['subscription:updated', 'restaurants:updated', 'admin:restaurants:updated'];

export default function SubscriptionsHub() {
  const restaurantsQuery = useLiveData(api.restaurants, REFRESH_EVENTS);
  const statsQuery = useLiveData(api.stats, REFRESH_EVENTS);
  const [busyId, setBusyId] = useState(null);
  const [actionError, setActionError] = useState(null);

  if (restaurantsQuery.loading || statsQuery.loading) return <LoadingState />;
  if (restaurantsQuery.error) {
    return <ErrorState message={restaurantsQuery.error} onRetry={restaurantsQuery.reload} />;
  }

  const restaurants = restaurantsQuery.data || [];
  const stats = statsQuery.data || {};

  // Suspending here genuinely removes the restaurant from the customer app and
  // blocks new orders — this is the enforcement behind the subscription.
  const changeStatus = async (restaurantId, status, reason) => {
    setBusyId(restaurantId);
    setActionError(null);
    try {
      await api.setSubscription(restaurantId, status, reason);
      restaurantsQuery.reload();
      statsQuery.reload();
    } catch (err) {
      setActionError(err.message);
    } finally {
      setBusyId(null);
    }
  };

  const metrics = [
    {
      label: 'Current monthly revenue',
      value: money(stats.currentMRR),
      hint: `${stats.activePaid || 0} paying restaurants @ ${money(stats.monthlyPrice)}/mo`,
      icon: CreditCard,
      accent: 'text-white',
      chip: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400'
    },
    {
      label: 'Projected pipeline MRR',
      value: money(stats.projectedMRR),
      hint: `If all ${stats.activeTrials || 0} active free trials convert`,
      icon: Sparkles,
      // Neutral on purpose. Amber means "watch this" and red means "act on this";
      // a projection is neither, and colouring it purple only diluted the two
      // tiles where colour is actually carrying a status.
      accent: 'text-slate-300',
      chip: 'bg-slate-500/10 border-slate-500/20 text-slate-400'
    },
    {
      label: 'Active free trials',
      value: stats.activeTrials ?? 0,
      hint: `${stats.trialDays || 14}-day trial, ${stats.graceDays || 3}-day grace after due date`,
      icon: Clock,
      accent: 'text-amber-400',
      chip: 'bg-amber-500/10 border-amber-500/20 text-amber-400'
    },
    {
      label: 'Overdue / suspended',
      value: `${stats.overdue ?? 0} / ${stats.suspended ?? 0}`,
      hint: 'Suspended restaurants are hidden from customers',
      icon: AlertTriangle,
      accent: 'text-red-400',
      chip: 'bg-red-500/10 border-red-500/20 text-red-400'
    }
  ];

  return (
    <div className="space-y-6">
      {actionError && (
        <div className="p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-sm text-red-300">
          {actionError}
        </div>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        {metrics.map(m => (
          <div key={m.label} className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400">{m.label}</span>
              <div className={`w-8 h-8 rounded-lg border flex items-center justify-center ${m.chip}`}>
                <m.icon className="w-4 h-4" />
              </div>
            </div>
            <div className="mt-3">
              <span className={`text-3xl font-black ${m.accent}`}>{m.value}</span>
              <p className="text-xs text-slate-400 mt-1">{m.hint}</p>
            </div>
          </div>
        ))}
      </div>

      <div className="rounded-2xl bg-slate-900/70 border border-slate-800 overflow-hidden">
        <div className="px-5 py-4 border-b border-slate-800 flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-orange-400" />
          <h3 className="font-bold text-white">Subscription control</h3>
          <span className="text-xs text-slate-400">— suspending hides the restaurant from every customer app</span>
        </div>

        {restaurants.length === 0 ? (
          <EmptyState
            title="No restaurants yet"
            hint="Register one from the Partner app and it will appear here within seconds."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-900 text-slate-400 text-xs uppercase tracking-wider">
                <tr>
                  <th className="text-left font-bold px-5 py-3">Restaurant</th>
                  <th className="text-left font-bold px-5 py-3">Status</th>
                  <th className="text-left font-bold px-5 py-3">Next due</th>
                  <th className="text-left font-bold px-5 py-3">Orders</th>
                  <th className="text-right font-bold px-5 py-3">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {restaurants.map(r => {
                  const sub = r.subscription || {};
                  const status = r.subscriptionStatus;
                  const busy = busyId === r.id;
                  return (
                    <tr key={r.id} className="hover:bg-slate-800/40">
                      <td className="px-5 py-4">
                        <div className="font-bold text-white">{r.name}</div>
                        <div className="text-xs text-slate-400">
                          {r.ownerName || 'Owner not set'} · {r.phone || 'no phone'}
                        </div>
                      </td>
                      <td className="px-5 py-4">
                        <span className={`px-2.5 py-1 rounded-lg text-xs font-bold border ${SUBSCRIPTION_STYLES[status] || ''}`}>
                          {SUBSCRIPTION_LABELS[status] || status}
                        </span>
                        {status === 'ACTIVE_TRIAL' && (
                          <div className="text-xs text-slate-400 mt-1">
                            {sub.trialDaysRemaining} {sub.trialDaysRemaining === 1 ? 'day' : 'days'} left
                          </div>
                        )}
                        {status === 'SUSPENDED' && sub.suspendedReason && (
                          <div className="text-xs text-slate-500 mt-1 max-w-[220px]">{sub.suspendedReason}</div>
                        )}
                      </td>
                      <td className="px-5 py-4 text-slate-300">{formatDate(sub.nextBillingDate)}</td>
                      <td className="px-5 py-4 text-slate-300">{r.totalOrders ?? 0}</td>
                      <td className="px-5 py-4">
                        <div className="flex items-center justify-end gap-2">
                          {status !== 'ACTIVE_PAID' && (
                            <button
                              disabled={busy}
                              onClick={() => changeStatus(r.id, 'ACTIVE_PAID')}
                              className="px-3 py-1.5 rounded-lg text-xs font-bold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 hover:bg-emerald-500/20 disabled:opacity-50 inline-flex items-center gap-1.5"
                            >
                              <CheckCircle2 className="w-3.5 h-3.5" />
                              Mark paid
                            </button>
                          )}
                          {status === 'SUSPENDED' ? (
                            <button
                              disabled={busy}
                              onClick={() => changeStatus(r.id, 'ACTIVE_TRIAL')}
                              className="px-3 py-1.5 rounded-lg text-xs font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20 hover:bg-blue-500/20 disabled:opacity-50 inline-flex items-center gap-1.5"
                            >
                              <RotateCcw className="w-3.5 h-3.5" />
                              Reactivate
                            </button>
                          ) : (
                            <button
                              disabled={busy}
                              onClick={() => changeStatus(r.id, 'SUSPENDED', 'Suspended by platform admin')}
                              className="px-3 py-1.5 rounded-lg text-xs font-bold bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20 disabled:opacity-50 inline-flex items-center gap-1.5"
                            >
                              <Ban className="w-3.5 h-3.5" />
                              Suspend
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
