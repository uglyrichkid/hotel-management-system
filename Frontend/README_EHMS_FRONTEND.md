# EHMS Frontend Notes

## What is already connected
- JWT login: `POST /api/auth/login`
- Hotels list/create/deactivate
- Rooms list/create/clean/delete
- Guests list/create
- Bookings list/create/status actions
- Payments create + list by booking ID + summary
- Reports (bookings/payments/occupancy)
- Users list/create

## Important backend notes
- Vite proxy is configured to forward `/api/*` to `http://localhost:8080`
- Payments page uses the actual backend controller paths:
  - `GET /api/bookings/{bookingId}/payments`
  - `GET /api/bookings/{bookingId}/payments/summary`
- Some create forms require manual numeric IDs because lookup endpoints for cities, room types, and roles are not exposed yet.

## Run
```bash
npm install
npm start
```
