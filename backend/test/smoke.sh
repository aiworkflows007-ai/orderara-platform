#!/usr/bin/env bash
# End-to-end smoke test of every cross-app flow, against a locally running backend.
set -u
B=http://localhost:8080
j() { python3 -c "import sys,json;d=json.load(sys.stdin);print(json.dumps(d)[:$1])"; }
pass=0; fail=0
check() { # name  expected-substring  actual
  if echo "$3" | grep -q "$2"; then echo "  PASS  $1"; pass=$((pass+1));
  else echo "  FAIL  $1"; echo "        wanted: $2"; echo "        got:    $(echo "$3" | head -c 300)"; fail=$((fail+1)); fi
}

echo "=== Flow 1: partner registers -> visible to customers + admin ==="
REG=$(curl -s -X POST $B/api/partner/register -H 'Content-Type: application/json' -d '{
  "id":"rest_test","name":"Test Kitchen","ownerName":"Ashok","phone":"+91 90000 00001",
  "email":"test@kitchen.com","address":"MG Road, Bangalore","deliveryRadiusKm":6,
  "minOrderValue":100,"isVegOnly":false,"cuisineTypes":["Chai","Snacks"],"upiId":"test@upi"}')
check "register returns restaurant" '"id": *"rest_test"' "$REG"
check "starts on free trial"        'ACTIVE_TRIAL'      "$REG"
check "customer feed shows it"      'Test Kitchen'      "$(curl -s $B/api/restaurants)"
check "admin directory shows it"    'Test Kitchen'      "$(curl -s $B/api/admin/restaurants)"
check "partner profile loads"       '"menuItems"'       "$(curl -s $B/api/partner/profile/rest_test)"
check "duplicate register blocked"  'already registered' "$(curl -s -X POST $B/api/partner/register -H 'Content-Type: application/json' -d '{"id":"rest_test","name":"Dupe"}')"

echo "=== Flow 2: menu add / edit / stock / delete ==="
ADD=$(curl -s -X POST $B/api/partner/menu -H 'Content-Type: application/json' -d '{
  "restaurantId":"rest_test","name":"Masala Chai","price":40,"category":"Beverages",
  "description":"Hot cutting chai","isVeg":true}')
check "menu item created" 'Masala Chai' "$ADD"
ITEM=$(echo "$ADD" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['id'])")
check "customer sees new dish" 'Masala Chai' "$(curl -s $B/api/restaurants/rest_test)"
check "stock toggled off"  '"isAvailable": *false' "$(curl -s -X PATCH $B/api/partner/menu/$ITEM/toggle-stock)"
check "stock toggled on"   '"isAvailable": *true'  "$(curl -s -X PATCH $B/api/partner/menu/$ITEM/toggle-stock)"
check "price edited"       '"price": *45'          "$(curl -s -X PATCH $B/api/partner/menu/$ITEM -H 'Content-Type: application/json' -d '{"price":45}')"
check "bad restaurant rejected" 'valid restaurantId' "$(curl -s -X POST $B/api/partner/menu -H 'Content-Type: application/json' -d '{"restaurantId":"nope","name":"X","price":1}')"

echo "=== Flow 3: store settings ==="
check "closed store saved" '"isOpen": *false' "$(curl -s -X PATCH $B/api/partner/restaurant/rest_test/settings -H 'Content-Type: application/json' -d '{"isOpen":false}')"
check "closed store rejects order" 'is closed' "$(curl -s -X POST $B/api/orders -H 'Content-Type: application/json' -d '{"userId":"user_101","subOrders":[{"restaurantId":"rest_test","subTotal":200,"deliveryFee":35,"items":[{"menuItem":{"id":"x","name":"Masala Chai","price":45},"quantity":1}]}]}')"
check "reopened" '"isOpen": *true' "$(curl -s -X PATCH $B/api/partner/restaurant/rest_test/settings -H 'Content-Type: application/json' -d '{"isOpen":true,"minOrderValue":150}')"
check "min order enforced" 'minimum order' "$(curl -s -X POST $B/api/orders -H 'Content-Type: application/json' -d '{"userId":"user_101","subOrders":[{"restaurantId":"rest_test","subTotal":50,"deliveryFee":35,"items":[]}]}')"

echo "=== Flow 4: multi-restaurant order splits correctly ==="
ORD=$(curl -s -X POST $B/api/orders -H 'Content-Type: application/json' -d '{
 "userId":"user_101","userName":"Ashok Sharma","userPhone":"+91 98765 43210",
 "deliveryAddress":"Flat 402, Indiranagar","paymentMethod":"UPI",
 "subOrders":[
  {"restaurantId":"rest_test","subTotal":180,"deliveryFee":35,"discount":0,"items":[{"menuItem":{"id":"i1","name":"Masala Chai","price":45,"isVeg":true},"quantity":4,"totalPrice":180}]},
  {"restaurantId":"rest_1","subTotal":320,"deliveryFee":35,"discount":0,"items":[{"menuItem":{"id":"item_101","name":"Hyderabadi Chicken Dum Biryani","price":320},"quantity":1,"totalPrice":320}]}
 ]}')
check "order created" '"orderId"' "$ORD"
ORDID=$(echo "$ORD" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['orderId'])")
S1=$(echo "$ORD" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['subOrders'][0]['subOrderId'])")
S2=$(echo "$ORD" | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['subOrders'][1]['subOrderId'])")
check "sub-order ids are globally unique" "$ORDID-S1" "$S1"
check "two sub-orders created" "$ORDID-S2" "$S2"
check "test kitchen sees only its own" "$S1" "$(curl -s $B/api/partner/orders/rest_test)"
NOTMINE=$(curl -s $B/api/partner/orders/rest_test | grep -c "$S2" || true)
check "test kitchen does NOT see other restaurant's sub-order" "^0$" "$NOTMINE"
check "admin sees the parent order" "$ORDID" "$(curl -s $B/api/orders)"
check "customer order history" "$ORDID" "$(curl -s $B/api/orders/customer/user_101)"

echo "=== Flow 5: status lifecycle, independently per sub-order ==="
check "accepted"    'ACCEPTED'   "$(curl -s -X PATCH $B/api/orders/sub-order/$S1/status -H 'Content-Type: application/json' -d '{"status":"ACCEPTED","estimatedPrepMinutes":15}')"
check "preparing"   'PREPARING'  "$(curl -s -X PATCH $B/api/orders/sub-order/$S1/status -H 'Content-Type: application/json' -d '{"status":"PREPARING"}')"
check "out for delivery" 'OUT_FOR_DELIVERY' "$(curl -s -X PATCH $B/api/orders/sub-order/$S1/status -H 'Content-Type: application/json' -d '{"status":"OUT_FOR_DELIVERY","driverName":"Ramesh"}')"
check "delivered"   'DELIVERED'  "$(curl -s -X PATCH $B/api/orders/sub-order/$S1/status -H 'Content-Type: application/json' -d '{"status":"DELIVERED"}')"
OTHER=$(curl -s $B/api/orders/$ORDID | python3 -c "import sys,json;print(json.load(sys.stdin)['data']['subOrders'][1]['status'])")
check "second sub-order UNAFFECTED (still PLACED)" '^PLACED$' "$OTHER"
check "status history recorded" 'OUT_FOR_DELIVERY' "$(curl -s $B/api/orders/$ORDID)"
check "bad status rejected" 'must be one of' "$(curl -s -X PATCH $B/api/orders/sub-order/$S1/status -H 'Content-Type: application/json' -d '{"status":"NONSENSE"}')"

echo "=== Flow 6: chat both directions ==="
check "customer message" 'freshly hot' "$(curl -s -X POST $B/api/chat/$S1 -H 'Content-Type: application/json' -d '{"senderName":"Ashok","isFromCustomer":true,"text":"Please make it freshly hot"}')"
check "restaurant reply" 'on it' "$(curl -s -X POST $B/api/chat/$S1 -H 'Content-Type: application/json' -d '{"senderName":"Chef","isFromCustomer":false,"text":"Sure sir, on it"}')"
check "thread has both" 'Chef' "$(curl -s $B/api/chat/$S1)"
check "empty message rejected" 'required' "$(curl -s -X POST $B/api/chat/$S1 -H 'Content-Type: application/json' -d '{"text":"  "}')"

echo "=== Flow 8: rating updates the restaurant average ==="
check "review accepted" '"rating": *4' "$(curl -s -X POST $B/api/reviews -H 'Content-Type: application/json' -d "{\"restaurantId\":\"rest_test\",\"subOrderId\":\"$S1\",\"customerName\":\"Ashok\",\"rating\":4,\"comment\":\"Great chai\"}")"
check "sub-order marked rated" '"isRated": *true' "$(curl -s $B/api/orders/$ORDID)"
check "reviews listed" 'Great chai' "$(curl -s $B/api/reviews/rest_test)"
check "bad rating rejected" 'between 1 and 5' "$(curl -s -X POST $B/api/reviews -H 'Content-Type: application/json' -d '{"restaurantId":"rest_test","rating":9}')"

echo "=== Flow 7: subscription + suspension enforcement ==="
check "trial visible in partner view" 'ACTIVE_TRIAL' "$(curl -s $B/api/partner/subscription/rest_test)"
check "paid plan activates" 'ACTIVE_PAID' "$(curl -s -X POST $B/api/partner/subscription/rest_test/activate)"
check "invoice recorded" '999.00' "$(curl -s $B/api/partner/subscription/rest_test)"
check "admin suspends" 'SUSPENDED' "$(curl -s -X PATCH $B/api/admin/restaurants/rest_test/subscription -H 'Content-Type: application/json' -d '{"status":"SUSPENDED","reason":"Non-payment"}')"
GONE=$(curl -s $B/api/restaurants | grep -c 'Test Kitchen' || true)
check "SUSPENDED hidden from customer feed" '^0$' "$GONE"
check "admin still sees it" 'Test Kitchen' "$(curl -s $B/api/admin/restaurants)"
check "suspended restaurant rejects orders" 'subscription suspended' "$(curl -s -X POST $B/api/orders -H 'Content-Type: application/json' -d '{"userId":"user_101","subOrders":[{"restaurantId":"rest_test","subTotal":300,"deliveryFee":35,"items":[]}]}')"
check "admin reactivates" 'ACTIVE_PAID' "$(curl -s -X PATCH $B/api/admin/restaurants/rest_test/subscription -H 'Content-Type: application/json' -d '{"status":"ACTIVE_PAID"}')"
check "back in customer feed" 'Test Kitchen' "$(curl -s $B/api/restaurants)"
check "bad status rejected" 'must be one of' "$(curl -s -X PATCH $B/api/admin/restaurants/rest_test/subscription -H 'Content-Type: application/json' -d '{"status":"WHATEVER"}')"

echo "=== Analytics + admin stats ==="
check "partner analytics real" 'topSellingItems' "$(curl -s $B/api/partner/analytics/rest_test)"
check "chai counted in top sellers" 'Masala Chai' "$(curl -s $B/api/partner/analytics/rest_test)"
check "admin stats" 'currentMRR' "$(curl -s $B/api/admin/stats)"

echo
echo "RESULT: $pass passed, $fail failed"
[ $fail -eq 0 ]
