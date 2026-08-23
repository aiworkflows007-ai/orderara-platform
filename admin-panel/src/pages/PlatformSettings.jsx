import React, { useState } from 'react';
import { Tag, Plus, CheckCircle2, Shield, Server, Smartphone, Save } from 'lucide-react';
import { platformCoupons } from '../mockData';

export default function PlatformSettings() {
  const [coupons, setCoupons] = useState(platformCoupons);
  const [newCode, setNewCode] = useState('');
  const [newDiscount, setNewDiscount] = useState('');
  const [newMinOrder, setNewMinOrder] = useState('');
  const [showSaved, setShowSaved] = useState(false);

  const toggleCoupon = (code) => {
    setCoupons(prev => prev.map(c => c.code === code ? { ...c, active: !c.active } : c));
  };

  const addCoupon = (e) => {
    e.preventDefault();
    if (!newCode.trim()) return;
    setCoupons(prev => [
      ...prev,
      {
        code: newCode.trim().toUpperCase(),
        discount: newDiscount || 'Flat ₹50 OFF',
        minOrder: Number(newMinOrder) || 199,
        appliesTo: 'All Restaurants (Platform-wide)',
        active: true
      }
    ]);
    setNewCode('');
    setNewDiscount('');
    setNewMinOrder('');
  };

  return (
    <div className="space-y-6">
      {/* Platform General Config */}
      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <Shield className="w-4 h-4 text-orange-400" />
            General Platform Configuration
          </h3>
          <span className="text-xs font-semibold text-slate-400">OrderAra Core v1.0.0</span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
          <div>
            <label className="text-xs font-bold text-slate-400 block mb-1.5">Platform Brand Name</label>
            <input
              type="text"
              defaultValue="OrderAra"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-800/60 border border-slate-700 text-white font-bold text-sm focus:outline-none focus:border-orange-500"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-400 block mb-1.5">Default Free Trial (Days)</label>
            <input
              type="number"
              defaultValue="14"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-800/60 border border-slate-700 text-white font-bold text-sm focus:outline-none focus:border-orange-500"
            />
          </div>

          <div>
            <label className="text-xs font-bold text-slate-400 block mb-1.5">Monthly Subscription Fee (₹)</label>
            <input
              type="number"
              defaultValue="999"
              className="w-full px-3.5 py-2.5 rounded-xl bg-slate-800/60 border border-slate-700 text-white font-bold text-sm focus:outline-none focus:border-orange-500"
            />
          </div>
        </div>

        <div className="pt-2">
          <button
            onClick={() => {
              setShowSaved(true);
              setTimeout(() => setShowSaved(false), 3000);
            }}
            className="px-5 py-2.5 rounded-xl bg-orange-500 hover:bg-orange-600 text-white text-xs font-bold transition-all shadow-md shadow-orange-500/20 flex items-center gap-2"
          >
            <Save className="w-3.5 h-3.5" />
            Save Platform Settings
          </button>
          {showSaved && (
            <p className="text-xs text-emerald-400 font-bold mt-2 flex items-center gap-1">
              <CheckCircle2 className="w-3.5 h-3.5" /> Settings saved successfully!
            </p>
          )}
        </div>
      </div>

      {/* Platform Coupons & Promos */}
      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-4">
        <div className="flex items-center justify-between border-b border-slate-800 pb-3">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Tag className="w-4 h-4 text-orange-400" />
              Global Platform Promo Coupons
            </h3>
            <p className="text-xs text-slate-400">Coupons valid across all restaurants during customer checkout</p>
          </div>
        </div>

        {/* Add Coupon Form */}
        <form onSubmit={addCoupon} className="grid grid-cols-1 sm:grid-cols-4 gap-3 p-4 rounded-xl bg-slate-800/40 border border-slate-800">
          <div>
            <label className="text-[11px] font-bold text-slate-400 block mb-1">Coupon Code</label>
            <input
              type="text"
              value={newCode}
              onChange={(e) => setNewCode(e.target.value)}
              placeholder="e.g. ARA50"
              className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs font-bold text-white uppercase focus:outline-none focus:border-orange-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-bold text-slate-400 block mb-1">Discount Text</label>
            <input
              type="text"
              value={newDiscount}
              onChange={(e) => setNewDiscount(e.target.value)}
              placeholder="e.g. Flat ₹50 OFF"
              className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs font-bold text-white focus:outline-none focus:border-orange-500"
            />
          </div>
          <div>
            <label className="text-[11px] font-bold text-slate-400 block mb-1">Min Order Value (₹)</label>
            <input
              type="number"
              value={newMinOrder}
              onChange={(e) => setNewMinOrder(e.target.value)}
              placeholder="e.g. 199"
              className="w-full px-3 py-2 rounded-lg bg-slate-900 border border-slate-700 text-xs font-bold text-white focus:outline-none focus:border-orange-500"
            />
          </div>
          <div className="flex items-end">
            <button
              type="submit"
              className="w-full py-2 rounded-lg bg-emerald-500 hover:bg-emerald-600 text-white text-xs font-bold transition-all shadow-md shadow-emerald-500/20 flex items-center justify-center gap-1.5"
            >
              <Plus className="w-3.5 h-3.5" />
              Create Coupon
            </button>
          </div>
        </form>

        {/* Coupons List */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2">
          {coupons.map(c => (
            <div key={c.code} className="p-4 rounded-xl bg-slate-800/50 border border-slate-800 space-y-2 relative">
              <div className="flex items-center justify-between">
                <span className="font-mono text-sm font-black text-white px-2 py-0.5 rounded bg-orange-500/15 text-orange-400 border border-orange-500/30">
                  {c.code}
                </span>
                <button
                  onClick={() => toggleCoupon(c.code)}
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    c.active ? 'bg-emerald-500/20 text-emerald-400' : 'bg-slate-700 text-slate-400'
                  }`}
                >
                  {c.active ? 'ACTIVE' : 'INACTIVE'}
                </button>
              </div>
              <p className="text-xs font-bold text-slate-200">{c.discount}</p>
              <p className="text-[11px] text-slate-400">Min Order: ₹{c.minOrder} • {c.appliesTo}</p>
            </div>
          ))}
        </div>
      </div>

      {/* System & Mobile Devices Health */}
      <div className="p-6 rounded-2xl bg-slate-900/70 border border-slate-800 space-y-3">
        <h3 className="text-base font-bold text-white flex items-center gap-2">
          <Server className="w-4 h-4 text-emerald-400" />
          Live Architecture Health
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
          <div className="p-3.5 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
              <div>
                <p className="font-bold text-white">OrderAra Real-time Backend Engine</p>
                <p className="text-slate-400">Port 8080 • Node.js + Express + Socket.IO</p>
              </div>
            </div>
            <span className="text-emerald-400 font-extrabold">ONLINE</span>
          </div>

          <div className="p-3.5 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <Smartphone className="w-4 h-4 text-emerald-400" />
              <div>
                <p className="font-bold text-white">Mobile Test Device (ADB Port 8080)</p>
                <p className="text-slate-400">Device ID: 00196654C005228</p>
              </div>
            </div>
            <span className="text-emerald-400 font-extrabold">CONNECTED</span>
          </div>
        </div>
      </div>
    </div>
  );
}
