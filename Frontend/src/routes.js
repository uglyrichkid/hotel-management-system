import React from 'react'

const Dashboard = React.lazy(() => import('./views/dashboard/Dashboard'))
const Hotels = React.lazy(() => import('./views/hotels/Hotels'))
const Rooms = React.lazy(() => import('./views/rooms/Rooms'))
const Guests = React.lazy(() => import('./views/guests/Guests'))
const Bookings = React.lazy(() => import('./views/bookings/Bookings'))
const BookingCreate = React.lazy(() => import('./views/bookings/BookingCreate'))
const BookingManage = React.lazy(() => import('./views/bookings/BookingManage'))
const Payments = React.lazy(() => import('./views/payments/Payments'))
const Reports = React.lazy(() => import('./views/reports/Reports'))
const Users = React.lazy(() => import('./views/users/Users'))
const HotelCreate = React.lazy(() => import('./views/hotels/HotelCreate'))
const HotelManage = React.lazy(() => import('./views/hotels/HotelManage'))
const RoomManage = React.lazy(() => import('./views/rooms/RoomManage'))

const ROLES = {
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  HOTEL_ADMIN: 'HOTEL_ADMIN',
  OPERATIONS_MANAGER: 'OPERATIONS_MANAGER',
  FRONT_DESK: 'FRONT_DESK',
}

const ALL_HOTEL_STAFF = [
  ROLES.SYSTEM_ADMIN,
  ROLES.HOTEL_ADMIN,
  ROLES.OPERATIONS_MANAGER,
  ROLES.FRONT_DESK,
]

const MANAGERS_AND_UP = [
  ROLES.SYSTEM_ADMIN,
  ROLES.HOTEL_ADMIN,
  ROLES.OPERATIONS_MANAGER,
]

const ADMINS_ONLY = [
  ROLES.SYSTEM_ADMIN,
  ROLES.HOTEL_ADMIN,
]

const SYSTEM_ADMIN_ONLY = [
  ROLES.SYSTEM_ADMIN,
]

const routes = [
  { path: '/', exact: true, name: 'Home' },
  
  {
    path: '/dashboard',
    name: 'Dashboard',
    element: Dashboard,
    roles: ALL_HOTEL_STAFF,
  },

  {
    path: '/hotels',
    name: 'Hotels',
    element: Hotels,
    roles: SYSTEM_ADMIN_ONLY,
  },
  {
    path: '/hotels/create',
    name: 'Hotel Create',
    element: HotelCreate,
    roles: SYSTEM_ADMIN_ONLY,
  },
  {
    path: '/hotels/:id/manage',
    name: 'Hotel Manage',
    element: HotelManage,
    roles: ADMINS_ONLY,
    requiresHotelAccess: true,
  },

  {
    path: '/rooms',
    name: 'Rooms',
    element: Rooms,
    roles: ALL_HOTEL_STAFF,
  },
  {
    path: '/rooms/:id',
    name: 'Manage Room',
    element: RoomManage,
    roles: ALL_HOTEL_STAFF,
  },

  {
    path: '/guests',
    name: 'Guests',
    element: Guests,
    roles: ALL_HOTEL_STAFF,
  },

  {
    path: '/bookings',
    name: 'Bookings',
    element: Bookings,
    roles: ALL_HOTEL_STAFF,
  },
  {
    path: '/bookings/create',
    name: 'Create Booking',
    element: BookingCreate,
    roles: ALL_HOTEL_STAFF,
  },
  {
    path: '/bookings/:id',
    name: 'Booking Manage',
    element: BookingManage,
    roles: ALL_HOTEL_STAFF,
  },

  {
    path: '/payments',
    name: 'Payments',
    element: Payments,
    roles: ALL_HOTEL_STAFF,
  },

  {
    path: '/reports',
    name: 'Reports',
    element: Reports,
    roles: MANAGERS_AND_UP,
  },

  {
    path: '/users',
    name: 'Users & Access',
    element: Users,
    roles: ADMINS_ONLY,
  },
]

export default routes