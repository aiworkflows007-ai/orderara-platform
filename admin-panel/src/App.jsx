import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import DashboardOverview from './pages/DashboardOverview';
import RestaurantsManagement from './pages/RestaurantsManagement';
import LiveOrdersMonitor from './pages/LiveOrdersMonitor';
import SubscriptionsHub from './pages/SubscriptionsHub';
import PlatformSettings from './pages/PlatformSettings';

export default function App() {
  const [activeTab, setActiveTab] = useState('subscriptions');

  const getPageInfo = () => {
    switch (activeTab) {
      case 'subscriptions':
        return { 
          title: 'Restaurant Subscriptions & MRR Management', 
          subtitle: 'Manage flat ₹999/mo partner subscriptions, 14-day free trials, and auto-suspensions' 
        };
      case 'restaurants':
        return { 
          title: 'Partner Restaurants Directory', 
          subtitle: 'Manage onboarded restaurant businesses, delivery radius, and subscription listings' 
        };
      case 'overview':
        return { 
          title: 'Platform Overview Dashboard', 
          subtitle: 'Real-time metrics, MRR growth, and platform activity pulse' 
        };
      case 'orders':
        return { 
          title: 'Live Multi-Restaurant Orders Pulse', 
          subtitle: 'Monitor real-time sub-orders across all restaurant kitchens' 
        };
      case 'settings':
        return { 
          title: 'Subscription Policies & Billing Settings', 
          subtitle: 'Configure subscription fee, free trial duration, grace period, and global promo codes' 
        };
      default:
        return { title: 'Restaurant Partner Portal', subtitle: 'Subscription & Management Hub' };
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
            {activeTab === 'subscriptions' && <SubscriptionsHub />}
            {activeTab === 'restaurants' && <RestaurantsManagement />}
            {activeTab === 'overview' && <DashboardOverview setActiveTab={setActiveTab} />}
            {activeTab === 'orders' && <LiveOrdersMonitor />}
            {activeTab === 'settings' && <PlatformSettings />}
          </div>
        </main>
      </div>
    </div>
  );
}
