import React, { useState } from 'react';
import { 
  Search, 
  Filter, 
  Store, 
  MapPin, 
  Phone, 
  Mail, 
  CreditCard, 
  ShieldAlert, 
  ShieldCheck, 
  Plus, 
  Clock, 
  CheckCircle2, 
  XCircle,
  MoreVertical
} from 'lucide-react';
import { initialRestaurants } from '../mockData';

export default function RestaurantsManagement() {
  const [restaurants, setRestaurants] = useState(initialRestaurants);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [selectedRest, setSelectedRest] = useState(null);

  const filtered = restaurants.filter(r => {
    const matchSearch = r.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                        r.owner.toLowerCase().includes(searchQuery.toLowerCase()) ||
                        r.address.toLowerCase().includes(searchQuery.toLowerCase());
    const matchStatus = statusFilter === 'ALL' ||
                        (statusFilter === 'PAID' && r.subscriptionStatus === 'ACTIVE_PAID') ||
                        (statusFilter === 'TRIAL' && r.subscriptionStatus === 'ACTIVE_TRIAL') ||
                        (statusFilter === 'SUSPENDED' && r.subscriptionStatus === 'SUSPENDED') ||
                        (statusFilter === 'ONLINE' && r.isOnline);
    return matchSearch && matchStatus;
  });

  const toggleSuspend = (id) => {
    setRestaurants(prev => prev.map(r => {
      if (r.id === id) {
        const nextStatus = r.subscriptionStatus === 'SUSPENDED' ? 'ACTIVE_PAID' : 'SUSPENDED';
        return { ...r, subscriptionStatus: nextStatus, isOnline: nextStatus !== 'SUSPENDED' };
      }
      return r;
    }));
  };

  const extendTrial = (id) => {
    setRestaurants(prev => prev.map(r => {
      if (r.id === id) {
        return { ...r, trialDaysRemaining: r.trialDaysRemaining + 7, subscriptionStatus: 'ACTIVE_TRIAL' };
      }
      return r;
    }));
  };

  return (
    <div className="space-y-6">
      {/* Top Controls Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Search Input */}
        <div className="relative flex-1 max-w-md">
          <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search restaurants by name, owner, or area..."
            className="w-full pl-10 pr-4 py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-orange-500 transition-colors"
          />
        </div>

        {/* Status Filter Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1">
          {[
            { id: 'ALL', label: `All (${restaurants.length})` },
            { id: 'PAID', label: 'Active Paid' },
            { id: 'TRIAL', label: '14-Day Free Trials' },
            { id: 'ONLINE', label: 'Online Now' },
            { id: 'SUSPENDED', label: 'Suspended' }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setStatusFilter(tab.id)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-bold transition-all whitespace-nowrap ${
                statusFilter === tab.id
                  ? 'bg-orange-500 text-white shadow-md shadow-orange-500/20'
                  : 'bg-slate-900 border border-slate-800 text-slate-400 hover:text-slate-200'
              }`}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Restaurants Table Card */}
      <div className="rounded-2xl bg-slate-900/70 border border-slate-800 overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/40 text-xs font-bold uppercase tracking-wider text-slate-400">
                <th className="py-3.5 px-5">Restaurant</th>
                <th className="py-3.5 px-4">Owner & Contact</th>
                <th className="py-3.5 px-4">Radius & Min Order</th>
                <th className="py-3.5 px-4">Subscription Plan</th>
                <th className="py-3.5 px-4">Store State</th>
                <th className="py-3.5 px-5 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {filtered.map(r => (
                <tr key={r.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-4 px-5">
                    <div className="flex items-center space-x-3">
                      <div className="w-10 h-10 rounded-xl bg-orange-500/10 border border-orange-500/20 flex items-center justify-center font-black text-orange-400 text-sm">
                        {r.name.charAt(0)}
                      </div>
                      <div>
                        <p className="font-bold text-white flex items-center gap-1.5">
                          {r.name}
                          <span className="text-xs font-bold text-emerald-400">★ {r.rating}</span>
                        </p>
                        <p className="text-xs text-slate-400 flex items-center gap-1">
                          <MapPin className="w-3 h-3 text-slate-500" />
                          {r.address}
                        </p>
                      </div>
                    </div>
                  </td>

                  <td className="py-4 px-4">
                    <p className="font-semibold text-slate-200">{r.owner}</p>
                    <p className="text-xs text-slate-400">{r.phone}</p>
                  </td>

                  <td className="py-4 px-4">
                    <p className="text-xs font-bold text-slate-200">Radius: {r.deliveryRadiusKm} km</p>
                    <p className="text-xs text-slate-400">Min Order: ₹{r.minOrderValue.toInt ? r.minOrderValue.toInt() : r.minOrderValue}</p>
                  </td>

                  <td className="py-4 px-4">
                    <div>
                      {r.subscriptionStatus === 'ACTIVE_PAID' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-extrabold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                          <CheckCircle2 className="w-3.5 h-3.5" /> ₹999/mo Paid
                        </span>
                      )}
                      {r.subscriptionStatus === 'ACTIVE_TRIAL' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-extrabold bg-amber-500/15 text-amber-400 border border-amber-500/30">
                          <Clock className="w-3.5 h-3.5" /> Trial: {r.trialDaysRemaining}d left
                        </span>
                      )}
                      {r.subscriptionStatus === 'SUSPENDED' && (
                        <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-extrabold bg-red-500/15 text-red-400 border border-red-500/30">
                          <XCircle className="w-3.5 h-3.5" /> Suspended
                        </span>
                      )}
                    </div>
                  </td>

                  <td className="py-4 px-4">
                    <span className={`inline-flex items-center gap-1.5 text-xs font-bold ${
                      r.isOnline ? 'text-emerald-400' : 'text-slate-400'
                    }`}>
                      <span className={`w-2 h-2 rounded-full ${r.isOnline ? 'bg-emerald-500 animate-pulse' : 'bg-slate-600'}`} />
                      {r.isOnline ? 'Online' : 'Offline'}
                    </span>
                  </td>

                  <td className="py-4 px-5 text-right space-x-2">
                    {r.subscriptionStatus === 'ACTIVE_TRIAL' && (
                      <button
                        onClick={() => extendTrial(r.id)}
                        className="px-2.5 py-1 rounded-lg bg-amber-500/15 text-amber-400 hover:bg-amber-500/25 border border-amber-500/30 text-xs font-bold transition-colors"
                        title="Add +7 days to free trial"
                      >
                        +7d Trial
                      </button>
                    )}

                    <button
                      onClick={() => toggleSuspend(r.id)}
                      className={`px-2.5 py-1 rounded-lg text-xs font-bold transition-colors border ${
                        r.subscriptionStatus === 'SUSPENDED'
                          ? 'bg-emerald-500/15 text-emerald-400 border-emerald-500/30 hover:bg-emerald-500/25'
                          : 'bg-red-500/10 text-red-400 border-red-500/20 hover:bg-red-500/20'
                      }`}
                    >
                      {r.subscriptionStatus === 'SUSPENDED' ? 'Reactivate' : 'Suspend'}
                    </button>

                    <button
                      onClick={() => setSelectedRest(r)}
                      className="px-2.5 py-1 rounded-lg bg-slate-800 text-slate-300 hover:text-white border border-slate-700 text-xs font-bold transition-colors"
                    >
                      Details
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Restaurant Details Modal */}
      {selectedRest && (
        <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="w-full max-w-lg rounded-2xl bg-slate-900 border border-slate-800 p-6 space-y-5 shadow-2xl">
            <div className="flex items-center justify-between border-b border-slate-800 pb-4">
              <div>
                <h3 className="text-lg font-bold text-white">{selectedRest.name}</h3>
                <p className="text-xs text-slate-400">ID: {selectedRest.id} • Joined {selectedRest.joinedDate}</p>
              </div>
              <button
                onClick={() => setSelectedRest(null)}
                className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800"
              >
                ✕
              </button>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800">
                <p className="text-xs text-slate-400">Owner Name</p>
                <p className="font-bold text-white">{selectedRest.owner}</p>
              </div>
              <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800">
                <p className="text-xs text-slate-400">Phone</p>
                <p className="font-bold text-white">{selectedRest.phone}</p>
              </div>
              <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800">
                <p className="text-xs text-slate-400">Delivery Radius</p>
                <p className="font-bold text-orange-400">{selectedRest.deliveryRadiusKm} km</p>
              </div>
              <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800">
                <p className="text-xs text-slate-400">Minimum Order</p>
                <p className="font-bold text-orange-400">₹{selectedRest.minOrderValue}</p>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800">
              <p className="text-xs text-slate-400">Payout Bank Details</p>
              <p className="font-bold text-emerald-400">{selectedRest.bankAccount}</p>
            </div>

            <div className="flex justify-end gap-3 pt-2">
              <button
                onClick={() => setSelectedRest(null)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-bold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
