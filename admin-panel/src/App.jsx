import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import DashboardOverview from './pages/DashboardOverview';
import RestaurantsManagement from './pages/RestaurantsManagement';
import LiveOrdersMonitor from './pages/LiveOrdersMonitor';
import SubscriptionsHub from './pages/SubscriptionsHub';
import PlatformSettings from './pages/PlatformSettings';

export default function App() {
  const [activeTab, setActiveTab] = useState('overview');

  const getPageInfo = () => {
    switch (activeTab) {
      case 'overview':
        return { title: 'Platform Control Overview', subtitle: 'Real-time metrics, MRR growth, and live platform pulse' };
      case 'restaurants':
        return { title: 'Restaurant Directory', subtitle: 'Manage registered restaurants, free trials, and listing statuses' };
      case 'orders':
        return { title: 'Live Multi-Restaurant Orders Pulse', subtitle: 'Monitor real-time sub-orders across all kitchens' };
      case 'subscriptions':
        return { title: 'Subscription & Revenue Hub', subtitle: 'Track monthly plans (₹999/mo), free trials, and auto-suspensions' };
      case 'settings':
        return { title: 'Platform Settings & Promos', subtitle: 'Manage global coupons, pricing rules, and server configurations' };
      default:
        return { title: 'OrderAra HQ', subtitle: 'Platform Management' };
    }
  };

  const { title, subtitle } = getPageInfo();

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden">
      {/* Sidebar Navigation */}
      <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        <Header 
          title={title} 
          subtitle={subtitle} 
          onRefresh={() => window.location.reload()} 
        />

        <main className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto">
            {activeTab === 'overview' && <DashboardOverview setActiveTab={setActiveTab} />}
            {activeTab === 'restaurants' && <RestaurantsManagement />}
            {activeTab === 'orders' && <LiveOrdersMonitor />}
            {activeTab === 'subscriptions' && <SubscriptionsHub />}
            {activeTab === 'settings' && <PlatformSettings />}
          </div>
        </main>
      </div>
    </div>
  );
}
