const {onDocumentCreated, onDocumentUpdated} = require("firebase-functions/v2/firestore");
const {initializeApp} = require("firebase-admin/app");
const {getFirestore, FieldValue} = require("firebase-admin/firestore");
const {getMessaging} = require("firebase-admin/messaging");

initializeApp();

const db = getFirestore();
const messaging = getMessaging();

const BIDS_CHANNEL = "bids";
const ORDERS_CHANNEL = "orders";

/**
 * @param {string[]} tokens
 * @param {{title: string, body: string, type: string, channelId?: string,
 *   requestId?: string, orderId?: string, bookingId?: string}} payload
 */
async function sendToTokens(tokens, payload) {
  const unique = [...new Set(tokens.filter(Boolean))];
  if (unique.length === 0) return;

  const channelId = payload.channelId || BIDS_CHANNEL;

  const response = await messaging.sendEachForMulticast({
    tokens: unique,
    notification: {
      title: payload.title,
      body: payload.body,
    },
    data: {
      type: payload.type,
      title: payload.title,
      body: payload.body,
      channelId,
      ...(payload.requestId ? {requestId: payload.requestId} : {}),
      ...(payload.orderId ? {orderId: payload.orderId} : {}),
      ...(payload.bookingId ? {bookingId: payload.bookingId} : {}),
    },
    android: {
      priority: "high",
      notification: {
        channelId,
      },
    },
  });

  const stale = [];
  response.responses.forEach((result, index) => {
    if (!result.success) {
      const code = result.error?.code;
      if (code === "messaging/registration-token-not-registered" ||
          code === "messaging/invalid-registration-token") {
        stale.push(unique[index]);
      }
    }
  });
  return stale;
}

/**
 * @param {string} userId
 * @return {Promise<string[]>}
 */
async function tokensForUser(userId) {
  if (!userId) return [];
  const snap = await db.collection("users").doc(userId).get();
  const tokens = snap.get("fcmTokens");
  return Array.isArray(tokens) ? tokens : [];
}

/**
 * @param {string} shopId
 * @return {Promise<string[]>}
 */
async function tokensForShopOwner(shopId) {
  if (!shopId) return [];
  const shop = await db.collection("shops").doc(shopId).get();
  const ownerId = shop.get("ownerId");
  return tokensForUser(ownerId);
}

/**
 * @param {string} userId
 * @param {string[]} staleTokens
 */
async function removeStaleTokens(userId, staleTokens) {
  if (!userId || staleTokens.length === 0) return;
  await db.collection("users").doc(userId).update({
    fcmTokens: FieldValue.arrayRemove(...staleTokens),
  });
}

function formatMoney(amount) {
  const value = Number(amount);
  if (Number.isNaN(value)) return "";
  return `ZMW ${value.toFixed(0)}`;
}

/**
 * @param {object} order
 * @return {string}
 */
function summarizePartOrder(order) {
  const items = Array.isArray(order.items) ? order.items : [];
  if (items.length === 0) return "New part order";
  if (items.length === 1) {
    const item = items[0];
    const qty = Number(item.quantity) || 1;
    return `${item.name || "Part"} x${qty}`;
  }
  const totalQty = items.reduce((sum, item) => sum + (Number(item.quantity) || 1), 0);
  return `${items.length} items (${totalQty} parts)`;
}

/**
 * @param {string} status
 * @param {boolean} isService
 * @return {{title: string, body: string}|null}
 */
function statusUpdateCopy(status, isService) {
  const label = isService ? "booking" : "order";
  const titleLabel = isService ? "Booking" : "Order";
  switch (status) {
    case "CONFIRMED":
      return {
        title: `${titleLabel} confirmed`,
        body: `Your ${label} was confirmed by the seller.`,
      };
    case "COMPLETED":
      return {
        title: `${titleLabel} completed`,
        body: `Your ${label} has been marked complete.`,
      };
    case "CANCELLED":
      return {
        title: `${titleLabel} cancelled`,
        body: `Your ${label} was cancelled.`,
      };
    default:
      return null;
  }
}

/**
 * @param {string} userId
 * @param {{title: string, body: string, type: string, channelId?: string,
 *   requestId?: string, orderId?: string, bookingId?: string}} payload
 */
async function notifyUser(userId, payload) {
  const tokens = await tokensForUser(userId);
  const stale = await sendToTokens(tokens, payload);
  if (stale?.length) await removeStaleTokens(userId, stale);
}

/**
 * @param {string} partId
 * @param {number} delta
 */
async function adjustPartCommitted(partId, delta) {
  if (!partId || !delta) return;
  const ref = db.collection("part_listings").doc(partId);
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(ref);
    if (!snap.exists) return;
    const current = Number(snap.get("committedQuantity")) || 0;
    tx.update(ref, {committedQuantity: Math.max(0, current + delta)});
  });
}

/**
 * @param {object} order
 * @param {number} multiplier
 */
async function adjustOrderCommitted(order, multiplier) {
  const items = Array.isArray(order.items) ? order.items : [];
  for (const item of items) {
    const partId = item.id;
    const qty = Number(item.quantity) || 1;
    if (partId) await adjustPartCommitted(partId, qty * multiplier);
  }
}

/** Customer notified when a shop places a bid. */
exports.onBidCreated = onDocumentCreated(
    "breakdown_requests/{requestId}/bids/{bidId}",
    async (event) => {
      const bid = event.data?.data();
      if (!bid) return;

      const requestId = event.params.requestId;
      const requestSnap = await db.collection("breakdown_requests").doc(requestId).get();
      if (!requestSnap.exists) return;

      const request = requestSnap.data();
      if (!request || request.status !== "BIDDING") return;

      const userId = request.userId;
      const tokens = await tokensForUser(userId);
      const stale = await sendToTokens(tokens, {
        title: "New bid received",
        body: `${bid.shopName} bid ${formatMoney(bid.price)} · ETA ${bid.etaMinutes} min`,
        type: "new_bid",
        requestId,
      });
      if (stale?.length) await removeStaleTokens(userId, stale);
    },
);

/** Winning provider notified when customer accepts a bid. */
exports.onBidAccepted = onDocumentUpdated(
    "breakdown_requests/{requestId}",
    async (event) => {
      const before = event.data?.before.data();
      const after = event.data?.after.data();
      if (!before || !after) return;

      const wasAccepted = before.status === "ACCEPTED" || before.status === "IN_PROGRESS";
      const isAccepted = after.status === "ACCEPTED" || after.status === "IN_PROGRESS";
      if (wasAccepted || !isAccepted) return;

      const requestId = event.params.requestId;
      const shopId = after.acceptedShopId;
      const acceptedBid = after.acceptedBid || {};
      const shopSnap = shopId ? await db.collection("shops").doc(shopId).get() : null;
      const ownerId = shopSnap?.get("ownerId");

      const tokens = await tokensForShopOwner(shopId);
      const stale = await sendToTokens(tokens, {
        title: "Your bid was accepted",
        body: `Job confirmed at ${formatMoney(acceptedBid.price)} · ${after.locationLabel || "Rescue request"}`,
        type: "bid_won",
        requestId,
      });
      if (stale?.length && ownerId) await removeStaleTokens(ownerId, stale);

      // Notify other bidders they were not selected.
      const bidsSnap = await db.collection("breakdown_requests")
          .doc(requestId)
          .collection("bids")
          .get();
      const winnerShopId = shopId;
      for (const bidDoc of bidsSnap.docs) {
        const bid = bidDoc.data();
        if (!bid.shopId || bid.shopId === winnerShopId) continue;
        const loserTokens = await tokensForShopOwner(bid.shopId);
        const loserShop = await db.collection("shops").doc(bid.shopId).get();
        const loserOwnerId = loserShop.get("ownerId");
        const loserStale = await sendToTokens(loserTokens, {
          title: "Bid not selected",
          body: "Another shop was chosen for this rescue request.",
          type: "bid_lost",
          requestId,
        });
        if (loserStale?.length && loserOwnerId) {
          await removeStaleTokens(loserOwnerId, loserStale);
        }
      }
    },
);

/** Online providers notified about a new open rescue request. */
exports.onOpenJobCreated = onDocumentCreated(
    "breakdown_requests/{requestId}",
    async (event) => {
      const request = event.data?.data();
      if (!request || request.status !== "BIDDING") return;

      const requestId = event.params.requestId;
      const typeLabel = request.type === "TOWING" ? "Towing" : "Mechanic";
      const shopsSnap = await db.collection("shops").where("isOnline", "==", true).get();

      for (const shopDoc of shopsSnap.docs) {
        const ownerId = shopDoc.get("ownerId");
        if (!ownerId || ownerId === request.userId) continue;
        const tokens = await tokensForUser(ownerId);
        const stale = await sendToTokens(tokens, {
          title: "New rescue request",
          body: `${typeLabel} · ${request.locationLabel || "Nearby"} — place a bid`,
          type: "open_job",
          requestId,
        });
        if (stale?.length) await removeStaleTokens(ownerId, stale);
      }
    },
);

/** Shop owner notified when a customer places a part order. */
exports.onPartOrderCreated = onDocumentCreated(
    "orders/{orderId}",
    async (event) => {
      const order = event.data?.data();
      if (!order) return;

      const orderId = event.params.orderId;
      const ownerId = order.shopOwnerId;
      await adjustOrderCommitted(order, 1);
      const summary = summarizePartOrder(order);
      await notifyUser(ownerId, {
        title: "New part order",
        body: `${summary} · ${formatMoney(order.totalPrice)}`,
        type: "new_part_order",
        channelId: ORDERS_CHANNEL,
        orderId,
      });
    },
);

/** Buyer notified when a part order status changes. */
exports.onPartOrderUpdated = onDocumentUpdated(
    "orders/{orderId}",
    async (event) => {
      const before = event.data?.before.data();
      const after = event.data?.after.data();
      if (!before || !after) return;

      if (before.status !== after.status) {
        if (after.status === "CANCELLED" && before.status !== "CANCELLED") {
          await adjustOrderCommitted(after, -1);
        } else if (before.status === "CANCELLED" && after.status !== "CANCELLED") {
          await adjustOrderCommitted(after, 1);
        }
      }

      if (before.status === after.status) return;

      const copy = statusUpdateCopy(after.status, false);
      if (!copy) return;

      const orderId = event.params.orderId;
      await notifyUser(after.buyerId, {
        ...copy,
        type: "part_order_update",
        channelId: ORDERS_CHANNEL,
        orderId,
      });
    },
);

/** Shop owner notified when a customer books a service. */
exports.onServiceBookingCreated = onDocumentCreated(
    "service_bookings/{bookingId}",
    async (event) => {
      const booking = event.data?.data();
      if (!booking) return;

      const bookingId = event.params.bookingId;
      await notifyUser(booking.shopOwnerId, {
        title: "New service booking",
        body: `${booking.serviceName} · ${booking.preferredDate || "Date TBC"} · ${formatMoney(booking.price)}`,
        type: "new_service_booking",
        channelId: ORDERS_CHANNEL,
        bookingId,
      });
    },
);

/** Buyer notified when a service booking status changes. */
exports.onServiceBookingUpdated = onDocumentUpdated(
    "service_bookings/{bookingId}",
    async (event) => {
      const before = event.data?.before.data();
      const after = event.data?.after.data();
      if (!before || !after) return;
      if (before.status === after.status) return;

      const copy = statusUpdateCopy(after.status, true);
      if (!copy) return;

      const bookingId = event.params.bookingId;
      await notifyUser(after.buyerId, {
        ...copy,
        type: "service_booking_update",
        channelId: ORDERS_CHANNEL,
        bookingId,
      });
    },
);
