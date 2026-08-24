// Thin wrapper around fetch() for every call the frontend makes.
// Everything routes through the API Gateway (API_BASE_URL), never directly
// to an individual microservice - see report section 4 & 5.
const Api = {

  async request(path, { method = "GET", body, auth = false } = {}) {
    const headers = { "Content-Type": "application/json" };
    if (auth) {
      const token = Session.getToken();
      if (token) headers["Authorization"] = `Bearer ${token}`;
    }

    const res = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      body: body ? JSON.stringify(body) : undefined
    });

    let data = null;
    try { data = await res.json(); } catch (_) { /* no body */ }

    if (!res.ok) {
      const message = (data && (data.message || data.title)) || `Request failed (${res.status})`;
      throw new Error(message);
    }
    return data;
  },

  // ---- User Service ----
  register(payload) {
    return this.request("/users/register", { method: "POST", body: payload });
  },
  login(payload) {
    return this.request("/users/login", { method: "POST", body: payload });
  },
  getUserProfile(userId) {
    return this.request(`/users/${userId}`, { auth: true });
  },
  updateProfile(userId, payload) {
    return this.request(`/users/${userId}/profile`, { method: "PUT", body: payload, auth: true });
  },

  // ---- Hotel Service ----
  searchHotels(location, name) {
    const params = new URLSearchParams();
    if (location) params.set("location", location);
    if (name) params.set("name", name);
    const qs = params.toString();
    return this.request(`/hotels${qs ? "?" + qs : ""}`);
  },
  getRooms(hotelId) {
    return this.request(`/hotels/${hotelId}/rooms`);
  },

  // ---- Booking Service ----
  createBooking(payload) {
    return this.request("/bookings", { method: "POST", body: payload, auth: true });
  },
  cancelBooking(bookingId) {
    return this.request(`/bookings/${bookingId}`, { method: "DELETE", auth: true });
  },
  getBookingHistory(userId) {
    return this.request(`/bookings/user/${userId}`, { auth: true });
  }
};
