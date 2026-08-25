import React from 'react';
import {
  Store, CreditCard, ShoppingBag, Star, TrendingUp, AlertTriangle, ArrowRight, Clock
} from 'lucide-react';
import { api, useLiveData, money, formatTime, ORDER_STATUS_STYLES, SUBSCRIPTION_STYLES, SUBSCRIPTION_LABELS, statusLabel } from '../api';
import { LoadingState, ErrorState, EmptyState } from '../components/StateBlocks';

const REFRESH_EVENTS = ['order:new', 'order:status', 'subscription:updated', 'restaurant:new', 'review:new'];

export default function DashboardOverview({ setActiveTab }) {
  const statsQuery = useLiveData(api.stats, REFRESH_EVENTS);
  const restaurantsQuery = useLiveData(api.restaurants, REFRESH_EVENTS);
  const ordersQuery = useLiveData(api.orders, REFRESH_EVENTS);

  if (statsQuery.loading) return <LoadingState label="Loading platform metrics…" />;
  if (statsQuery.error) return <ErrorState message={statsQuery.error} onRetry={statsQuery.reload} />;

  const stats = statsQuery.data || {};
  const restaurants = restaurantsQuery.data || [];
  const orders = ordersQuery.data || [];

  const needsAttention = restaurants.filter(
    r => r.subscriptionStatus === 'OVERDUE' || r.subscriptionStatus === 'SUSPENDED'
  );

  const recentSubOrders = orders
    .flatMap(o => o.subOrders.map(s => ({ ...s, orderId: o.orderId, customer: o.userName, createdAt: o.createdAt })))
    .slice(0, 6);

  const tiles = [
    { label: 'Partner restaurants', value: stats.totalRestaurants ?? 0, hint: `${stats.activePaid ?? 0} paying · ${stats.activeTrials ?? 0} on trial`, icon: Store, tone: 'text-white', chip: 'bg-orange-500/10 border-orange-500/20 text-orange-400', tab: 'restaurants' },
    { label: 'Monthly recurring revenue', value: money(stats.currentMRR), hint: `${money(stats.monthlyPrice)} per restaurant per month`, icon: CreditCard, tone: 'text-emerald-400', chip: 'bg-emerald-500/10 border-emerald-500/20 text-emerald-400', tab: 'subscriptions' },
    { label: 'Orders today', value: stats.ordersToday ?? 0, hint: `${money(stats.gmvToday)} order value · ${stats.liveSubOrders ?? 0} live now`, icon: ShoppingBag, tone: 'text-blue-400', chip: 'bg-blue-500/10 border-blue-500/20 text-blue-400', tab: 'orders' },
    { label: 'Customer reviews', value: stats.totalReviews ?? 0, hint: 'Ratings posted after delivery', icon: Star, tone: 'text-amber-400', chip: 'bg-amber-500/10 border-amber-500/20 text-amber-400' }
  ];

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        {tiles.map(t => (
          <button
            key={t.label}
            onClick={() => t.tab && setActiveTab?.(t.tab)}
            className={`text-left p-5 rounded-2xl bg-slate-900/70 border border-slate-800 ${t.tab ? 'hover:border-slate-700 cursor-pointer' : 'cursor-default'}`}
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold uppercase tracking-wider text-slate-400">{t.label}</span>
              <div className={`w-8 h-8 rounded-lg border flex items-center justify-center ${t.chip}`}>
                <t.icon className="w-4 h-4" />
              </div>
            </div>
            <p className={`text-3xl font-black mt-3 ${t.tone}`}>{t.value}</p>
            <p className="text-xs text-slate-400 mt-1">{t.hint}</p>
          </button>
        ))}
      </div>

      <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
        <div className="rounded-2xl bg-slate-900/70 border border-slate-800 overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-800 flex items-center justify-between">
            <h3 className="font-bold text-white flex items-center gap-2">
              <AlertTriangle className="w-4 h-4 text-amber-400" />
              Needs your attention
            </h3>
            <button
              onClick={() => setActiveTab?.('subscriptions')}
              className="text-xs font-bold text-orange-400 hover:text-orange-300 inline-flex items-center gap-1"
            >
              Subscriptions <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          {needsAttention.length === 0 ? (
            <EmptyState title="Nothing overdue" hint="Every restaurant is either paid or inside its free trial." />
          ) : (
            <div className="divide-y divide-slate-800">
              {needsAttention.map(r => (
                <div key={r.id} className="px-5 py-3 flex items-center justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-bold text-white truncate">{r.name}</p>
                    <p className="text-xs text-slate-400 truncate">
                      {r.subscription?.suspendedReason || `Due ${r.subscription?.daysUntilDue ?? 0} days ago`}
                    </p>
                  </div>
                  <span className={`shrink-0 px-2.5 py-1 rounded-lg text-xs font-bold border ${SUBSCRIPTION_STYLES[r.subscriptionStatus] || ''}`}>
                    {SUBSCRIPTION_LABELS[r.subscriptionStatus]}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="rounded-2xl bg-slate-900/70 border border-slate-800 overflow-hidden">
          <div className="px-5 py-4 border-b border-slate-800 flex items-center justify-between">
            <h3 className="font-bold text-white flex items-center gap-2">
              <TrendingUp className="w-4 h-4 text-blue-400" />
              Latest sub-orders
            </h3>
            <button
              onClick={() => setActiveTab?.('orders')}
              className="text-xs font-bold text-orange-400 hover:text-orange-300 inline-flex items-center gap-1"
            >
              Live pulse <ArrowRight className="w-3 h-3" />
            </button>
          </div>
          {recentSubOrders.length === 0 ? (
            <EmptyState title="No orders yet" hint="They appear here the moment a customer pays." />
          ) : (
            <div className="divide-y divide-slate-800">
              {recentSubOrders.map(s => (
                <div key={s.subOrderId} className="px-5 py-3 flex items-center justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-bold text-white truncate">{s.restaurantName}</p>
                    <p className="text-xs text-slate-400 truncate flex items-center gap-2">
                      <Clock className="w-3 h-3" />{formatTime(s.createdAt)} · {s.customer} · {money(s.subTotal)}
                    </p>
                  </div>
                  <span className={`shrink-0 px-2.5 py-1 rounded-lg text-xs font-bold border ${ORDER_STATUS_STYLES[s.status] || ''}`}>
                    {statusLabel(s.status)}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
