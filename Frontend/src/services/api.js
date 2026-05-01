export function buildQuery(params = {}) {
  const query = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '' || value === false) return
    query.append(key, value)
  })

  const str = query.toString()
  return str ? `?${str}` : ''
}

export async function apiFetch(path, options = {}, token = '') {
  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...(options.headers || {}),
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(path, {
    ...options,
    headers,
  })

  const contentType = response.headers.get('content-type') || ''
  const isJson = contentType.includes('application/json')
  const data = isJson ? await response.json().catch(() => null) : await response.text()

  if (!response.ok) {
    const message =
      data?.message ||
      data?.error ||
      data?.details ||
      (typeof data === 'string' && data) ||
      `Request failed with status ${response.status}`

    const error = new Error(message)
    error.status = response.status
    error.payload = data
    throw error
  }

  return data
}

export const bookingApi = {
  getAllByHotel: (hotelId, params = {}, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings${buildQuery(params)}`, {}, token),

  getById: (hotelId, bookingId, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings/${bookingId}`, {}, token),

  create: (hotelId, body, token = '') =>
    apiFetch(
      `/api/hotels/${hotelId}/bookings`,
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),

  update: (hotelId, bookingId, body, token = '') =>
    apiFetch(
      `/api/hotels/${hotelId}/bookings/${bookingId}`,
      {
        method: 'PUT',
        body: JSON.stringify(body),
      },
      token,
    ),

  confirm: (hotelId, bookingId, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings/${bookingId}/confirm`, { method: 'PATCH' }, token),

  cancel: (hotelId, bookingId, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings/${bookingId}/cancel`, { method: 'PATCH' }, token),

  checkIn: (hotelId, bookingId, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings/${bookingId}/check-in`, { method: 'PATCH' }, token),

  checkOut: (hotelId, bookingId, token = '') =>
    apiFetch(`/api/hotels/${hotelId}/bookings/${bookingId}/check-out`, { method: 'PATCH' }, token),

  quickCreateGuest: (hotelId, body, token = '') =>
    apiFetch(
      `/api/hotels/${hotelId}/bookings/quick-guest`,
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),

  getFinancial: (bookingId, token = '') =>
    apiFetch(`/api/bookings/${bookingId}/financial`, {}, token),

  getPayments: (bookingId, token = '') =>
    apiFetch(`/api/bookings/${bookingId}/payments`, {}, token),
}

export const guestApi = {
  getAll: (params = {}, token = '') =>
    apiFetch(`/api/guests${buildQuery(params)}`, {}, token),

  create: (body, token = '') =>
    apiFetch(
      '/api/guests',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),
}

export const roomApi = {
  getAvailable: (params = {}, token = '') =>
    apiFetch(`/api/rooms/available${buildQuery(params)}`, {}, token),
}

export const paymentApi = {
  payCash: (body, token = '') =>
    apiFetch(
      '/api/payments/cash',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),

  payCard: (body, token = '') =>
    apiFetch(
      '/api/payments/card',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),
}

export const reportsApi = {
  getBookings: (params = {}, token = '') =>
    apiFetch(`/api/reports/bookings${buildQuery(params)}`, {}, token),

  getRevenue: (params = {}, token = '') =>
    apiFetch(`/api/reports/revenue${buildQuery(params)}`, {}, token),

  getOccupancy: (params = {}, token = '') =>
    apiFetch(`/api/reports/occupancy${buildQuery(params)}`, {}, token),
}

export const usersApi = {
  getAll: (token = '') =>
    apiFetch('/api/access/users', {}, token),

  getById: (id, token = '') =>
    apiFetch(`/api/access/users/${id}`, {}, token),

  create: (body, token = '') =>
    apiFetch(
      '/api/access/users',
      {
        method: 'POST',
        body: JSON.stringify(body),
      },
      token,
    ),

  update: (id, body, token = '') =>
    apiFetch(
      `/api/access/users/${id}`,
      {
        method: 'PUT',
        body: JSON.stringify(body),
      },
      token,
    ),

  updateStatus: (id, status, token = '') =>
    apiFetch(
      `/api/access/users/${id}/status`,
      {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      },
      token,
    ),

  getRoles: (token = '') =>
    apiFetch('/api/access/roles', {}, token),

  getHotels: (token = '') =>
    apiFetch('/api/access/hotels', {}, token),
}