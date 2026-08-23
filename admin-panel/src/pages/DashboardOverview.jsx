import React from 'react';
import { 
  Store, 
  CreditCard, 
  ShoppingBag, 
  TrendingUp, 
  Clock, 
  ArrowUpRight, 
  Users,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { initialRestaurants, liveOrders, monthlyRevenueStats } from '../mockData';

export default function DashboardOverview({ setActiveTab }) {
  const totalRestaurants = initialRestaurants.length;
  const activePaid = initialRestaurants.filter(r => r.subscriptionStatus === 'ACTIVE_PAID').length;
  const activeTrials = initialRestaurants.filter(r => r.subscriptionStatus === 'ACTIVE_TRIAL').length;
  const suspended = initialRestaurants.filter(r => r.subscriptionStatus === 'SUSPENDED').length;

  const currentMRR = activePaid * 999;
  const potentialMRR = (activePaid + activeTrials) * 999;

  return (
    <div className="space-y-6">
      {/* 4 Stat Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        {/* Total Listed Restaurants */}
        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Total Restaurants</span>
            <div className="w-9 h-9 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center">
              <Store className="w-5 h-5 text-orange-400" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-black text-white">{totalRestaurants}</span>
            <span className="text-xs font-semibold text-emerald-400 flex items-center gap-0.5">
              <ArrowUpRight className="w-3.5 h-3.5" /> +4 this week
            </span>
          </div>
          <div className="mt-2 flex items-center gap-3 text-xs text-slate-400">
            <span className="text-emerald-400 font-semibold">{activePaid} Paid</span>
            <span>•</span>
            <span className="text-amber-400 font-semibold">{activeTrials} Free Trials</span>
          </div>
        </div>

        {/* Monthly Recurring Revenue (MRR) */}
        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Monthly Subscriptions (MRR)</span>
            <div className="w-9 h-9 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
              <CreditCard className="w-5 h-5 text-emerald-400" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-black text-white">₹{currentMRR.toLocaleString()}</span>
            <span className="text-xs font-semibold text-emerald-400 flex items-center gap-0.5">
              <ArrowUpRight className="w-3.5 h-3.5" /> 100% Retained
            </span>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            Potential with Trials: <span className="text-white font-bold">₹{potentialMRR.toLocaleString()}/mo</span>
          </p>
        </div>

        {/* Today's Platform Sub-Orders */}
        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Today's Platform Orders</span>
            <div className="w-9 h-9 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center">
              <ShoppingBag className="w-5 h-5 text-blue-400" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-black text-white">142</span>
            <span className="text-xs font-semibold text-blue-400">Split Sub-Orders</span>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            0% Commission model • Delivery by restaurants
          </p>
        </div>

        {/* Free Trial Conversion Rate */}
        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm relative overflow-hidden">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Trial Conversion Rate</span>
            <div className="w-9 h-9 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
              <TrendingUp className="w-5 h-5 text-purple-400" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-black text-white">87.5%</span>
            <span className="text-xs font-semibold text-emerald-400">+5.2%</span>
          </div>
          <p className="mt-2 text-xs text-slate-400">
            14-Day Automated Trial System
          </p>
        </div>
      </div>

      {/* Charts & Pulse Section */}
      <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
        {/* MRR Growth Chart */}
        <div className="xl:col-span-2 p-6 rounded-2xl bg-slate-900/70 border border-slate-800">
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-base font-bold text-white">Monthly Subscription Revenue Growth</h3>
              <p className="text-xs text-slate-400 font-medium">Flat ₹999/restaurant subscription model growth</p>
            </div>
            <div className="flex items-center gap-2">
              <span className="w-3 h-3 rounded-full bg-orange-500" />
              <span className="text-xs font-semibold text-slate-300">MRR (₹)</span>
            </div>
          </div>

          {/* Bar Chart Visualization */}
          <div className="h-52 flex items-end justify-between gap-4 pt-6 px-2">
            {monthlyRevenueStats.map((item) => {
              const maxVal = 25000;
              const heightPct = Math.round((item.mrr / maxVal) * 100);
              return (
                <div key={item.month} className="flex-1 flex flex-col items-center gap-2 h-full justify-end group">
                  <div className="text-[11px] font-bold text-slate-400 group-hover:text-orange-400 transition-colors">
                    ₹{item.mrr.toLocaleString()}
                  </div>
                  <div 
                    style={{ height: `${heightPct}%` }}
                    className="w-full max-w-[48px] bg-gradient-to-t from-orange-600 to-orange-400 rounded-t-xl transition-all duration-300 group-hover:from-orange-500 group-hover:to-orange-300 shadow-lg shadow-orange-500/10"
                  />
                  <span className="text-xs font-bold text-slate-400">{item.month}</span>
                </div>
              );
            })}
          </div>
        </div>

        {/* Free Trials Expiring Soon Widget */}
        <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-base font-bold text-white flex items-center gap-2">
                <Clock className="w-4 h-4 text-amber-400" />
                Trial Watchlist
              </h3>
              <span className="text-xs font-bold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20">
                Action Needed
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-4">
              Restaurants reaching the end of their 14-day free trial:
            </p>

            <div className="space-y-3">
              {initialRestaurants
                .filter(r => r.subscriptionStatus === 'ACTIVE_TRIAL')
                .slice(0, 3)
                .map(r => (
                  <div key={r.id} className="p-3 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center justify-between">
                    <div>
                      <p className="text-sm font-bold text-white">{r.name}</p>
                      <p className="text-xs text-slate-400">{r.owner} • {r.phone}</p>
                    </div>
                    <div className="text-right">
                      <span className="text-xs font-extrabold px-2 py-1 rounded bg-amber-500/15 text-amber-400 border border-amber-500/30">
                        {r.trialDaysRemaining}d left
                      </span>
                    </div>
                  </div>
                ))}
            </div>
          </div>

          <button
            onClick={() => setActiveTab('subscriptions')}
            className="w-full mt-4 py-2.5 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold transition-colors border border-slate-700"
          >
            View All Subscription Accounts →
          </button>
        </div>
      </div>

      {/* Live Orders Pulse Preview */}
      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800">
        <div className="flex items-center justify-between mb-5">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
              Live Multi-Restaurant Orders Pulse
            </h3>
            <p className="text-xs text-slate-400 font-medium">Real-time split sub-orders and kitchen preparation updates</p>
          </div>
          <button
            onClick={() => setActiveTab('orders')}
            className="text-xs font-bold text-orange-400 hover:text-orange-300 transition-colors"
          >
            View Live Stream →
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-800 text-xs font-bold uppercase tracking-wider text-slate-400">
                <th className="pb-3">Parent Order</th>
                <th className="pb-3">Customer</th>
                <th className="pb-3">Independent Sub-Orders</th>
                <th className="pb-3">Total Paid</th>
                <th className="pb-3">Payment</th>
                <th className="pb-3">Order Time</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {liveOrders.map(order => (
                <tr key={order.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3.5 font-bold text-white font-mono">{order.id}</td>
                  <td className="py-3.5 font-semibold text-slate-200">{order.customer}</td>
                  <td className="py-3.5">
                    <div className="space-y-1.5">
                      {order.subOrders.map(so => (
                        <div key={so.id} className="flex items-center gap-2 text-xs">
                          <span className="font-mono text-slate-400 font-bold">#{so.id}</span>
                          <span className="text-slate-300 font-semibold">{so.restaurant}</span>
                          <span className={`px-1.5 py-0.5 rounded text-[10px] font-black ${
                            so.status === 'DELIVERED' ? 'bg-emerald-500/15 text-emerald-400' :
                            so.status === 'OUT_FOR_DELIVERY' ? 'bg-blue-500/15 text-blue-400' :
                            so.status === 'PREPARING' ? 'bg-orange-500/15 text-orange-400' :
                            'bg-amber-500/15 text-amber-400'
                          }`}>
                            {so.status}
                          </span>
                        </div>
                      ))}
                    </div>
                  </td>
                  <td className="py-3.5 font-bold text-emerald-400">₹{order.totalAmount}</td>
                  <td className="py-3.5 text-xs font-medium text-slate-400">{order.paymentMethod}</td>
                  <td className="py-3.5 text-xs text-slate-400 font-mono">{order.time}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
