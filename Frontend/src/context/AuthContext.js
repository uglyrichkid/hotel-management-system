import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'

const STORAGE_KEY = 'ehms-auth'
const HOTEL_KEY = 'ehms-selected-hotel'
const HOTEL_NAME_KEY = 'ehms-selected-hotel-name'

const SYSTEM_ADMIN = 'SYSTEM_ADMIN'
const HOTEL_ADMIN = 'HOTEL_ADMIN'
const OPERATIONS_MANAGER = 'OPERATIONS_MANAGER'
const FRONT_DESK = 'FRONT_DESK'

const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState('')
  const [selectedHotelId, setSelectedHotelIdState] = useState('')
  const [selectedHotelName, setSelectedHotelNameState] = useState('')

  useEffect(() => {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return

    try {
      const parsed = JSON.parse(raw)
      setUser(parsed.user || null)
      setToken(parsed.token || '')
    } catch {
      localStorage.removeItem(STORAGE_KEY)
    }
  }, [])

  useEffect(() => {
    const storedHotelId = localStorage.getItem(HOTEL_KEY)
    const storedHotelName = localStorage.getItem(HOTEL_NAME_KEY)

    if (storedHotelId) setSelectedHotelIdState(storedHotelId)
    if (storedHotelName) setSelectedHotelNameState(storedHotelName)
  }, [])

  useEffect(() => {
    if (!user) return

    const allowedHotels = user.hotelIds || []

    if (!allowedHotels.length) {
      setSelectedHotelIdState('')
      setSelectedHotelNameState('')
      localStorage.removeItem(HOTEL_KEY)
      localStorage.removeItem(HOTEL_NAME_KEY)
      return
    }

    const current = selectedHotelId || localStorage.getItem(HOTEL_KEY)

    if (!current || !allowedHotels.includes(Number(current))) {
      const fallback = String(allowedHotels[0])
      setSelectedHotelIdState(fallback)
      localStorage.setItem(HOTEL_KEY, fallback)

      setSelectedHotelNameState('')
      localStorage.removeItem(HOTEL_NAME_KEY)
    }
  }, [user, selectedHotelId])

  const login = (loginResponse) => {
    const authUser = {
      userId: loginResponse.userId,
      email: loginResponse.email,
      fullName: loginResponse.fullName,
      status: loginResponse.status,
      roles: loginResponse.roles || [],
      hotelIds: loginResponse.hotelIds || [],
    }

    setUser(authUser)
    setToken(loginResponse.token)

    localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        user: authUser,
        token: loginResponse.token,
      }),
    )

    const defaultHotelId = loginResponse.hotelIds?.length ? String(loginResponse.hotelIds[0]) : ''
    setSelectedHotelIdState(defaultHotelId)

    if (defaultHotelId) {
      localStorage.setItem(HOTEL_KEY, defaultHotelId)
    } else {
      localStorage.removeItem(HOTEL_KEY)
    }

    setSelectedHotelNameState('')
    localStorage.removeItem(HOTEL_NAME_KEY)
  }

  const logout = () => {
    setUser(null)
    setToken('')
    setSelectedHotelIdState('')
    setSelectedHotelNameState('')

    localStorage.removeItem(STORAGE_KEY)
    localStorage.removeItem(HOTEL_KEY)
    localStorage.removeItem(HOTEL_NAME_KEY)
  }

  const setSelectedHotelId = (value) => {
    const next = value ? String(value) : ''
    setSelectedHotelIdState(next)

    if (next) {
      localStorage.setItem(HOTEL_KEY, next)
    } else {
      localStorage.removeItem(HOTEL_KEY)
    }
  }

  const setSelectedHotelName = (value) => {
    const next = value || ''
    setSelectedHotelNameState(next)

    if (next) {
      localStorage.setItem(HOTEL_NAME_KEY, next)
    } else {
      localStorage.removeItem(HOTEL_NAME_KEY)
    }
  }

  const roles = user?.roles || []
  const hotelIds = user?.hotelIds || []

  const hasRole = (...allowedRoles) => {
    if (!roles.length) return false
    return allowedRoles.some((role) => roles.includes(role))
  }

  const isSystemAdmin = hasRole(SYSTEM_ADMIN)
  const isHotelAdmin = hasRole(HOTEL_ADMIN)
  const isOperationsManager = hasRole(OPERATIONS_MANAGER)
  const isFrontDesk = hasRole(FRONT_DESK)

  const canAccessHotel = (hotelId) => {
    if (!hotelId) return false
    if (isSystemAdmin) return true
    return hotelIds.includes(Number(hotelId))
  }

  const canAccessRoute = (allowedRoles = []) => {
    if (!allowedRoles?.length) return true
    if (!user) return false
    return allowedRoles.some((role) => roles.includes(role))
  }

  const value = useMemo(
    () => ({
      user,
      token,
      roles,
      hotelIds,
      isAuthenticated: Boolean(token),
      login,
      logout,
      selectedHotelId,
      selectedHotelName,
      setSelectedHotelId,
      setSelectedHotelName,
      hasRole,
      canAccessHotel,
      canAccessRoute,
      isSystemAdmin,
      isHotelAdmin,
      isOperationsManager,
      isFrontDesk,
      ROLE_NAMES: {
        SYSTEM_ADMIN,
        HOTEL_ADMIN,
        OPERATIONS_MANAGER,
        FRONT_DESK,
      },
    }),
    [user, token, roles, hotelIds, selectedHotelId, selectedHotelName, isSystemAdmin, isHotelAdmin, isOperationsManager, isFrontDesk],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider')
  }
  return context
}