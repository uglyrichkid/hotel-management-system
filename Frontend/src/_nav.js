import React from 'react'
import CIcon from '@coreui/icons-react'
import {
  cilBed,
  cilBuilding,
  cilChartPie,
  cilCreditCard,
  cilPeople,
  cilSettings,
  cilSpeedometer,
  cilUser,
} from '@coreui/icons'
import { CNavItem, CNavTitle } from '@coreui/react'

const ROLES = {
  SYSTEM_ADMIN: 'SYSTEM_ADMIN',
  HOTEL_ADMIN: 'HOTEL_ADMIN',
  OPERATIONS_MANAGER: 'OPERATIONS_MANAGER',
  FRONT_DESK: 'FRONT_DESK',
}

const _nav = [
  {
    component: CNavItem,
    name: 'Dashboard',
    to: '/dashboard',
    icon: <CIcon icon={cilSpeedometer} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavTitle,
    name: 'PMS Modules',
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavItem,
    name: 'Hotels',
    to: '/hotels',
    icon: <CIcon icon={cilBuilding} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN],
  },
  {
    component: CNavItem,
    name: 'My Hotel',
    to: '/my-hotel',
    icon: <CIcon icon={cilBuilding} customClassName="nav-icon" />,
    roles: [ROLES.HOTEL_ADMIN],
  },
  {
    component: CNavItem,
    name: 'Rooms',
    to: '/rooms',
    icon: <CIcon icon={cilBed} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavItem,
    name: 'Guests',
    to: '/guests',
    icon: <CIcon icon={cilPeople} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavItem,
    name: 'Bookings',
    to: '/bookings',
    icon: <CIcon icon={cilSettings} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavItem,
    name: 'Payments',
    to: '/payments',
    icon: <CIcon icon={cilCreditCard} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER, ROLES.FRONT_DESK],
  },
  {
    component: CNavItem,
    name: 'Reports',
    to: '/reports',
    icon: <CIcon icon={cilChartPie} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN, ROLES.OPERATIONS_MANAGER],
  },
  {
    component: CNavItem,
    name: 'Users & Access',
    to: '/users',
    icon: <CIcon icon={cilUser} customClassName="nav-icon" />,
    roles: [ROLES.SYSTEM_ADMIN, ROLES.HOTEL_ADMIN],
  },
]

export default _nav