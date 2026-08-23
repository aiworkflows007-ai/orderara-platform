import React, { useState } from 'react';
import { CreditCard, CheckCircle2, Clock, AlertTriangle, ShieldCheck, Sparkles, Plus } from 'lucide-react';
import { initialRestaurants } from '../mockData';

export default function SubscriptionsHub() {
  const [restaurants, setRestaurants] = useState(initialRestaurants);

  const activePaid = restaurants.filter(r => r.subscriptionStatus === 'ACTIVE_PAID').length;
  const activeTrials = restaurants.filter(r => r.subscriptionStatus === 'ACTIVE_TRIAL').length;
  const suspended = restaurants.filter(r => r.subscriptionStatus === 'SUSPENDED').length;

  const currentMRR = activePaid * 999;
  const projectedMRR = (activePaid + activeTrials) * 999;

  return (
    <div className="space-y-6">
      {/* Top Metrics Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Current Monthly Revenue</span>
            <div className="w-8 h-8 rounded-lg bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center">
              <CreditCard className="w-4 h-4 text-emerald-400" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-black text-white">₹{currentMRR.toLocaleString()}</span>
            <p className="text-xs text-slate-400 mt-1">{activePaid} paying restaurants @ ₹999/mo</p>
          </div>
        </div>

        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Projected Pipeline MRR</span>
            <div className="w-8 h-8 rounded-lg bg-purple-500/10 border border-purple-500/20 flex items-center justify-center">
              <Sparkles className="w-4 h-4 text-purple-400" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-black text-purple-400">₹{projectedMRR.toLocaleString()}</span>
            <p className="text-xs text-slate-400 mt-1">If all {activeTrials} active free trials convert</p>
          </div>
        </div>

        <div className="p-5 rounded-2xl bg-slate-900/70 border border-slate-800 shadow-sm">
          <div className="flex items-center justify-between">
            <span className="text-xs font-bold uppercase tracking-wider text-slate-400">Active Free Trials</span>
            <div className="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center">
              <Clock className="w-4 h-4 text-amber-400" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-black text-amber-400">{activeTrials}</span>
            <p className="text-xs text-slate-400 mt-1">14-Day Free Period Active</p>
          </div>
        </div>
      </div>

      {/* Subscription Plan Model Rule Card */}
      <div className="p-6 rounded-2xl bg-gradient-to-r from-slate-900 via-slate-900 to-orange-950/30 border border-slate-800 space-y-4">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div>
            <div className="flex items-center gap-2">
              <span className="px-2.5 py-0.5 rounded-full text-[10px] font-black uppercase tracking-wider bg-orange-500 text-white">
                Platform Pricing Model
              </span>
              <h3 className="text-lg font-black text-white">OrderAra Unlimited Partner Subscription</h3>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Flat rate per restaurant • Unlimited orders • 0% Commission • Self-onboarding automated
            </p>
          </div>

          <div className="text-right">
            <span className="text-2xl font-black text-orange-400">₹999</span>
            <span className="text-xs text-slate-400 font-bold"> / month / restaurant</span>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-3 pt-2 text-xs">
          <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span className="text-slate-200 font-semibold">14-Day Free Trial upon restaurant signup</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span className="text-slate-200 font-semibold">3-Day Grace Period on missed payment</span>
          </div>
          <div className="p-3 rounded-xl bg-slate-800/50 border border-slate-800 flex items-center gap-2.5">
            <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            <span className="text-slate-200 font-semibold">Auto-Suspend listing if subscription lapses</span>
          </div>
        </div>
      </div>

      {/* Restaurant Billing Directory */}
      <div className="rounded-2xl bg-slate-900/70 border border-slate-800 overflow-hidden shadow-sm">
        <div className="p-4 border-b border-slate-800 flex items-center justify-between">
          <h4 className="font-bold text-white text-sm">Restaurant Subscription Status & Invoices</h4>
          <span className="text-xs text-slate-400 font-semibold">{restaurants.length} Registered Accounts</span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead>
              <tr className="border-b border-slate-800 bg-slate-950/40 text-xs font-bold uppercase tracking-wider text-slate-400">
                <th className="py-3 px-4">Restaurant</th>
                <th className="py-3 px-4">Plan & Rate</th>
                <th className="py-3 px-4">Subscription Status</th>
                <th className="py-3 px-4">Trial / Renewal</th>
                <th className="py-3 px-4">Linked Payout Bank</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {restaurants.map(r => (
                <tr key={r.id} className="hover:bg-slate-800/30 transition-colors">
                  <td className="py-3.5 px-4 font-bold text-white">{r.name}</td>
                  <td className="py-3.5 px-4 font-semibold text-slate-300">₹999/mo Unlimited</td>
                  <td className="py-3.5 px-4">
                    {r.subscriptionStatus === 'ACTIVE_PAID' && (
                      <span className="px-2.5 py-1 rounded-full text-xs font-extrabold bg-emerald-500/15 text-emerald-400 border border-emerald-500/30">
                        Paid & Active
                      </span>
                    )}
                    {r.subscriptionStatus === 'ACTIVE_TRIAL' && (
                      <span className="px-2.5 py-1 rounded-full text-xs font-extrabold bg-amber-500/15 text-amber-400 border border-amber-500/30">
                        Free Trial ({r.trialDaysRemaining}d left)
                      </span>
                    )}
                    {r.subscriptionStatus === 'SUSPENDED' && (
                      <span className="px-2.5 py-1 rounded-full text-xs font-extrabold bg-red-500/15 text-red-400 border border-red-500/30">
                        Suspended (Unpaid)
                      </span>
                    )}
                  </td>
                  <td className="py-3.5 px-4 text-xs font-mono text-slate-400">
                    {r.subscriptionStatus === 'ACTIVE_TRIAL' ? `${r.trialDaysRemaining} Days Remaining` : 'Renews 05 Sept 2026'}
                  </td>
                  <td className="py-3.5 px-4 text-xs font-bold text-slate-300">{r.bankAccount}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
