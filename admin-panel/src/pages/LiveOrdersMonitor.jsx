import React, { useState } from 'react';
import { ShoppingBag, Clock, CheckCircle2, Bike, RefreshCw, Filter } from 'lucide-react';
import { liveOrders } from '../mockData';

export default function LiveOrdersMonitor() {
  const [orders, setOrders] = useState(liveOrders);
  const [filterStatus, setFilterStatus] = useState('ALL');

  return (
    <div className="space-y-6">
      {/* Live Order Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Active Live Orders</p>
            <p className="text-2xl font-black text-white mt-1">2 Orders (3 Sub-Orders)</p>
          </div>
          <div className="w-10 h-10 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center">
            <ShoppingBag className="w-5 h-5 text-orange-400" />
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Direct Staff Dispatches</p>
            <p className="text-2xl font-black text-blue-400 mt-1">1 Out for Delivery</p>
          </div>
          <div className="w-10 h-10 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center">
            <Bike className="w-5 h-5 text-blue-400" />
          </div>
        </div>

        <div className="p-4 rounded-2xl bg-slate-900/70 border border-slate-800 flex items-center justify-between">
          <div>
            <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Delivered Today</p>
            <p className="text-2xl font-black text-emerald-400 mt-1">141 Sub-Orders</p>
          </div>
          <div className="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
            <CheckCircle2 className="w-5 h-5 text-emerald-400" />
          </div>
        </div>
      </div>

      {/* Orders Stream Cards */}
      <div className="space-y-4">
        {orders.map(order => (
          <div key={order.id} className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4 shadow-sm">
            {/* Parent Order Header */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-3 border-b border-slate-800 gap-2">
              <div className="flex items-center space-x-3">
                <div className="w-9 h-9 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center font-bold text-orange-400 text-xs">
                  ORD
                </div>
                <div>
                  <h4 className="font-extrabold text-white font-mono text-base flex items-center gap-2">
                    {order.id}
                    <span className="text-xs font-medium text-slate-400 font-sans">
                      ({order.subOrders.length} Split Sub-Orders)
                    </span>
                  </h4>
                  <p className="text-xs text-slate-400">
                    Customer: <span className="text-slate-200 font-bold">{order.customer}</span> ({order.phone}) • {order.address}
                  </p>
                </div>
              </div>

              <div className="text-right flex sm:flex-col items-center sm:items-end justify-between">
                <span className="text-base font-black text-emerald-400">₹{order.totalAmount}</span>
                <span className="text-xs text-slate-400">{order.paymentMethod} • {order.time}</span>
              </div>
            </div>

            {/* Split Sub-Orders Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {order.subOrders.map(so => (
                <div key={so.id} className="p-4 rounded-xl bg-slate-800/40 border border-slate-800 space-y-3">
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="font-bold text-white text-sm">{so.restaurant}</p>
                      <p className="text-xs font-mono text-slate-400">Sub-Order #{so.id}</p>
                    </div>

                    <span className={`px-2 py-0.5 rounded-full text-xs font-extrabold ${
                      so.status === 'DELIVERED' ? 'bg-emerald-500/15 text-emerald-400 border border-emerald-500/30' :
                      so.status === 'OUT_FOR_DELIVERY' ? 'bg-blue-500/15 text-blue-400 border border-blue-500/30' :
                      so.status === 'PREPARING' ? 'bg-orange-500/15 text-orange-400 border border-orange-500/30' :
                      'bg-amber-500/15 text-amber-400 border border-amber-500/30'
                    }`}>
                      {so.status.replace(/_/g, ' ')}
                    </span>
                  </div>

                  <div className="p-2.5 rounded-lg bg-slate-900/60 border border-slate-800/80 text-xs text-slate-300">
                    {so.items}
                  </div>

                  <div className="flex items-center justify-between text-xs pt-1">
                    <span className="text-slate-400">Subtotal: <strong className="text-white">₹{so.amount}</strong></span>
                    <span className="text-slate-400 flex items-center gap-1">
                      <Clock className="w-3 h-3 text-orange-400" />
                      Direct Restaurant Staff Delivery
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
