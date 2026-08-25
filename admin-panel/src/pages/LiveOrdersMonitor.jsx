import React, { useState } from 'react';
import { ShoppingBag, Bike, CheckCircle2, User, MapPin, Store } from 'lucide-react';
import { api, useLiveData, money, formatTime, ORDER_STATUS_STYLES, statusLabel } from '../api';
import { LoadingState, ErrorState, EmptyState } from '../components/StateBlocks';

const REFRESH_EVENTS = ['order:new', 'order:status'];
const LIVE_STATUSES = ['PLACED', 'ACCEPTED', 'PREPARING', 'OUT_FOR_DELIVERY'];

const FILTERS = [
  { id: 'LIVE', label: 'Live now' },
  { id: 'ALL', label: 'All orders' },
  { id: 'DELIVERED', label: 'Delivered' },
  { id: 'REJECTED', label: 'Rejected' }
];

export default function LiveOrdersMonitor() {
  const ordersQuery = useLiveData(api.orders, REFRESH_EVENTS, 4000);
  const statsQuery = useLiveData(api.stats, REFRESH_EVENTS, 4000);
  const [filter, setFilter] = useState('LIVE');

  if (ordersQuery.loading) return <LoadingState label="Loading live orders…" />;
  if (ordersQuery.error) return <ErrorState message={ordersQuery.error} onRetry={ordersQuery.reload} />;

  const orders = ordersQuery.data || [];
  const stats = statsQuery.data || {};

  const matches = sub => {
    if (filter === 'ALL') return true;
    if (filter === 'LIVE') return LIVE_STATUSES.includes(sub.status);
    if (filter === 'REJECTED') return sub.status === 'REJECTED' || sub.status === 'CANCELLED';
    return sub.status === filter;
  };

  // Keep the parent order together, showing only the sub-orders that match.
  const visible = orders
    .map(o => ({ ...o, subOrders: o.subOrders.filter(matches) }))
    .filter(o => o.subOrders.length > 0);

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Metric label="Live sub-orders" value={stats.liveSubOrders ?? 0} icon={ShoppingBag} tone="text-orange-400" chip="bg-orange-500/10 border-orange-500/20" />
        <Metric label="Orders today" value={stats.ordersToday ?? 0} icon={Bike} tone="text-blue-400" chip="bg-blue-500/10 border-blue-500/20" />
        <Metric label="Delivered (all time)" value={stats.deliveredToday ?? 0} icon={CheckCircle2} tone="text-emerald-400" chip="bg-emerald-500/10 border-emerald-500/20" />
        <Metric label="Order value today" value={money(stats.gmvToday)} icon={ShoppingBag} tone="text-white" chip="bg-slate-800 border-slate-700" />
      </div>

      <div className="flex flex-wrap gap-2">
        {FILTERS.map(f => (
          <button
            key={f.id}
            onClick={() => setFilter(f.id)}
            className={`px-3.5 py-2 rounded-xl text-xs font-bold border transition-colors ${
              filter === f.id
                ? 'bg-orange-500/15 text-orange-400 border-orange-500/30'
                : 'bg-slate-900/70 text-slate-400 border-slate-800 hover:text-white'
            }`}
          >
            {f.label}
          </button>
        ))}
      </div>

      {visible.length === 0 ? (
        <EmptyState
          title={orders.length === 0 ? 'No orders yet' : 'Nothing matches this filter'}
          hint={
            orders.length === 0
              ? 'Place an order in the Customer app — it appears here the moment it is paid for.'
              : 'Switch to "All orders" to see completed ones.'
          }
        />
      ) : (
        <div className="space-y-4">
          {visible.map(order => (
            <div key={order.orderId} className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 pb-3 border-b border-slate-800">
                <div className="flex items-center gap-3">
                  <div className="w-9 h-9 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center text-orange-400 font-bold text-xs">
                    ORD
                  </div>
                  <div>
                    <h4 className="font-extrabold text-white font-mono text-base flex items-center gap-2 flex-wrap">
                      {order.orderId}
                      <span className="text-xs font-medium text-slate-400 font-sans">
                        {order.subOrders.length > 1
                          ? `split across ${order.subOrders.length} restaurants`
                          : 'single restaurant'}
                      </span>
                    </h4>
                    <p className="text-xs text-slate-400 flex items-center gap-3 flex-wrap">
                      <span className="flex items-center gap-1"><User className="w-3 h-3" />{order.userName}</span>
                      <span className="flex items-center gap-1"><MapPin className="w-3 h-3" />{order.deliveryAddress || '—'}</span>
                      <span>{formatTime(order.createdAt)}</span>
                    </p>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-lg font-black text-white">{money(order.totalPaid)}</p>
                  <p className="text-xs text-slate-400">{order.paymentMethod} · {order.paymentStatus}</p>
                </div>
              </div>

              <div className="space-y-3">
                {order.subOrders.map(sub => (
                  <div key={sub.subOrderId} className="p-4 rounded-xl bg-slate-950/50 border border-slate-800">
                    <div className="flex items-start justify-between gap-3 flex-wrap">
                      <div className="min-w-0">
                        <p className="font-bold text-white flex items-center gap-2">
                          <Store className="w-4 h-4 text-slate-500" />
                          {sub.restaurantName}
                        </p>
                        <p className="text-xs text-slate-500 font-mono mt-0.5">{sub.subOrderId}</p>
                      </div>
                      <span className={`px-2.5 py-1 rounded-lg text-xs font-bold border ${ORDER_STATUS_STYLES[sub.status] || ''}`}>
                        {statusLabel(sub.status)}
                      </span>
                    </div>

                    <ul className="mt-3 space-y-1 text-sm text-slate-300">
                      {sub.items.map((item, i) => (
                        <li key={`${sub.subOrderId}-${i}`} className="flex justify-between gap-3">
                          <span className="truncate">{item.quantity}× {item.name}</span>
                          <span className="text-slate-400 shrink-0">{money(item.totalPrice)}</span>
                        </li>
                      ))}
                    </ul>

                    <div className="mt-3 pt-3 border-t border-slate-800 flex items-center justify-between text-xs">
                      <span className="text-slate-500">
                        {sub.driverName && sub.driverName !== 'Pending assignment'
                          ? `Delivery: ${sub.driverName}`
                          : 'Delivery staff not assigned yet'}
                      </span>
                      <span className="text-slate-300 font-bold">
                        {money(sub.subTotal + sub.deliveryFee - sub.discount)}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function Metric({ label, value, icon: Icon, tone, chip }) {
  return (
    <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800 flex items-center justify-between">
      <div>
        <p className="text-xs font-bold uppercase tracking-wider text-slate-400">{label}</p>
        <p className={`text-2xl font-black mt-1 ${tone}`}>{value}</p>
      </div>
      <div className={`w-10 h-10 rounded-xl border flex items-center justify-center ${chip}`}>
        <Icon className={`w-5 h-5 ${tone}`} />
      </div>
    </div>
  );
}
