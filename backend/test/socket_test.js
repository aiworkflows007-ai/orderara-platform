// Verifies that a new order rings ONLY the restaurant that owns it,
// and that the customer + admin get status updates.
const io = require('../../admin-panel/node_modules/socket.io-client');
const B = 'http://localhost:8080';

const got = { partnerA: [], partnerB: [], customer: [], admin: [] };

function connect(name, joinPayload) {
  return new Promise(resolve => {
    const s = io(B, { transports: ['websocket'] });
    s.on('connect', () => s.emit('join', joinPayload));
    s.on('joined', () => resolve(s));
    ['order:new', 'order:status', 'chat:new', 'subscription:updated'].forEach(ev =>
      s.on(ev, d => got[name].push(`${ev}:${d.subOrderId || (d.subOrder && d.subOrder.subOrderId) || d.status || ''}`))
    );
  });
}

(async () => {
  const pA = await connect('partnerA', { role: 'partner', restaurantId: 'rest_test' });
  const pB = await connect('partnerB', { role: 'partner', restaurantId: 'rest_1' });
  const cu = await connect('customer', { role: 'customer', userId: 'user_777' });
  const ad = await connect('admin', { role: 'admin' });

  const post = (path, body) => fetch(B + path, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
  }).then(r => r.json());
  const patch = (path, body) => fetch(B + path, {
    method: 'PATCH', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
  }).then(r => r.json());

  const order = await post('/api/orders', {
    userId: 'user_777', userName: 'Socket Tester', deliveryAddress: 'Test Rd',
    subOrders: [{ restaurantId: 'rest_test', subTotal: 400, deliveryFee: 35, items: [] }]
  });
  const sub = order.data.subOrders[0].subOrderId;
  await new Promise(r => setTimeout(r, 400));

  await patch(`/api/orders/sub-order/${sub}/status`, { status: 'ACCEPTED' });
  await post(`/api/chat/${sub}`, { senderName: 'Chef', isFromCustomer: false, text: 'cooking now' });
  await post('/api/partner/subscription/rest_test/activate', {});
  await new Promise(r => setTimeout(r, 500));

  const checks = [
    ['owning restaurant got the new order', got.partnerA.some(e => e.startsWith('order:new'))],
    ['OTHER restaurant did NOT get it', !got.partnerB.some(e => e.startsWith('order:new'))],
    ['admin got the new order', got.admin.some(e => e.startsWith('order:new'))],
    ['customer got status update', got.customer.some(e => e.startsWith('order:status'))],
    ['owning restaurant got status update', got.partnerA.some(e => e.startsWith('order:status'))],
    ['OTHER restaurant got no status update', !got.partnerB.some(e => e.startsWith('order:status'))],
    ['customer got chat message', got.customer.some(e => e.startsWith('chat:new'))],
    ['restaurant got chat message', got.partnerA.some(e => e.startsWith('chat:new'))],
    ['restaurant got subscription update', got.partnerA.some(e => e.startsWith('subscription:updated'))],
    ['admin got subscription update', got.admin.some(e => e.startsWith('subscription:updated'))]
  ];
  let fail = 0;
  checks.forEach(([n, ok]) => { console.log(`  ${ok ? 'PASS' : 'FAIL'}  ${n}`); if (!ok) fail++; });
  console.log(`\nRESULT: ${checks.length - fail} passed, ${fail} failed`);
  [pA, pB, cu, ad].forEach(s => s.close());
  process.exit(fail ? 1 : 0);
})();
