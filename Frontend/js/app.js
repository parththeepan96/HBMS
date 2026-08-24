// ---------------- Session (in-memory only - no browser storage) ----------------
const Session = {
  state: { token: null, userId: null, name: null, email: null },
  getToken() { return this.state.token; },
  isAuthenticated() { return !!this.state.token; },
  setUser({ token, userId, name, email }) {
    this.state = { token, userId, name, email: email || this.state.email };
  },
  clear() { this.state = { token: null, userId: null, name: null, email: null }; }
};

// ---------------- View router ----------------
const Views = {
  all: ["auth", "search", "book", "payment", "bookings", "account"],
  show(name) {
    this.all.forEach(v => {
      document.getElementById(`view-${v}`)?.classList.toggle("active", v === name);
    });
    document.querySelectorAll("#mainNav button").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.view === name);
    });
  }
};

function alertBox(containerId, type, message) {
  const el = document.getElementById(containerId);
  el.innerHTML = `<div class="alert ${type}">${message}</div>`;
}
function clearAlert(containerId) {
  document.getElementById(containerId).innerHTML = "";
}

// ---------------- Confirm modal ----------------
// Promise-based replacement for window.confirm() that renders centred on the page,
// styled to match the rest of the app instead of the browser's native dialog box.
function confirmModal({ title = "Are you sure?", body = "", confirmText = "OK", cancelText = "Cancel", danger = false } = {}) {
  return new Promise((resolve) => {
    const root = document.getElementById("modalRoot");

    const close = (result) => {
      root.innerHTML = "";
      document.removeEventListener("keydown", onKeydown);
      resolve(result);
    };
    const onKeydown = (e) => {
      if (e.key === "Escape") close(false);
    };
    document.addEventListener("keydown", onKeydown);

    root.innerHTML = `
      <div class="modal-overlay">
        <div class="modal-card" role="dialog" aria-modal="true">
          <p class="modal-title">${title}</p>
          <p class="modal-body">${body}</p>
          <div class="modal-actions">
            <button type="button" class="btn outline" id="modalCancelBtn">${cancelText}</button>
            <button type="button" class="btn ${danger ? "danger" : "gold"}" id="modalConfirmBtn">${confirmText}</button>
          </div>
        </div>
      </div>
    `;

    root.querySelector(".modal-overlay").addEventListener("click", (e) => {
      if (e.target.classList.contains("modal-overlay")) close(false);
    });
    root.querySelector("#modalCancelBtn").addEventListener("click", () => close(false));
    root.querySelector("#modalConfirmBtn").addEventListener("click", () => close(true));
  });
}

// ---------------- Live room availability registry ----------------
// Tracks the currently-rendered DOM row (if any) for each hotel/room pair so that a booking
// or cancellation elsewhere in the app (e.g. from the bookings list) can flip that room's
// card in the search results immediately, without re-running the search.
const roomRowRegistry = new Map();

function registerRoomRow(hotelId, roomId, room, row) {
  roomRowRegistry.set(`${hotelId}|${roomId}`, { room, row });
}

function setRoomRowAvailability(hotelId, roomId, isAvailable) {
  const key = `${hotelId}|${roomId}`;
  const entry = roomRowRegistry.get(key);
  if (!entry || !entry.row || !document.body.contains(entry.row)) {
    roomRowRegistry.delete(key);
    return;
  }
  entry.room.isAvailable = isAvailable;
  const typeEl = entry.row.querySelector(".room-type");
  if (typeEl) {
    typeEl.innerHTML = `${entry.room.type} ${isAvailable ? "" : '<span class="badge rejected">Unavailable</span>'}`;
  }
  const btn = entry.row.querySelector("button");
  if (btn) {
    btn.disabled = !isAvailable;
    btn.textContent = isAvailable ? "Select" : "Booked";
    btn.classList.toggle("gold", isAvailable);
  }
}

// ---------------- Session bar ----------------
function refreshSessionBar() {
  const label = document.getElementById("sessionLabel");
  const action = document.getElementById("sessionAction");
  if (Session.isAuthenticated()) {
    label.textContent = `Signed in as ${Session.state.name}`;
    action.textContent = "Sign Out";
  } else {
    label.textContent = "Not signed in";
    action.textContent = "Sign In";
  }
}

document.getElementById("sessionAction").addEventListener("click", () => {
  if (Session.isAuthenticated()) {
    Session.clear();
    refreshSessionBar();
    Views.show("search");
  } else {
    Views.show("auth");
  }
});

document.getElementById("mainNav").addEventListener("click", (e) => {
  const btn = e.target.closest("button[data-view]");
  if (!btn) return;
  const target = btn.dataset.view;

  if ((target === "bookings" || target === "account") && !Session.isAuthenticated()) {
    Views.show("auth");
    alertBox("authAlert", "info", "Please sign in first to view this page.");
    return;
  }
  Views.show(target);
  if (target === "bookings") loadBookings();
  if (target === "account") loadAccount();
});

// ---------------- Register / Login (User Service - BR1) ----------------
document.getElementById("registerForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAlert("authAlert");
  try {
    await Api.register({
      name: document.getElementById("regName").value,
      email: document.getElementById("regEmail").value,
      phone: document.getElementById("regPhone").value,
      password: document.getElementById("regPassword").value
    });
    alertBox("authAlert", "success", "Account created. You can now sign in on the left.");
    e.target.reset();
  } catch (err) {
    alertBox("authAlert", "error", err.message);
  }
});

document.getElementById("loginForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAlert("authAlert");
  try {
    const email = document.getElementById("loginEmail").value;
    const result = await Api.login({
      email,
      password: document.getElementById("loginPassword").value
    });
    Session.setUser({ token: result.token, userId: result.userId, name: result.name, email });
    refreshSessionBar();
    alertBox("authAlert", "success", `Welcome back, ${result.name}.`);
    Views.show("search");
  } catch (err) {
    alertBox("authAlert", "error", err.message);
  }
});

// ---------------- Search hotels (Hotel Service - BR2) ----------------
document.getElementById("searchForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  await runSearch();
});

async function runSearch() {
  const location = document.getElementById("searchLocation").value.trim();
  const name = document.getElementById("searchName").value.trim();
  const resultsEl = document.getElementById("hotelResults");
  resultsEl.innerHTML = `<div class="empty-state">Searching…</div>`;

  try {
    const hotels = await Api.searchHotels(location, name);
    if (!hotels || hotels.length === 0) {
      resultsEl.innerHTML = `<div class="empty-state"><div class="glyph">—</div><p>No hotels matched your search.</p></div>`;
      return;
    }
    resultsEl.innerHTML = "";
    for (const hotel of hotels) {
      resultsEl.appendChild(await renderHotelCard(hotel));
    }
  } catch (err) {
    resultsEl.innerHTML = `<div class="alert error">${err.message}</div>`;
  }
}

async function renderHotelCard(hotel) {
  const card = document.createElement("div");
  card.className = "hotel-card";
  card.innerHTML = `
    <div class="hotel-name">${hotel.name}</div>
    <div class="hotel-loc">${hotel.location}</div>
    <div class="hotel-desc">${hotel.description || ""}</div>
    <div class="rooms-container">Loading rooms…</div>
  `;

  try {
    const rooms = await Api.getRooms(hotel.hotelId);
    const roomsEl = card.querySelector(".rooms-container");
    if (!rooms || rooms.length === 0) {
      roomsEl.innerHTML = `<p style="color:var(--muted);font-size:13px;">No rooms listed for this hotel yet.</p>`;
    } else {
      roomsEl.innerHTML = "";
      rooms.forEach(room => {
        const row = document.createElement("div");
        row.className = "room-row";
        row.innerHTML = `
          <div class="room-meta">
            <div class="room-type">${room.type} ${room.isAvailable ? "" : '<span class="badge rejected">Unavailable</span>'}</div>
            <div class="room-facilities">${(room.facilities || []).join(" · ") || "Standard amenities"}</div>
          </div>
          <div style="display:flex;align-items:center;">
            <div class="room-price">£${Number(room.pricePerNight).toFixed(2)}<small>per night</small></div>
            <button class="btn small ${room.isAvailable ? "gold" : ""}" ${room.isAvailable ? "" : "disabled"}>
              ${room.isAvailable ? "Select" : "Booked"}
            </button>
          </div>
        `;
        row.querySelector("button").addEventListener("click", () => openBookingView(hotel, room, row));
        registerRoomRow(hotel.hotelId, room.roomId, room, row);
        roomsEl.appendChild(row);
      });
    }
  } catch (err) {
    card.querySelector(".rooms-container").innerHTML = `<p style="color:var(--danger);font-size:13px;">Could not load rooms.</p>`;
  }

  return card;
}

// ---------------- Booking flow (Booking Service BR3 -> Payment Service BR4, two pages) ----------------
let pendingSelection = null;   // { hotel, room, rowEl }
let pendingDetails = null;     // { checkIn, checkOut, adults, children, specialRequests, nights, totalAmount }

function openBookingView(hotel, room, rowEl) {
  if (!Session.isAuthenticated()) {
    Views.show("auth");
    alertBox("authAlert", "info", "Please sign in before booking a room.");
    return;
  }
  pendingSelection = { hotel, room, rowEl };
  pendingDetails = null;
  document.getElementById("bookHotelSub").textContent =
    `${hotel.name} — ${room.type} at £${Number(room.pricePerNight).toFixed(2)} / night`;

  document.getElementById("bookSummary").innerHTML = `
    <p><strong>${hotel.name}</strong><br/><span style="color:var(--muted);font-size:13px;">${hotel.location}</span></p>
    <p style="margin-top:14px;">Room type: <strong>${room.type}</strong></p>
    <p>Facilities: ${(room.facilities || []).join(", ") || "Standard amenities"}</p>
    <p>Rate: <strong>£${Number(room.pricePerNight).toFixed(2)}</strong> per night</p>
  `;
  clearAlert("bookAlert");
  document.getElementById("bookForm").reset();
  document.getElementById("guestsAdults").value = 2;
  document.getElementById("guestsChildren").value = 0;
  Views.show("book");
}

// Step 1: dates, guests, requests -> move to payment page
document.getElementById("bookForm").addEventListener("submit", (e) => {
  e.preventDefault();
  clearAlert("bookAlert");

  if (!pendingSelection) return;
  const { hotel, room } = pendingSelection;
  const checkIn = document.getElementById("checkIn").value;
  const checkOut = document.getElementById("checkOut").value;
  const adults = parseInt(document.getElementById("guestsAdults").value, 10) || 1;
  const children = parseInt(document.getElementById("guestsChildren").value, 10) || 0;
  const specialRequests = document.getElementById("specialRequests").value.trim();

  if (new Date(checkOut) <= new Date(checkIn)) {
    alertBox("bookAlert", "error", "Check-out date must be after check-in date.");
    return;
  }
  if (adults < 1) {
    alertBox("bookAlert", "error", "At least one adult guest is required.");
    return;
  }

  const nights = Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000);
  const totalAmount = nights * Number(room.pricePerNight);

  pendingDetails = { checkIn, checkOut, adults, children, specialRequests, nights, totalAmount };

  // Build the payment-page order summary
  document.getElementById("paymentSummary").innerHTML = `
    <p><strong>${hotel.name}</strong><br/><span style="color:var(--muted);font-size:13px;">${hotel.location}</span></p>
    <p style="margin-top:14px;">Room type: <strong>${room.type}</strong></p>
    <p>${checkIn} → ${checkOut} <span style="color:var(--muted);">(${nights} night${nights === 1 ? "" : "s"})</span></p>
    <p>Guests: <strong>${adults} adult${adults === 1 ? "" : "s"}${children ? `, ${children} child${children === 1 ? "" : "ren"}` : ""}</strong></p>
    ${specialRequests ? `<p>Requests: ${specialRequests}</p>` : ""}
    <p style="margin-top:14px;border-top:1px solid var(--line);padding-top:14px;">Total: <strong style="color:var(--blue-900);font-size:18px;">£${totalAmount.toFixed(2)}</strong></p>
  `;
  clearAlert("paymentAlert");
  document.getElementById("paymentForm").reset();
  Views.show("payment");
});

document.getElementById("paymentBackBtn").addEventListener("click", () => {
  clearAlert("paymentAlert");
  Views.show("book");
});

// Step 2: card details -> submit booking + payment together
document.getElementById("paymentForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAlert("paymentAlert");

  if (!pendingSelection || !pendingDetails) {
    Views.show("book");
    return;
  }
  const { hotel, room, rowEl } = pendingSelection;
  const { checkIn, checkOut, adults, children, specialRequests } = pendingDetails;
  const cardNumber = document.getElementById("cardNumber").value;

  const submitBtn = e.target.querySelector("button[type=submit]");
  submitBtn.disabled = true;
  submitBtn.textContent = "Processing…";

  try {
    const result = await Api.createBooking({
      userId: Session.state.userId,
      hotelId: hotel.hotelId,
      roomId: room.roomId,
      checkInDate: checkIn,
      checkOutDate: checkOut,
      adults,
      children,
      specialRequests,
      cardNumber
    });

    if (result.status === "CONFIRMED") {
      // Reflect the now-booked room as unavailable in the search results, in place, without
      // re-running the search or reloading the page.
      setRoomRowAvailability(hotel.hotelId, room.roomId, false);

      pendingSelection = null;
      pendingDetails = null;

      Views.show("bookings");
      await loadBookings();
      alertBox("bookingsAlert", "success",
        `Booking confirmed — reference #${result.bookingId}. Guests: ${adults} adult${adults === 1 ? "" : "s"}${children ? `, ${children} child${children === 1 ? "" : "ren"}` : ""}. Total charged: £${Number(result.totalAmount).toFixed(2)}. A confirmation email is on its way.`);
    } else {
      alertBox("paymentAlert", "error", result.message || "Booking could not be completed.");
    }
  } catch (err) {
    alertBox("paymentAlert", "error", err.message);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Pay & Confirm Booking";
  }
});

// ---------------- Booking history (BR3) ----------------
async function loadBookings() {
  clearAlert("bookingsAlert");
  const listEl = document.getElementById("bookingsList");
  listEl.innerHTML = `<div class="empty-state">Loading your bookings…</div>`;

  try {
    const bookings = await Api.getBookingHistory(Session.state.userId);
    if (!bookings || bookings.length === 0) {
      listEl.innerHTML = `<div class="empty-state"><div class="glyph">—</div><p>No bookings yet. Go search for a hotel.</p></div>`;
      return;
    }
    listEl.innerHTML = "";
    bookings.forEach(b => listEl.appendChild(renderBookingItem(b)));
  } catch (err) {
    listEl.innerHTML = `<div class="alert error">${err.message}</div>`;
  }
}

function renderBookingItem(booking) {
  const el = document.createElement("div");
  el.className = "booking-item";
  const statusClass = {
    CONFIRMED: "ok", PENDING: "pending", REJECTED: "rejected", CANCELLED: "cancelled"
  }[booking.status] || "pending";

  el.innerHTML = `
    <div>
      <div class="booking-id">Booking #${booking.id}</div>
      <div style="margin-top:4px;">
        <span class="badge ${statusClass}">${booking.status}</span>
        <span style="margin-left:10px;color:var(--muted);font-size:13px;">
          ${booking.checkInDate} → ${booking.checkOutDate}
        </span>
      </div>
      <div style="margin-top:4px;color:var(--muted);font-size:13px;">
        Guests: ${booking.adults || 1} adult${(booking.adults || 1) === 1 ? "" : "s"}${booking.children ? `, ${booking.children} child${booking.children === 1 ? "" : "ren"}` : ""}
        ${booking.specialRequests ? `<br/>Requests: ${booking.specialRequests}` : ""}
      </div>
    </div>
    <div style="display:flex;align-items:center;gap:16px;">
      <div class="booking-amount">£${Number(booking.totalAmount).toFixed(2)}</div>
      ${booking.status === "CONFIRMED" ? `<button class="btn small danger">Cancel</button>` : ""}
    </div>
  `;

  const cancelBtn = el.querySelector("button");
  if (cancelBtn) {
    cancelBtn.addEventListener("click", async () => {
      const confirmed = await confirmModal({
        title: "Cancel this booking?",
        body: `Do you want to cancel booking #${booking.id} (${booking.checkInDate} → ${booking.checkOutDate})? This will refund your payment where eligible and cannot be undone.`,
        confirmText: "Yes, cancel booking",
        cancelText: "Keep booking",
        danger: true
      });
      if (!confirmed) return;

      cancelBtn.disabled = true;
      cancelBtn.textContent = "Cancelling…";
      try {
        const result = await Api.cancelBooking(booking.id);
        // Free the room back up immediately for this user's search results, without
        // requiring a fresh search.
        setRoomRowAvailability(booking.hotelId, booking.roomId, true);

        // Reload the list first - loadBookings() clears the alert box as soon as it starts,
        // so the message must be set after it finishes, not before.
        await loadBookings();

        if (result && result.refundStatus && result.refundStatus !== "Refunded") {
          alertBox("bookingsAlert", "error",
            `Booking #${booking.id} was cancelled, but the refund did not complete (${result.refundStatus}). Please contact support.`);
        } else {
          const refundAmount = result && result.refundAmount != null ? Number(result.refundAmount) : 0;
          alertBox("bookingsAlert", "success",
            `Booking #${booking.id} cancelled. Refund of £${refundAmount.toFixed(2)} processed and notification sent.`);
        }
      } catch (err) {
        alertBox("bookingsAlert", "error", err.message);
        cancelBtn.disabled = false;
        cancelBtn.textContent = "Cancel";
      }
    });
  }

  return el;
}

// ---------------- Account (BR1) ----------------
async function loadAccount() {
  clearAlert("accountAlert");
  document.getElementById("accountDetails").innerHTML = `
    <strong>Name:</strong> ${Session.state.name}<br/>
    <strong>Email:</strong> ${Session.state.email || "—"}<br/>
    <strong>Phone:</strong> Loading…<br/>
    <strong>User ID:</strong> ${Session.state.userId}
  `;

  try {
    const profile = await Api.getUserProfile(Session.state.userId);
    document.getElementById("accountDetails").innerHTML = `
      <strong>Name:</strong> ${profile.name || Session.state.name}<br/>
      <strong>Email:</strong> ${profile.email || Session.state.email || "—"}<br/>
      <strong>Phone:</strong> ${profile.phone || "—"}<br/>
      <strong>User ID:</strong> ${Session.state.userId}
    `;
    document.getElementById("accountName").value = profile.name || Session.state.name || "";
    document.getElementById("accountPhone").value = profile.phone || "";
  } catch (err) {
    document.getElementById("accountDetails").innerHTML = `
      <strong>Name:</strong> ${Session.state.name}<br/>
      <strong>Email:</strong> ${Session.state.email || "—"}<br/>
      <strong>Phone:</strong> —<br/>
      <strong>User ID:</strong> ${Session.state.userId}
    `;
    document.getElementById("accountName").value = Session.state.name || "";
    document.getElementById("accountPhone").value = "";
  }
}

document.getElementById("accountForm").addEventListener("submit", async (e) => {
  e.preventDefault();
  clearAlert("accountAlert");

  const name = document.getElementById("accountName").value.trim();
  const phone = document.getElementById("accountPhone").value.trim();
  if (!name) {
    alertBox("accountAlert", "error", "Name is required.");
    return;
  }

  const submitBtn = e.target.querySelector("button[type=submit]");
  submitBtn.disabled = true;
  submitBtn.textContent = "Saving…";

  try {
    await Api.updateProfile(Session.state.userId, { name, phone });
    Session.state.name = name;
    refreshSessionBar();
    alertBox("accountAlert", "success", "Details updated.");
    await loadAccount();
  } catch (err) {
    alertBox("accountAlert", "error", err.message);
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = "Save changes";
  }
});

// ---------------- Init ----------------
refreshSessionBar();
Views.show("search");
